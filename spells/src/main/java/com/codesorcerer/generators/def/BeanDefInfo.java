package com.codesorcerer.generators.def;

import com.codesorcerer.generators.def.spells.Types;
import com.google.common.base.Joiner;
import com.squareup.javapoet.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class BeanDefInfo {
    public TypeElement typeElement;

    public String pkg;
    public String immutableClassName;
    public boolean isInterfaceDef;


    public List<BeanDefFieldInfo> beanDefFieldInfos;
    public List<BeanDefFieldInfo> nonNullBeanDefFieldInfos;
    public List<BeanDefFieldInfo> nullableBeanDefFieldInfos;


    public ClassName typeDef;
    public ClassName typeImmutable;

    public ParameterizedTypeName typeCallbackImpl;

    public String listAllUsageParametrs() {
        List<String> listOfParameteNames = beanDefFieldInfos.stream().map(i -> i.nameMangled).collect(toList());
        return Joiner.on(", ").join(listOfParameteNames);
    }

    public TypeVariableName lastGeneric() {
        if (nonNullBeanDefFieldInfos.size() == 0) {
            return Types.jpT;
        }
        return TypeVariableName.get("T" + nonNullBeanDefFieldInfos.size());
    }

    public List<TypeVariableName> genericsT1T2T3() {
        if (nonNullBeanDefFieldInfos.size() == 0) {
            return Collections.singletonList(Types.jpT);
        }
        return IntStream.range(0, nonNullBeanDefFieldInfos.size())
                .mapToObj(i -> TypeVariableName.get("T" + (i + 1)))
                .collect(Collectors.toList());
    }

    public List<TypeVariableName> genericsTTT() {
        if (nonNullBeanDefFieldInfos.size() == 0) {
            return Collections.singletonList(Types.jpT);
        }
        return IntStream.range(1, nonNullBeanDefFieldInfos.size())
                .mapToObj(i -> Types.jpT)
                .collect(Collectors.toList());
    }


    public static class BeanDefFieldInfo {

        public ExecutableElement getter;
        public String prefix;       // 'get' or 'is'
        public String nameUpper;    // 'Thing'
        public String nameAllUpper; // 'THING'
        public String name;         // 'thing'
        public String nameMangled;  // same as name, but with KEYWORDS fixed

        public Boolean isNonNull;     // annotated with @Nonnull
        public Boolean isComparable;  //Extends from Comparable

        public TypeName nReturnType; // the return type

        public String returnType;


        public MethodSpec buildGetter() {
            MethodSpec.Builder m = MethodSpec.methodBuilder(prefix + nameUpper);
            m.addModifiers(Modifier.PUBLIC);
            m.returns(nReturnType);
            m.addAnnotation(CheckReturnValue.class);
            if (isNonNull) {
                m.addAnnotation(Nonnull.class);
            }
            m.addStatement("return " + nameMangled);
            return m.build();
        }

        public MethodSpec buildSetter() {
            MethodSpec.Builder m = MethodSpec.methodBuilder("set" + nameUpper);
            m.addModifiers(Modifier.PUBLIC);
            m.addParameter(buildParameter());
            m.returns(void.class);
            m.addStatement("this." + nameMangled + " = " + nameMangled);
            return m.build();
        }

        public ParameterSpec buildParameter() {
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(nReturnType, nameMangled);
            if (isNonNull) {
                parameterBuilder.addAnnotation(Nonnull.class);
            }
            return parameterBuilder.build();
        }

        public FieldSpec buildField(Modifier... modifiers) {
            FieldSpec.Builder builder = FieldSpec.builder(nReturnType, nameMangled);
            builder.addModifiers(modifiers);
            return builder.build();
        }


    }

}
