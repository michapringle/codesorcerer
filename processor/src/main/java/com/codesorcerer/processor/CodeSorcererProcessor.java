package com.codesorcerer.processor;

import com.codesorcerer.LambdaExceptionUtils;
import com.codesorcerer.abstracts.AbstractInputBuilder;
import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.google.common.collect.*;
import com.google.common.reflect.ClassPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;


@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CodeSorcererProcessor extends javax.annotation.processing.AbstractProcessor {

    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;


    private static boolean headerPrinted = false;
    private static Multimap<String, AbstractSpell> allGeneratorsByAnnotationClass = HashMultimap.create(); //AnnotationClassName to processor
    private static Map<Class, AbstractInputBuilder> allInputBuilders = Maps.newHashMap(); //InputClass to inputBuilder
    private static Set<TypeElement> processedTypes = Sets.newHashSet();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of("*");
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();

        try {
            if (!headerPrinted) {
                headerPrinted = true;

                printHeader();

                ClassPath.from(getClass().getClassLoader()).getAllClasses().stream()
                        .filter(c -> c.getSimpleName().endsWith("Spell"))
                        .map(this::safeLoad)
                        .filter(AbstractSpell.class::isAssignableFrom)
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .map(this::newGenerator)
                        .forEach(g -> allGeneratorsByAnnotationClass.put(g.getAnnotationClass().getName(), g));
                ClassPath.from(getClass().getClassLoader()).getAllClasses().stream()
                        .filter(c -> c.getSimpleName().endsWith("InputBuilder"))
                        .map(this::safeLoad)
                        .filter(AbstractInputBuilder.class::isAssignableFrom)
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .map(this::newInputBuilder)
                        .forEach(ib -> allInputBuilders.put(ib.getInputClass(), ib));

                System.out.println("Inputs:");
                for (Map.Entry<Class, AbstractInputBuilder> b : allInputBuilders.entrySet()) {
                    System.out.println("  " + b.getValue().getClass().getSimpleName() + " builds " + b.getKey().getSimpleName());
                }
                System.out.println("Spells:");
                for (Map.Entry<String, AbstractSpell> b : allGeneratorsByAnnotationClass.entries()) {
                    System.out.println("  @" + StringUtils.substringAfterLast(b.getKey(), ".") + " casts " + b.getValue().getClass().getSimpleName());
                }
                System.out.println("Castings:");
            }

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
        final Set<TypeElement> types = elementsThatNeedProcessing(annotations, roundEnv);

        //Calculate the set of types that need processing
        final Set<TypeElement> notYetProcessed = Sets.newHashSet();
        for (TypeElement te : types) {
            if (processedTypes.add(te)) {
                notYetProcessed.add(te);
            }
        }

        //Run the generators for this type
        runFunction(notYetProcessed, (result) -> System.out.println("  Casting " + result.spell.getClass().getSimpleName() + " on " + result.te.getQualifiedName() ), "Debug");

//        runFunction(notYetProcessed, (result) -> {
//            result.spell.build(result);
//            result.spell.modify(result, results.values());
//            result.spell.write(result);
//        }, "All");

        runFunction(notYetProcessed, (result) -> result.spell.build(result), "Build");
        runFunction(notYetProcessed, (result) -> result.spell.modify(result, results.values()), "Modify");
        runFunction(notYetProcessed, (result) -> result.spell.write(result), "Write");
        runFunction(notYetProcessed, (result) -> result.spell.processingOver(results.values()), "Over");


        //Print Summary
        if(roundEnv.processingOver()) {
            Long outputCount = results.values()
                    .stream()
                    .filter(r -> r.output != null)
                    .collect(Collectors.counting());

            System.out.println("Conjured " + outputCount + " code files!");
        }

        return false;

    }

    private final ConcurrentMap<Pair<TypeElement, AbstractSpell>, Result> results = Maps.newConcurrentMap();

    private void runFunction(Collection<TypeElement> types, LambdaExceptionUtils.Consumer_WithExceptions<Result> f, String phase) {

        Multimap<AbstractSpell, TypeElement> runOrder = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());

        for (TypeElement te : types) {
            for (String ann : AbstractSpell.getAllAnnotations(te)) {
                for (AbstractSpell g : allGeneratorsByAnnotationClass.get(ann)) {
                    runOrder.put(g, te);
                }
            }
        }

        for (Map.Entry<AbstractSpell, TypeElement> e : runOrder.entries()) {
            try {
                Result result = getResult(e.getValue(), e.getKey());
                fillSpell(result.spell);
                f.accept(result);
            } catch (Exception ex) {
                messager.printMessage(Diagnostic.Kind.ERROR, trimToEmpty(ex.getMessage()), e.getValue());
                ex.printStackTrace();
            }

        }

    }

    private Result getResult(TypeElement te, AbstractSpell g) {
        return results.computeIfAbsent(Pair.of(te, g), p -> {
            AbstractInputBuilder inputBuilder = allInputBuilders.get(g.getInputClass());
            fillInputBuilder(inputBuilder);
            Object input = inputBuilder.buildInput(te);

            Result r = new Result();
            r.te = p.getLeft();
            r.spell = p.getRight();
            r.input = input;
            return r;
        });
    }

    private AbstractSpell newGenerator(Class c) {
        try {
            return (AbstractSpell) c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    private void fillSpell(AbstractSpell abstractSpell) {
        abstractSpell.elementUtils = elementUtils;
        abstractSpell.filer = filer;
        abstractSpell.messager = messager;
        abstractSpell.processingEnvironment = processingEnv;
        abstractSpell.typeUtils = typeUtils;
    }

    private AbstractInputBuilder newInputBuilder(Class c) {
        try {
            return (AbstractInputBuilder) c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    private void fillInputBuilder(AbstractInputBuilder abstractInputBuilder) {
        abstractInputBuilder.elementUtils = elementUtils;
        abstractInputBuilder.filer = filer;
        abstractInputBuilder.messager = messager;
        abstractInputBuilder.processingEnvironment = processingEnv;
        abstractInputBuilder.typeUtils = typeUtils;
    }


    private Set<TypeElement> elementsThatNeedProcessing(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<TypeElement> elementThatNeedProcessing = Sets.newHashSet();

        annotations.forEach(a -> {
            Set e = roundEnv.getElementsAnnotatedWith(a);
            elementThatNeedProcessing.addAll(e);
        });


        final Set<TypeElement> interstingElementsThatNeedProcessing = ElementFilter.typesIn(elementThatNeedProcessing)
                .stream()
                .filter(te -> te.getKind() == ElementKind.CLASS || te.getKind() == ElementKind.INTERFACE || te.getKind() == ElementKind.PACKAGE || te.getKind() == ElementKind.ENUM)
                .collect(Collectors.toSet());

        return interstingElementsThatNeedProcessing;
    }



    private void printHeader() {
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("                   ,%%%,                                                               ");
        System.out.println("                 ,%%%` %==--                                                               ");
        System.out.println("                ,%%`( '|              ,-.       .        ,-.                                ");
        System.out.println("               ,%%@ /\\_/            /          |       (   `\\                             ");
        System.out.println("     ,%.-\"\"\"-- % %%\"@@__            |    ,-. ,-| ,-.    `-.  ,-. ;-. ,-. ,-. ;-. ,-. ;-. ");
        System.out.println("    %%/             |__`\\           \\    | | | | |-'   .   ) | | |   |   |-' |   |-' |     ");
        System.out.println("   .%'\\     |   \\   /  //            `-' `-' `-' `-'    `-'  `-' '   `-' `-' '   `-' '     ");
        System.out.println("   ,%' >   .'----\\ |  [/                                                               ");
        System.out.println("      < <<`       ||                                                               ");
        System.out.println("       `\\\\\\       ||                                                               ");
        System.out.println("         )\\\\      )\\                                                               ");
        System.out.println("^^^^^^^^^\"\"\"^^^^^^\"\"^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

}
