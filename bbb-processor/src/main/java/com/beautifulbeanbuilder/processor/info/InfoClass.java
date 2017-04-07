package com.beautifulbeanbuilder.processor.info;

import com.beautifulbeanbuilder.generators.Types;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class InfoClass {
    public TypeElement typeElement;

    public String pkg;
    public String immutableClassName;
    public boolean isInterfaceDef;


    public List<Info> infos;
    public List<Info> nonNullInfos;
    public List<Info> nullableInfos;


    public ClassName typeDef;
    public ClassName typeImmutable;

    public ParameterizedTypeName typeCallbackImpl;

    public String listAllUsageParametrs() {
        List<String> listOfParameteNames = infos.stream().map(i -> i.nameMangled).collect(toList());
        return Joiner.on(", ").join(listOfParameteNames);
    }

    public TypeVariableName lastGeneric() {
        if (nonNullInfos.size() == 0) {
            return Types.jpT;
        }
        return TypeVariableName.get("T" + nonNullInfos.size());
    }

    public List<TypeVariableName> genericsT1T2T3() {
        if (nonNullInfos.size() == 0) {
            return Collections.singletonList(Types.jpT);
        }
        return IntStream.range(0, nonNullInfos.size())
                .mapToObj(i -> TypeVariableName.get("T" + (i + 1)))
                .collect(Collectors.toList());
    }

    public List<TypeVariableName> genericsTTT() {
        if (nonNullInfos.size() == 0) {
            return Collections.singletonList(Types.jpT);
        }
        return IntStream.range(1, nonNullInfos.size())
                .mapToObj(i -> Types.jpT)
                .collect(Collectors.toList());
    }
}
