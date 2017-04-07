package com.beautifulbeanbuilder.processor;

import com.beautifulbeanbuilder.processor.info.InfoBuilder;
import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.endsWith;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BBBProcessor extends javax.annotation.processing.AbstractProcessor {

    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;

    private static boolean headerPrinted = false;
    private static List<AbstractGenerator> allGenerators;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> generatorAnnotations = allGenerators.stream().map(g -> g.getAnnotationClass().getName()).collect(Collectors.toSet());
        generatorAnnotations.add("*");
        return generatorAnnotations;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();

        try {
            allGenerators = ClassPath.from(getClass().getClassLoader()).getAllClasses().stream()
                    .filter(c -> c.getSimpleName().contains("Generator") && !c.getName().equals(AbstractGenerator.class.getName()))
                    .map(c -> safeLoad(c))
                    .filter(c -> AbstractGenerator.class.isAssignableFrom(c))
                    .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                    .map(c -> newGenerator(c))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Class<?> safeLoad(ClassPath.ClassInfo c) {
        try {
            return c.load();
        } catch (Throwable e) {
            return String.class;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //See if any of these annotations extend from a generator annotation
        if (!shouldClaimTheseAnnotations(annotations)) {
            return false;
        }


        final Set<TypeElement> elementThatNeedProcessing = Sets.newHashSet();
        annotations.forEach(a -> {
            Set e = roundEnv.getElementsAnnotatedWith(a);
            elementThatNeedProcessing.addAll(e);
        });
        final Set<TypeElement> types = ElementFilter.typesIn(elementThatNeedProcessing);

        for (TypeElement te : types) {
            String currentTypeName = te.getSimpleName().toString();
            String currentTypePackage = elementUtils.getPackageOf(te).toString();
            process(te, currentTypeName, currentTypePackage, roundEnv);
        }

        return true;
    }

    private boolean shouldClaimTheseAnnotations(Set<? extends TypeElement> annotations) {
        for (TypeElement t : annotations) {
            for (AbstractGenerator g : allGenerators) {
                final String annClassName = t.getQualifiedName().toString();
                final String generatorAnnClassName = g.getAnnotationClass().getName();

                if (annClassName.equals(generatorAnnClassName)) {
                    return true;
                }

                for (String all : getAllAnnotations(t)) {
                    if (all.equals(generatorAnnClassName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public static boolean hasAnnotation(Element te, Class<? extends Annotation> ann) {
        return getAllAnnotations(te).stream()
                .filter(a -> a.equals(ann.getName()))
                .findAny()
                .isPresent();
    }

    public static Set<String> getAllAnnotations(Element te) {
        Set<String> res = Sets.newHashSet();
        getAllAnnotations(te, res);
        return res;
    }

    public static void getAllAnnotations(Element te, Set<String> res) {
        for (AnnotationMirror am : te.getAnnotationMirrors()) {
            if (res.add(am.getAnnotationType().toString())) {
                getAllAnnotations(am.getAnnotationType().asElement(), res);
            }
        }
    }


    public void process(TypeElement te, String currentTypeName, String currentTypePackage, RoundEnvironment roundEnvironment) {
        try {

            printHeader();

            //Analyze the Def class
            final InfoClass ic = new InfoBuilder().init(processingEnv, te, currentTypeName, currentTypePackage);

            //Holds a bunch of builders that have been run
            final Map<AbstractGenerator, TypeSpec.Builder> processedBuilders = Maps.newHashMap();

            //Run the generators
            for (String annotationClassName : getAllAnnotations(te)) {
                runGeneratorforAnnotation(ic, processedBuilders, allGenerators, annotationClassName);
            }

            //Write the code out
            for (TypeSpec.Builder builder : processedBuilders.values()) {
                write(ic, builder);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runGeneratorforAnnotation(InfoClass ic, Map<AbstractGenerator, TypeSpec.Builder> processedBuilders, List<AbstractGenerator> allGenerators, String annotationClassName) {
        allGenerators
                .stream()
                .filter(g -> g.getAnnotationClass().getName().equals(annotationClassName))
                .filter(g -> !processedBuilders.containsKey(g))
                .findAny()
                .ifPresent(g -> runGenerator(ic, processedBuilders, allGenerators, g));
    }

    private void runGenerator(InfoClass ic, Map<AbstractGenerator, TypeSpec.Builder> processedBuilders, List<AbstractGenerator> allGenerators, AbstractGenerator g) {
        printBeanStatus(ic.typeElement, g);
        checkBBBUsage(ic.typeElement);

        try {
            for (Object wtf : g.requires()) {
                Class<? extends Annotation> aClass = (Class<? extends Annotation>) wtf;
                runGeneratorforAnnotation(ic, processedBuilders, allGenerators, aClass.getName());
            }

            final TypeSpec.Builder builder = g.build(ic, processedBuilders);
            processedBuilders.put(g, builder);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private AbstractGenerator newGenerator(Class c) {
        try {
            return (AbstractGenerator) c.newInstance();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void printHeader() {
        if (!headerPrinted) {
            System.out.println("************************************************************************    ");
            System.out.println("*  _                  _          _              _               (v2.0) *    ");
            System.out.println("* |_) _  _    _|_ o _|_    |    |_) _  _ __    |_)    o  |  _| _  __   *    ");
            System.out.println("* |_)(/_(_||_| |_ |  | |_| |    |_)(/_(_|| |   |_)|_| |  | (_|(/_ |    *    ");
            System.out.println("*                                                                      *    ");
            System.out.println("************************************************************************    ");
            headerPrinted = true;
        }
    }

    private void printBeanStatus(TypeElement te, AbstractGenerator g) {
        System.out.println("* Making it beautiful - " + te.getQualifiedName() + " " + g.getAnnotationClass().getName());
    }


    private void write(InfoClass ic, TypeSpec.Builder classBuilder) throws IOException {
        if (classBuilder != null) {
            JavaFile javaFile = JavaFile.builder(ic.pkg, classBuilder.build()).build();
            javaFile.writeTo(processingEnv.getFiler());
        }
    }

    private void checkBBBUsage(TypeElement te) {
        if (!endsWith(te.getSimpleName(), "Def")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Must end with Def", te);
        }
    }


}