package com.beautifulbeanbuilder.processor;

import com.beautifulbeanbuilder.BeautifulBean;
import com.beautifulbeanbuilder.processor.builders.GuavaBuilder;
import com.beautifulbeanbuilder.processor.builders.ImmutableBuilder;
import com.beautifulbeanbuilder.processor.builders.JacksonBuilder;
import com.beautifulbeanbuilder.processor.builders.MutableBuilder;
import com.beautifulbeanbuilder.processor.info.InfoBuilder;
import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.endsWith;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.beautifulbeanbuilder.BeautifulBean")
public class BBBProcessor extends AbstractProcessor {

    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;

    private static boolean headerPrinted = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<TypeElement> types = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(BeautifulBean.class));
        for (Element te : types) {
            String currentTypeName = te.getSimpleName().toString();
            String currentTypePackage = elementUtils.getPackageOf(te).toString();
            process((TypeElement) te, currentTypeName, currentTypePackage, roundEnv);
        }

        return true;
    }


    public void process(TypeElement te, String currentTypeName, String currentTypePackage, RoundEnvironment roundEnvironment) {
        try {

            printHeader();
            printBeanStatus(te);

            checkBBBUsage(te);

            final InfoClass ic = new InfoBuilder().init(processingEnv, te, currentTypeName, currentTypePackage);

            TypeSpec.Builder i = new ImmutableBuilder().build(ic);
            TypeSpec.Builder m = new MutableBuilder().build(ic, i);
            TypeSpec.Builder g = new GuavaBuilder().build(ic, i);
            TypeSpec.Builder j = new JacksonBuilder().build(ic, i);

            write(ic, m);
            write(ic, i);
            write(ic, g);
            write(ic, j);

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
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

    private void printBeanStatus(TypeElement te) {
        System.out.println("* Making it beautiful - " + te.getQualifiedName());
    }


    private void write(InfoClass ic, TypeSpec.Builder classBuilder) throws IOException {
        JavaFile javaFile = JavaFile.builder(ic.pkg, classBuilder.build()).build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    private void checkBBBUsage(TypeElement te) {
        if (!endsWith(te.getSimpleName(), "Def")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Must end with Def", te);
        }
    }


}