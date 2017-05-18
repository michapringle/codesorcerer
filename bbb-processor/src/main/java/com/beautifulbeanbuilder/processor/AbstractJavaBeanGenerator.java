package com.beautifulbeanbuilder.processor;

import com.beautifulbeanbuilder.generators.beandef.BeanDefInfo;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.lang.annotation.Annotation;

public abstract class AbstractJavaBeanGenerator<T extends Annotation> extends AbstractJavaGenerator<T, BeanDefInfo> {

    public void write(BeanDefInfo ic, TypeSpec.Builder objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        if (objectToWrite != null) {
            JavaFile javaFile = JavaFile.builder(ic.pkg, objectToWrite.build()).build();
            System.out.println("Writing out object " + javaFile.packageName + "." + javaFile.typeSpec.name);
            javaFile.writeTo(processingEnv.getFiler());
        }
    }
}