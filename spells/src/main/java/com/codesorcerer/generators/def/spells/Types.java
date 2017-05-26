package com.codesorcerer.generators.def.spells;

import com.codesorcerer.Buildable;
import com.codesorcerer.Doneable;
import com.google.common.base.*;
import com.google.common.collect.Ordering;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.Objects;
import java.util.function.Consumer;

public final class Types {


    //Java
    public static final ClassName java_objects = ClassName.get(Objects.class);

    //Guava
    public static final ClassName guava_moreObjects = ClassName.get(MoreObjects.class);
    public static final ClassName guava_preconditions = ClassName.get(Preconditions.class);
    public static final ClassName guava_ordering = ClassName.get(Ordering.class);
    public static final ClassName guava_equivilance = ClassName.get(Equivalence.class);
    public static final ClassName guava_equivilanceWrapper = ClassName.get(Equivalence.Wrapper.class);
    public static final ClassName guava_predicate = ClassName.get(Predicate.class);
    public static final ClassName guava_function = ClassName.get(Function.class);

    //TypeParameters
    public static final TypeVariableName jpP = TypeVariableName.get("P");
    public static final TypeVariableName jpT = TypeVariableName.get("T");

    //Others...
    public static final ClassName jpDoneable = ClassName.get(Doneable.class);
    public static final ParameterizedTypeName jpDoneableP = ParameterizedTypeName.get(jpDoneable, jpP);

    public static final ClassName jpBuildable = ClassName.get(Buildable.class);

    public static final ClassName jpBeanRequires0 = ClassName.bestGuess("BeanRequires0");
    public static final ClassName jpSubBeanRequires0 = ClassName.bestGuess("SubBeanRequires0");
    public static final ParameterizedTypeName jpSubBeanRequires0P = ParameterizedTypeName.get(Types.jpSubBeanRequires0, Types.jpP);

    public static final ClassName jpBeanBuilder = ClassName.bestGuess("BeanBuilder");
    public static final ClassName jpSubBeanBuilder = ClassName.bestGuess("SubBeanBuilder");
    public static final ParameterizedTypeName jpSubBeanBuilderP = ParameterizedTypeName.get(jpSubBeanBuilder, jpP);
    public static final ClassName jpBeanUpdater = ClassName.bestGuess("BeanUpdater");
    public static final ClassName jpSubBeanUpdater = ClassName.bestGuess("SubBeanUpdater");
    public static final ClassName jpAbstract = ClassName.bestGuess("Abstract");
    public static final ClassName jpBeanUpdatable = ClassName.bestGuess("BeanUpdateable");
    public static final ClassName jpNonNullable = ClassName.bestGuess("NonNullable");
    public static final ClassName jpNullable = ClassName.bestGuess("Nullable");
    public static final ClassName jpSubBeanUpdatable = ClassName.bestGuess("SubBeanUpdatable");
    public static final ParameterizedTypeName jpSubBeanUpdatableP = ParameterizedTypeName.get(jpSubBeanUpdatable, jpP);
    public static final ClassName jpSubBeanBuildable = ClassName.bestGuess("SubBeanBuildable");
    public static final ParameterizedTypeName jpSubBeanBuildableP = ParameterizedTypeName.get(jpSubBeanBuildable, jpP);
    public static final ClassName jpBeanBuildable = ClassName.bestGuess("BeanBuildable");

    public static final ClassName jpCallback = ClassName.get(Consumer.class);


}
