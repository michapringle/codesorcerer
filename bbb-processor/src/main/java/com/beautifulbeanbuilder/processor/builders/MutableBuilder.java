package com.beautifulbeanbuilder.processor.builders;

import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.jvm.Code;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.stream.Collectors;

public class MutableBuilder extends AbstractBuilder {

    public TypeSpec.Builder buildMutableBean(InfoClass ic) throws IOException {
        final TypeSpec.Builder classBuilder = buildClass(ic.typeMutable);
        addSerialVersionUUID(classBuilder);

        //Members
        ic.infos.forEach(i -> {
            final FieldSpec f = i.buildField(Modifier.PRIVATE);
            classBuilder.addField(f);
        });

        //Getters
        ic.infos.forEach(i -> {
            final MethodSpec getter = i.buildGetter();
            classBuilder.addMethod(getter);
        });

        //Setters
        ic.infos.forEach(i -> {
            final MethodSpec setter = i.buildSetter();
            classBuilder.addMethod(setter);
        });

        addFromImmutable(ic, classBuilder);
        addToImmutable(ic, classBuilder);

        return classBuilder;
    }



    private void addFromImmutable(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fromImmutable");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.addParameter(ic.typeImmutable, "imm");
        builder.returns(ic.typeMutable);


        CodeBlock.Builder cb = CodeBlock.builder();
        cb.addStatement("$T x = new $T()", ic.typeMutable, ic.typeMutable);
        ic.infos.forEach(i -> cb.addStatement("x.set" + i.nameUpper + "(imm." + i.prefix + i.nameUpper+ "())"));
        cb.addStatement("return x");

        builder.addCode(cb.build());

        classBuilder.addMethod(builder.build());
    }

    private void addToImmutable(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder toMutable1 = MethodSpec.methodBuilder("toImmutable");
        toMutable1.addModifiers(Modifier.PUBLIC);
        toMutable1.returns(ic.typeImmutable);

        CodeBlock.Builder cb1 = CodeBlock.builder();
        cb1.add("return $T.build" + ic.immutableClassName + "()", ic.typeImmutable);
        ic.nonNullInfos.forEach(i -> cb1.add("." + i.nameMangled + "(" + i.nameMangled + ")"));
        ic.nullableInfos.forEach(i -> cb1.add("." + i.nameMangled + "(" + i.nameMangled + ")"));
        cb1.add(".build();");
        toMutable1.addCode(cb1.build());
        classBuilder.addMethod(toMutable1.build());

//
//        MethodSpec.Builder toMutable = MethodSpec.methodBuilder("toMutable");
//        toMutable.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
//        toMutable.returns(ic.typeMutable);
//        toMutable.addParameter(ic.typeImmutable, "immutable");
//
//        CodeBlock.Builder cb = CodeBlock.builder();
//        cb.addStatement("$T mutable = new $T()", ic.typeMutable, ic.typeMutable);
//        cb.beginControlFlow("if(immutable != null)");
//        ic.infos.forEach(i -> cb.addStatement("mutable.set" + i.nameUpper + "(immutable." + i.prefix + i.nameUpper + "())"));
//        cb.endControlFlow();
//        cb.addStatement("return mutable");
//
//        toMutable.addCode(cb.build());
//        classBuilder.addMethod(toMutable.build());

    }
}
