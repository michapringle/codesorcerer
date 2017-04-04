package com.beautifulbeanbuilder.processor.builders;

import com.squareup.javapoet.*;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.io.Serializable;

public class AbstractBuilder {

    protected TypeSpec.Builder buildClass(ClassName c) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(c);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        addGeneratedAnnotation(classBuilder);
        addSuppressWarningsAnnotation(classBuilder);
        return classBuilder;
    }

    protected void addSerialVersionUUID(TypeSpec.Builder classBuilder) {
        FieldSpec.Builder builder = FieldSpec.builder(Long.class, "serialVersionUID");
        builder.addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC);
        builder.initializer("$LL", 1L);
        classBuilder.addField(builder.build());

        classBuilder.addSuperinterface(Serializable.class);
    }


    protected void addSuppressWarningsAnnotation(TypeSpec.Builder classBuilder) {
        classBuilder.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", CodeBlock.of("$S", "all"))
                .build());
    }

    protected void addGeneratedAnnotation(TypeSpec.Builder classBuilder) {
        classBuilder.addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", "BeautifulBeanBuilder"))
                .build());
    }


    protected MethodSpec.Builder startHashcode() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("hashCode");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(TypeName.INT);
        builder.addAnnotation(Override.class);
        return builder;
    }

    protected MethodSpec.Builder startToString() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toString");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(String.class);
        builder.addAnnotation(Override.class);
        return builder;
    }

    protected MethodSpec.Builder startEquals() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("equals");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(TypeName.BOOLEAN);
        builder.addAnnotation(Override.class);
        builder.addParameter(Object.class, "o");
        return builder;
    }
}