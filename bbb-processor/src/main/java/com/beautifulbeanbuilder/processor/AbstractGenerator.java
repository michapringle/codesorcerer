package com.beautifulbeanbuilder.processor;

import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.*;

import javax.annotation.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractGenerator<T extends Annotation> {

    private TypeToken<T> type = new TypeToken<T>(getClass()) {
    };


    public List<Class<? extends Annotation>> requires() {
        return Collections.emptyList();
    }

    public abstract TypeSpec.Builder build(InfoClass ic, Map<AbstractGenerator, TypeSpec.Builder> generatorBuilderMap) throws IOException;

    public Class<T> getAnnotationClass() {
        return (Class<T>) type.getRawType();
    }



    protected TypeSpec.Builder getTypeBuilder(Class<? extends AbstractGenerator> generatorClass, Map<AbstractGenerator, TypeSpec.Builder> generatorBuilderMap) {
        for (Map.Entry<AbstractGenerator, TypeSpec.Builder> e : generatorBuilderMap.entrySet()) {
            if (e.getKey().getClass() == generatorClass) {
                return e.getValue();
            }
        }
        throw new RuntimeException("Cant find generatorClass of type " + generatorClass.getName());
    }


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