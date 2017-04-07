package com.beautifulbeanbuilder.generators;

import com.beautifulbeanbuilder.BBBImmutable;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.BBBProcessor;
import com.beautifulbeanbuilder.processor.info.Info;
import com.beautifulbeanbuilder.processor.info.InfoBuilder;
import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.Lists;
import com.squareup.javapoet.*;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Iterables.toArray;

public class ImmutableGenerator extends AbstractGenerator<BBBImmutable> {


    private boolean isBBB(Info i) {
        final TypeMirror returnTypeMirror = i.getter.getReturnType();

        return !InfoBuilder.isPrimitive(returnTypeMirror) &&
                !InfoBuilder.isArray(returnTypeMirror) &&
                BBBProcessor.hasAnnotation(MoreTypes.asElement(returnTypeMirror), BBBImmutable.class);
    }

    public TypeSpec.Builder build(InfoClass ic, Map<AbstractGenerator, TypeSpec.Builder> generatorBuilderMap) throws IOException {
        TypeSpec.Builder classBuilder = buildClass(ic.typeImmutable);

        classBuilder.addAnnotation(Immutable.class);
        classBuilder.addAnnotation(ThreadSafe.class);

        addExtends(ic, classBuilder);
        addSerialVersionUUID(classBuilder);


        addNonNullableInterface(ic, classBuilder);
        addNullableInterface(ic, classBuilder);

        addBaseInterfaces(ic, classBuilder);

        addBeanRequiresInterfaces(ic, classBuilder);
        addUpdaterInternalInterface(ic, classBuilder);

        addAbstract(ic, classBuilder);
        addBaseClasses(ic, classBuilder);

        addBuilder(ic, classBuilder);
        addUpdater(ic, classBuilder);
        addMemberFields(ic.infos, classBuilder);
        addConstructor(ic, classBuilder);
        addGetters(ic.infos, classBuilder);
        addWiths(ic, classBuilder);
        addToString(ic, classBuilder);
        addHashcode(ic, classBuilder);
        addEquals(ic, classBuilder);
        addSimpleBuilders(ic, classBuilder);

        return classBuilder;
    }

    private void addAbstract(InfoClass ic, TypeSpec.Builder classBuilder) {
        TypeSpec.Builder a = TypeSpec.classBuilder(Types.jpAbstract);
        a.addModifiers(Modifier.PRIVATE, Modifier.ABSTRACT, Modifier.STATIC);
        a.addTypeVariable(Types.jpP);
        a.addTypeVariables(ic.genericsT1T2T3());

        //Fields
        a.addField(Types.jpP, "parent", Modifier.PRIVATE);
        a.addField(ic.typeCallbackImpl, "callback", Modifier.PRIVATE);
        ic.infos.forEach(i -> a.addField(i.buildField(Modifier.PRIVATE)));

        //Constructor
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
        constructor.addModifiers(Modifier.PUBLIC);
        constructor.addParameter(ic.typeImmutable, "x");
        constructor.addParameter(Types.jpP, "parent");
        constructor.addParameter(ic.typeCallbackImpl, "callback");
        constructor.addStatement("this.parent = parent");
        constructor.addStatement("this.callback = callback");

        CodeBlock.Builder cbConst = CodeBlock.builder();
        cbConst.beginControlFlow("if(x != null)");
        ic.infos.forEach(i -> cbConst.addStatement("this." + i.nameMangled + " = x." + i.prefix + i.nameUpper + "()"));
        cbConst.endControlFlow();
        constructor.addCode(cbConst.build());

        a.addMethod(constructor.build());

        //setters
        for (int x = 0; x < ic.nonNullInfos.size(); x++) {
            Info i = ic.nonNullInfos.get(x);
            addSetter(ic, i, a, TypeVariableName.get("T" + (x + 1)));
        }
        for (int x = 0; x < ic.nullableInfos.size(); x++) {
            Info i = ic.nullableInfos.get(x);
            addSetter(ic, i, a, ic.lastGeneric());
        }

        //getters
        ic.infos.stream().filter(i -> isBBB(i)).forEach(i -> {
            MethodSpec.Builder m = MethodSpec.methodBuilder("get" + i.nameUpper);
            m.addModifiers(Modifier.PUBLIC);
            m.returns(ParameterizedTypeName.get(ClassName.bestGuess(i.returnType + "." + Types.jpSubBeanUpdatable.simpleName()), ic.lastGeneric()));
            m.addStatement("return " + i.returnType + ".Internal.newSubBeanUpdater((" + ic.lastGeneric() + ")this, (v)->" + i.nameMangled + "=v, " + i.nameMangled + ")");
            a.addMethod(m.build());
        });

        //Build
        {
            MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build");
            buildMethod.addModifiers(Modifier.PUBLIC);
            buildMethod.returns(ic.typeImmutable);

            CodeBlock.Builder cb = CodeBlock.builder();
            cb.add("return new $T(", ic.typeImmutable);
            cb.indent();
            String params = ic.infos.stream()
                    .map(i -> i.nameMangled)
                    .collect(Collectors.joining(",\n"));
            cb.add(params);
            cb.add(");\n");
            cb.unindent();
            buildMethod.addCode(cb.build());

            a.addMethod(buildMethod.build());
        }

        //Done
        {
            MethodSpec.Builder doneMethod = MethodSpec.methodBuilder("done");
            doneMethod.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            doneMethod.returns(Types.jpP);
            doneMethod.addStatement("callback.accept(build())");
            doneMethod.addStatement("return parent");
            a.addMethod(doneMethod.build());
        }

//        //Callbacks
//        ic.infos.stream().filter(i -> isBBB(i)).forEach(i -> {
//            MethodSpec.Builder m = MethodSpec.methodBuilder("newCallback" + i.nameUpper);
//            m.addModifiers(Modifier.PRIVATE);
//            m.returns(ParameterizedTypeName.get(Types.jpCallback, i.nReturnType));
//            //m.addParameter(ic.typeMutable, "m");
//
//            TypeSpec callback = TypeSpec.anonymousClassBuilder("")
//                    .addSuperinterface(ParameterizedTypeName.get(Types.jpCallback, i.nReturnType))
//                    .addMethod(MethodSpec.methodBuilder("update")
//                            .addAnnotation(Override.class)
//                            .addModifiers(Modifier.PUBLIC)
//                            .addParameter(i.nReturnType, "val")
//                            .returns(void.class)
//                            .addStatement("" + i.nameMangled + " = val")
//                            .build())
//                    .build();
//
//            m.addStatement("return $L", callback);
//            a.addMethod(m.build());
//        });

        classBuilder.addType(a.build());
    }

    private void addSetter(InfoClass ic, Info i, TypeSpec.Builder a, TypeVariableName tn) {
        MethodSpec.Builder m = MethodSpec.methodBuilder(i.nameMangled);
        m.addModifiers(Modifier.PUBLIC);
        m.returns(tn);
        m.addParameter(i.nReturnType, i.nameMangled);
        if (i.isNonNull) {
            m.addStatement("$L.checkNotNull($N, \"$L.$L cannot be null\")", Types.guava_preconditions, i.nameMangled, ic.immutableClassName, i.nameMangled);
        }
        m.addStatement("this." + i.nameMangled + " = " + i.nameMangled);
        m.addStatement("return (" + tn.name + ")this");
        a.addMethod(m.build());

        if (isBBB(i)) {
            MethodSpec.Builder m2 = MethodSpec.methodBuilder("new" + i.nameUpper);
            m2.addModifiers(Modifier.PUBLIC);
            m2.returns(ParameterizedTypeName.get(ClassName.bestGuess(i.returnType + ".SubBeanRequires0"), tn));
            m2.addStatement("return " + i.returnType + ".Internal.newSubBeanBuilder((" + tn.name + ")this, (v)->" + i.nameMangled + "=v)");
            a.addMethod(m2.build());
        }
    }


    private void addBaseClasses(InfoClass ic, TypeSpec.Builder classBuilder) {
        {
            final List<TypeName> l = Lists.newArrayList();
            l.add(Types.jpBeanBuildable);
            l.addAll(fckedUpMethodThatGenertesStuff(ic, "BeanRequires", null, true, false, false));
            l.add(Types.jpBeanBuildable);

            TypeSpec.Builder if1 = TypeSpec.classBuilder(Types.jpBeanBuilder);
            if1.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
            if1.superclass(ParameterizedTypeName.get(Types.jpAbstract, toArray(l, TypeName.class)));
            if1.addSuperinterface(Types.jpBeanBuildable);
            if1.addSuperinterface(Types.jpBeanRequires0);
            if1.addSuperinterfaces(fckedUpMethodThatGenertesStuff(ic, "BeanRequires", null, true, false, false));

            MethodSpec.Builder m = MethodSpec.constructorBuilder();
            m.addModifiers(Modifier.PUBLIC);
            m.addStatement("super(null,null,null)");
            if1.addMethod(m.build());

            classBuilder.addType(if1.build());
        }

        {
            final List<TypeName> l = Lists.newArrayList();
            l.add(Types.jpP);
            l.addAll(fckedUpMethodThatGenertesStuff(ic, "SubBeanRequires", "P", true, false, false));
            l.add(Types.jpSubBeanBuildableP);

            TypeSpec.Builder if1 = TypeSpec.classBuilder(Types.jpSubBeanBuilder);
            if1.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
            if1.addTypeVariable(Types.jpP);
            if1.superclass(ParameterizedTypeName.get(Types.jpAbstract, toArray(l, TypeName.class)));

            if1.addSuperinterface(Types.jpSubBeanRequires0P);
            if1.addSuperinterface(Types.jpSubBeanBuildableP);
            if1.addSuperinterfaces(fckedUpMethodThatGenertesStuff(ic, "SubBeanRequires", "P", true, false, false));


            MethodSpec.Builder m = MethodSpec.constructorBuilder();
            m.addModifiers(Modifier.PUBLIC);
            m.addParameter(Types.jpP, "parent");
            m.addParameter(ic.typeCallbackImpl, "callback");
            m.addStatement("super(null,parent,callback)");
            if1.addMethod(m.build());

            classBuilder.addType(if1.build());
        }

        {
            final List<TypeName> l = Lists.newArrayList();
            l.add(Types.jpBeanUpdatable);
            l.addAll(fckedUpMethodThatGenertesStuff(ic, "BeanUpdateable", null, false, true, true));
            //l.add(Types.jpBeanBuildable);

            TypeSpec.Builder if1 = TypeSpec.classBuilder(Types.jpBeanUpdater);
            if1.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
            if1.superclass(ParameterizedTypeName.get(Types.jpAbstract, toArray(l, TypeName.class)));
            if1.addSuperinterface(Types.jpBeanUpdatable);

            MethodSpec.Builder m = MethodSpec.constructorBuilder();
            m.addModifiers(Modifier.PUBLIC);
            m.addParameter(ic.typeImmutable, "x");
            m.addStatement("super(x,null,null)");
            if1.addMethod(m.build());

            classBuilder.addType(if1.build());
        }


        {
            final List<TypeName> l = Lists.newArrayList();
            l.add(Types.jpP);
            l.add(Types.jpSubBeanUpdatableP);
            l.addAll(fckedUpMethodThatGenertesStuff(ic, Types.jpSubBeanUpdatable.simpleName(), "P", false, false, false));

            TypeSpec.Builder if1 = TypeSpec.classBuilder(Types.jpSubBeanUpdater);
            if1.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
            if1.addTypeVariable(Types.jpP);
            if1.superclass(ParameterizedTypeName.get(Types.jpAbstract, toArray(l, TypeName.class)));
            if1.addSuperinterface(Types.jpSubBeanUpdatableP);

            MethodSpec.Builder m = MethodSpec.constructorBuilder();
            m.addModifiers(Modifier.PUBLIC);
            m.addParameter(Types.jpP, "parent");
            m.addParameter(ic.typeCallbackImpl, "callback");
            m.addParameter(ic.typeImmutable, "x");
            m.addStatement("super(x,parent,callback)");
            if1.addMethod(m.build());

            classBuilder.addType(if1.build());
        }

    }

    private List<TypeName> fckedUpMethodThatGenertesStuff(InfoClass ic, String prefix, String typ, boolean includeCount, boolean minOne, boolean includeFirst) {
        if (ic.nonNullInfos.size() == 0 && minOne) {
            ClassName cn = ClassName.bestGuess(prefix);
            return typ == null ? Collections.singletonList(cn) : Collections.singletonList(ParameterizedTypeName.get(cn, TypeVariableName.get(typ)));
        }

        int start = includeFirst ? 0 : 1;
        return IntStream.range(start, ic.nonNullInfos.size())
                .mapToObj(i -> {
                    ClassName cn = ClassName.bestGuess(includeCount ? prefix + (i) : prefix);
                    return typ == null ? cn : ParameterizedTypeName.get(cn, TypeVariableName.get(typ));
                })
                .collect(Collectors.toList());
    }

    private void addBaseInterfaces(InfoClass ic, TypeSpec.Builder classBuilder) {
        TypeSpec.Builder if1 = TypeSpec.interfaceBuilder(Types.jpBeanBuildable);
        if1.addModifiers(Modifier.PUBLIC);
        if1.addSuperinterface(ParameterizedTypeName.get(Types.jpNullable, Types.jpBeanBuildable));
        if1.addSuperinterface(ParameterizedTypeName.get(Types.jpBuildable, ic.typeImmutable));
        classBuilder.addType(if1.build());

        TypeSpec.Builder if2 = TypeSpec.interfaceBuilder(Types.jpSubBeanBuildable);
        if2.addTypeVariable(Types.jpP);
        if2.addModifiers(Modifier.PUBLIC);
        if2.addSuperinterface(ParameterizedTypeName.get(Types.jpNullable, Types.jpSubBeanBuildableP));
        if2.addSuperinterface(Types.jpDoneableP);
        classBuilder.addType(if2.build());

        TypeSpec.Builder if3 = TypeSpec.interfaceBuilder(Types.jpBeanUpdatable);
        if3.addModifiers(Modifier.PUBLIC);
        if3.addSuperinterface(ParameterizedTypeName.get(Types.jpNullable, Types.jpBeanUpdatable));
        if3.addSuperinterface(ParameterizedTypeName.get(Types.jpNonNullable, Types.jpBeanUpdatable));
        if3.addSuperinterface(ParameterizedTypeName.get(Types.jpBuildable, ic.typeImmutable));
        classBuilder.addType(if3.build());

        TypeSpec.Builder if4 = TypeSpec.interfaceBuilder(Types.jpSubBeanUpdatable);
        if4.addTypeVariable(Types.jpP);
        if4.addModifiers(Modifier.PUBLIC);
        if4.addSuperinterface(ParameterizedTypeName.get(Types.jpNullable, Types.jpSubBeanUpdatableP));
        if4.addSuperinterface(ParameterizedTypeName.get(Types.jpNonNullable, Types.jpSubBeanUpdatableP));
        if4.addSuperinterface(Types.jpDoneableP);
        classBuilder.addType(if4.build());
    }

    private void addNonNullableInterface(InfoClass ic, TypeSpec.Builder classBuilder) {

        TypeSpec.Builder if1 = TypeSpec.interfaceBuilder(Types.jpNonNullable);
        if1.addModifiers(Modifier.PRIVATE);
        if1.addTypeVariable(Types.jpT);

        ic.infos.stream().filter(i -> isBBB(i)).forEach(i -> {
            MethodSpec.Builder m = MethodSpec.methodBuilder("get" + i.nameUpper);
            m.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            m.returns(ParameterizedTypeName.get(ClassName.bestGuess(i.returnType + "." + Types.jpSubBeanUpdatable.simpleName()), Types.jpT));
            if1.addMethod(m.build());
        });

        ic.nonNullInfos.stream().forEach(i -> {
            MethodSpec.Builder m = MethodSpec.methodBuilder(i.nameMangled);
            m.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            m.addParameter(i.nReturnType, i.nameMangled);
            m.returns(Types.jpT);
            if1.addMethod(m.build());
        });

        ic.nonNullInfos.stream().filter(i -> isBBB(i)).forEach(i -> {
            MethodSpec.Builder m = MethodSpec.methodBuilder("new" + i.nameUpper);
            m.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            m.returns(ParameterizedTypeName.get(ClassName.bestGuess(i.returnType + ".SubBeanRequires0"), Types.jpT));
            if1.addMethod(m.build());
        });

        classBuilder.addType(if1.build());
    }

    private void addNullableInterface(InfoClass ic, TypeSpec.Builder classBuilder) {

        TypeSpec.Builder if1 = TypeSpec.interfaceBuilder(Types.jpNullable);
        if1.addModifiers(Modifier.PRIVATE);
        if1.addTypeVariable(Types.jpT);

        ic.nullableInfos.forEach(i -> {
            MethodSpec.Builder m = MethodSpec.methodBuilder(i.nameMangled);
            m.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            m.addParameter(i.nReturnType, i.nameMangled);
            m.returns(Types.jpT);
            if1.addMethod(m.build());
        });

        ic.nullableInfos.stream().filter(i -> isBBB(i)).forEach(i -> {
            MethodSpec.Builder m = MethodSpec.methodBuilder("new" + i.nameUpper);
            m.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            m.returns(ParameterizedTypeName.get(ClassName.bestGuess(i.returnType + ".SubBeanRequires0"), Types.jpT));
            if1.addMethod(m.build());
        });

        classBuilder.addType(if1.build());
    }


    private void addBeanRequiresInterfaces(InfoClass ic, TypeSpec.Builder classBuilder) {


        if (ic.nonNullInfos.isEmpty()) {

            TypeSpec.Builder if1 = TypeSpec.interfaceBuilder(Types.jpBeanRequires0);
            if1.addSuperinterface(Types.jpBeanBuildable);
            if1.addModifiers(Modifier.PUBLIC);
            classBuilder.addType(if1.build());

            TypeSpec.Builder if2 = TypeSpec.interfaceBuilder(Types.jpSubBeanRequires0);
            if2.addSuperinterface(Types.jpSubBeanBuildableP);
            if2.addTypeVariable(Types.jpP);
            if2.addModifiers(Modifier.PUBLIC);
            classBuilder.addType(if2.build());
        }

        for (int x = 0; x < ic.nonNullInfos.size(); x++) {
            Info a = ic.nonNullInfos.get(x);
            ClassName classNameSubBeanRequires0 = ClassName.bestGuess(a.returnType + "." + "SubBeanRequires0");

            TypeSpec.Builder if1 = TypeSpec.interfaceBuilder("BeanRequires" + x);
            if1.addModifiers(Modifier.PUBLIC);

            TypeSpec.Builder if2 = TypeSpec.interfaceBuilder("SubBeanRequires" + x);
            if2.addModifiers(Modifier.PUBLIC);
            if2.addTypeVariable(Types.jpP);

            MethodSpec.Builder m1 = MethodSpec.methodBuilder(a.nameMangled);
            m1.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
            m1.addParameter(a.nReturnType, a.nameMangled);
            MethodSpec.Builder m2 = MethodSpec.methodBuilder("new" + a.nameUpper);
            m2.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
            //m2.addParameter(a.nReturnType, a.nameMangled);
            MethodSpec.Builder m3 = MethodSpec.methodBuilder(a.nameMangled);
            m3.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
            m3.addParameter(a.nReturnType, a.nameMangled);
            MethodSpec.Builder m4 = MethodSpec.methodBuilder("new" + a.nameUpper);
            m4.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
            //m4.addParameter(a.nReturnType, a.nameMangled);

            if (x < ic.nonNullInfos.size() - 1) {
                int xx = x + 1;

                ClassName classNameBeanRequiresXX = ClassName.bestGuess("BeanRequires" + xx);
                ClassName classNameSubBeanRequiresXX = ClassName.bestGuess("SubBeanRequires" + xx);

                //Not last
                m1.returns(classNameBeanRequiresXX);
                m2.returns(ParameterizedTypeName.get(classNameSubBeanRequires0, classNameBeanRequiresXX));
                m3.returns(ParameterizedTypeName.get(classNameSubBeanRequiresXX, Types.jpP));
                m4.returns(ParameterizedTypeName.get(classNameSubBeanRequires0, classNameSubBeanRequiresXX));

            } else {
                //Last
                m1.returns(Types.jpBeanBuildable);
                m2.returns(ParameterizedTypeName.get(classNameSubBeanRequires0, Types.jpBeanBuildable));
                m3.returns(Types.jpSubBeanBuildableP);
                m4.returns(ParameterizedTypeName.get(classNameSubBeanRequires0, Types.jpSubBeanBuildableP));
            }

            if1.addMethod(m1.build());
            if2.addMethod(m3.build());

            if (isBBB(a)) {
                if1.addMethod(m2.build());
                if2.addMethod(m4.build());
            }

            classBuilder.addType(if1.build());
            classBuilder.addType(if2.build());
        }
    }


    private void addUpdaterInternalInterface(InfoClass ic, TypeSpec.Builder classBuilder) {

        TypeSpec.Builder innerClassBuilder = TypeSpec.classBuilder("Internal");
        innerClassBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);


        MethodSpec.Builder subBeanBuilder = MethodSpec.methodBuilder("newSubBeanBuilder");
        subBeanBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        subBeanBuilder.addTypeVariable(Types.jpP);
        subBeanBuilder.addParameter(Types.jpP, "parent");
        subBeanBuilder.addParameter(ic.typeCallbackImpl, "c");
        subBeanBuilder.returns(Types.jpSubBeanRequires0P);
        subBeanBuilder.addStatement("return new SubBeanBuilder(parent, c)");
        innerClassBuilder.addMethod(subBeanBuilder.build());

        MethodSpec.Builder subBeanUpdater = MethodSpec.methodBuilder("newSubBeanUpdater");
        subBeanUpdater.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        subBeanUpdater.addTypeVariable(Types.jpP);
        subBeanUpdater.addParameter(Types.jpP, "parent");
        subBeanUpdater.addParameter(ic.typeCallbackImpl, "c");
        subBeanUpdater.addParameter(ic.typeImmutable, "x");
        subBeanUpdater.returns(Types.jpSubBeanUpdatableP);
        subBeanUpdater.addStatement("return new $T(parent, c, x)", Types.jpSubBeanUpdater);
        innerClassBuilder.addMethod(subBeanUpdater.build());


        classBuilder.addType(innerClassBuilder.build());
    }


    private void addBuilder(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("build" + ic.immutableClassName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(Types.jpBeanRequires0);
        builder.addAnnotation(Nonnull.class);
        builder.addAnnotation(CheckReturnValue.class);
        builder.addStatement("return new $T()", Types.jpBeanBuilder);
        classBuilder.addMethod(builder.build());
    }

    private void addUpdater(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(Types.jpBeanUpdatable);
        builder.addAnnotation(Nonnull.class);
        builder.addAnnotation(CheckReturnValue.class);
        builder.addStatement("return new $T(this)", Types.jpBeanUpdater);
        classBuilder.addMethod(builder.build());
    }

    private void addSimpleBuilders(InfoClass ic, TypeSpec.Builder classBuilder) {
        if (ic.infos.size() > 3) {
            return;
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("new" + ic.immutableClassName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(ic.typeImmutable);
        builder.addAnnotation(Nonnull.class);
        builder.addAnnotation(CheckReturnValue.class);
        ic.infos.forEach(i -> builder.addParameter(i.buildParameter()));
        builder.addStatement("return new $T(" + ic.listAllUsageParametrs() + ")", ic.typeImmutable);
        classBuilder.addMethod(builder.build());
    }


    private void addEquals(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = startEquals();

        builder.beginControlFlow("if (this == o)");
        builder.addStatement("return true");
        builder.endControlFlow();

        builder.beginControlFlow("if ( !( o instanceof $T))", ic.typeImmutable);
        builder.addStatement("return false");
        builder.endControlFlow();

        builder.addStatement("final $T that = ($T)o", ic.typeImmutable, ic.typeImmutable);


        CodeBlock.Builder cb = CodeBlock.builder();
        cb.add("return ");
        cb.indent();
        ic.infos.forEach(i -> cb.add("Objects.equals(this.$N, that.$N) &&\n", i.nameMangled, i.nameMangled));
        cb.add("true;\n");
        cb.unindent();

        builder.addCode(cb.build());

        classBuilder.addMethod(builder.build());
    }


    private void addHashcode(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = startHashcode();
        builder.addStatement("return $T.hash(" + ic.listAllUsageParametrs() + ")", Types.java_objects);
        classBuilder.addMethod(builder.build());
    }

    private void addToString(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = startToString();

        CodeBlock.Builder cb = CodeBlock.builder();
        cb.add("return $T.toStringHelper(this)\n", Types.guava_moreObjects);
        cb.indent();
        ic.infos.forEach(i -> cb.add(".add($S, $N)\n", i.nameUpper, i.nameMangled));
        cb.add(".omitNullValues()\n");
        cb.add(".toString();\n");
        cb.unindent();

        builder.addCode(cb.build());
        classBuilder.addMethod(builder.build());
    }

    private void addWiths(InfoClass ic, TypeSpec.Builder classBuilder) {
        ic.infos.forEach(i -> {
                    MethodSpec.Builder builder = MethodSpec.methodBuilder("with" + i.nameUpper);
                    builder.addModifiers(Modifier.PUBLIC);
                    builder.addParameter(i.buildParameter());
                    builder.returns(ic.typeImmutable);
                    builder.addStatement("return new $N(" + ic.listAllUsageParametrs() + ")", ic.immutableClassName);

                    builder.addAnnotation(CheckReturnValue.class);
                    builder.addAnnotation(Nonnull.class);

                    classBuilder.addMethod(builder.build());
                }
        );
    }


    private void addGetters(List<Info> infos, TypeSpec.Builder classBuilder) {
        infos.forEach(i -> {
            MethodSpec getter = i.buildGetter();
            classBuilder.addMethod(getter);
        });
    }

    private void addMemberFields(List<Info> infos, TypeSpec.Builder classBuilder) {
        infos.forEach(info -> {
            FieldSpec f = info.buildField(Modifier.PRIVATE, Modifier.FINAL);
            classBuilder.addField(f);
        });
    }

    private void addConstructor(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        builder.addModifiers(Modifier.PRIVATE);
        ic.infos.forEach(i -> builder.addParameter(i.buildParameter()));
        ic.infos.forEach(i -> builder.addStatement("this.$N = $N", i.nameMangled, i.nameMangled));
        classBuilder.addMethod(builder.build());
    }

    private void addExtends(InfoClass ic, TypeSpec.Builder classBuilder) {
        if (!ic.isInterfaceDef) {
            classBuilder.superclass(ic.typeDef);
        }
    }


}