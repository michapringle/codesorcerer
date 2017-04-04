package com.beautifulbeanbuilder.processor.info;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;

public class Info {

    public String prefix;       // 'get' or 'is'
    public String nameUpper;    // 'Thing'
    public String nameAllUpper; // 'THING'
    public String name;         // 'thing'
    public String nameMangled;  // same as name, but with KEYWORDS fixed

    public Boolean isNonNull;     // annotated with @Nonnull
    public Boolean isBB;        //Is a BBB
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
