package com.beautifulbeanbuilder.generators;

import com.beautifulbeanbuilder.BBBGuava;
import com.beautifulbeanbuilder.BBBImmutable;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GuavaGenerator extends AbstractGenerator<BBBGuava> {

    @Override
    public List<Class<? extends Annotation>> requires() {
        return Collections.singletonList(BBBImmutable.class);
    }

    public TypeSpec.Builder build(InfoClass ic, Map<AbstractGenerator, TypeSpec.Builder> generatorBuilderMap) throws IOException {
        ClassName typeGuava = ClassName.get(ic.pkg, ic.immutableClassName + "Guava");

        TypeSpec.Builder classBuilder = buildClass(typeGuava);

        ParameterizedTypeName typePredicateImmutable = ParameterizedTypeName.get(Types.guava_predicate, ic.typeImmutable);
        ParameterizedTypeName typeOrderingImmutable = ParameterizedTypeName.get(Types.guava_ordering, ic.typeImmutable);

        //Privte constructor
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
        constructorBuilder.addModifiers(Modifier.PRIVATE);
        classBuilder.addMethod(constructorBuilder.build());


        //TO_  BY_
        ic.infos.forEach(i -> {
            TypeName nReturnType = i.nReturnType.isPrimitive() ? i.nReturnType.box() : i.nReturnType;
            ParameterizedTypeName ret = ParameterizedTypeName.get(Types.guava_function, ic.typeImmutable, nReturnType);

            FieldSpec.Builder f1 = FieldSpec.builder(ret, "TO_" + i.nameAllUpper);
            f1.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            f1.initializer("(x) -> x == null ? null : x." + i.prefix + i.nameUpper + "()");
            classBuilder.addField(f1.build());

            FieldSpec.Builder f2 = FieldSpec.builder(ret, "BY_" + i.nameAllUpper);
            f2.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            f2.initializer("TO_" + i.nameAllUpper);
            classBuilder.addField(f2.build());
        });


        //Equivilance
        ic.infos.forEach(i -> {
            ParameterizedTypeName eqImpl = ParameterizedTypeName.get(Types.guava_equivilance, ic.typeImmutable);
            FieldSpec.Builder f = FieldSpec.builder(eqImpl, "EQUALS_" + i.nameAllUpper);
            f.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            f.initializer("$T.equals().onResultOf(TO_" + i.nameAllUpper + ")", Types.guava_equivilance);
            classBuilder.addField(f.build());
        });


        //Equivilance Wrapper
        ic.infos.forEach(i -> {
            ParameterizedTypeName eqWrapperImpl = ParameterizedTypeName.get(Types.guava_equivilanceWrapper, ic.typeImmutable);
            ParameterizedTypeName ret = ParameterizedTypeName.get(Types.guava_function, ic.typeImmutable, eqWrapperImpl);
            FieldSpec.Builder f = FieldSpec.builder(ret, "EQUALS_" + i.nameAllUpper + "_WRAPPER");
            f.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            f.initializer("(x) -> EQUALS_" + i.nameAllUpper + ".wrap(x)");
            classBuilder.addField(f.build());
        });

        //Equivilance Unwrapper
        {
            ParameterizedTypeName eqWrapperImpl = ParameterizedTypeName.get(Types.guava_equivilanceWrapper, ic.typeImmutable);
            ParameterizedTypeName ret = ParameterizedTypeName.get(Types.guava_function, eqWrapperImpl, ic.typeImmutable);
            FieldSpec.Builder f = FieldSpec.builder(ret, "EQUALS_UNWRAPPER");
            f.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            f.initializer("(x) -> x.get()");
            classBuilder.addField(f.build());
        }

        //Predicate
        ic.infos.stream().filter(i -> !i.nReturnType.isPrimitive() || !(i.nReturnType instanceof ArrayTypeName)).forEach(i -> {

            MethodSpec.Builder m = MethodSpec.methodBuilder("by" + i.nameUpper);
            m.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            m.addParameter(i.nReturnType, i.nameMangled);
            m.returns(typePredicateImmutable);
            m.addStatement("return (x) -> {return x == null ? false : $T.equals(x." + i.prefix + i.nameUpper + "(), " + i.nameMangled + ");}", ClassName.get(Objects.class));
            classBuilder.addMethod(m.build());
        });

        //Boolean
        ic.infos.stream().filter(i -> i.nReturnType.equals(ClassName.get(Boolean.class))).forEach(i -> {
            FieldSpec.Builder f = FieldSpec.builder(typePredicateImmutable, "IS_" + i.nameAllUpper);
            f.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            f.initializer("(x) -> {return ((x == null) ? false : (x." + i.prefix + i.nameUpper + "() == null ? false : x." + i.prefix + i.nameUpper + "()));}");
            classBuilder.addField(f.build());
        });

        //Primitive Boolean
        ic.infos.stream().filter(i -> i.nReturnType.equals(TypeName.BOOLEAN)).forEach(i -> {
            FieldSpec.Builder f = FieldSpec.builder(typePredicateImmutable, "IS_" + i.nameAllUpper);
            f.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
            f.initializer("(x) -> {return x." + i.prefix + i.nameUpper + "();}");
            classBuilder.addField(f.build());
        });


        //Orderings
        ic.infos.stream().filter(i -> i.isComparable).forEach(i -> {
            FieldSpec.Builder f = FieldSpec.builder(typeOrderingImmutable, "ORDER_BY_" + i.nameAllUpper);
            f.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);

            CodeBlock.Builder cb = CodeBlock.builder();
            cb.add("if(l == null && r != null) return 1;");
            cb.add("if(l != null && r == null) return -1;");
            cb.add("if(l == null && r == null) return 0;");
            cb.add("return $T.start()", ClassName.get(ComparisonChain.class));
            cb.add(".compare(l." + i.prefix + i.nameUpper + "(), r." + i.prefix + i.nameUpper + "(), $T.natural().nullsFirst())", ClassName.get(Ordering.class));
            cb.add(".result();");

            TypeSpec callback = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(typeOrderingImmutable)
                    .addMethod(MethodSpec.methodBuilder("compare")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(ic.typeImmutable, "l")
                            .addParameter(ic.typeImmutable, "r")
                            .returns(int.class)
                            .addCode(cb.build())
                            .build())
                    .build();

            f.initializer("$L", callback);
            classBuilder.addField(f.build());
        });

        return classBuilder;
    }

}
