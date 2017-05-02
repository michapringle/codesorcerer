package com.beautifulbeanbuilder.generators.beandef;

import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

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
}
