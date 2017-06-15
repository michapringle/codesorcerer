package com.codesorcerer.generators.subclassdispatch;

import com.codesorcerer.abstracts.AbstractJavaSpell;
import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.codesorcerer.generators.def.spells.Types;
import com.codesorcerer.generators.subclassdispatch.SubclassDispatchInputBuilder.SubclassDispatchInfo;
import com.codesorcerer.targets.SubclassDispatch;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Collectors;

public class SubclassDispatchSpell extends AbstractJavaSpell<SubclassDispatch, SubclassDispatchInfo> {


    @Override
    public int getRunOrder() {
        return 200;
    }

    @Override
    public void build(Result<AbstractSpell<SubclassDispatch, SubclassDispatchInfo, TypeSpec.Builder>, SubclassDispatchInfo, TypeSpec.Builder> result) throws Exception {
        SubclassDispatchInfo ic = result.input;
        ClassName typeDispatcher = ClassName.get(ic.pkg, ic.name + "Dispatch");

        TypeSpec.Builder classBuilder = buildClass(typeDispatcher);

        classBuilder.addMethod(buildConstructor());
        classBuilder.addMethod(buildCreateFunction(ic));
        classBuilder.addMethod(buildCallFunction(ic));
        classBuilder.addMethod(buildCreateConsumer(ic));
        classBuilder.addMethod(buildCallConsumer(ic));

        result.output = classBuilder;
    }

    private MethodSpec buildConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
        constructor.addModifiers(Modifier.PRIVATE);
        return constructor.build();
    }

    private MethodSpec buildCallFunction(SubclassDispatchInfo ic) {
        MethodSpec.Builder callFunction = MethodSpec.methodBuilder("apply");
        callFunction.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        callFunction.returns(Types.jpT);
        callFunction.addTypeVariable(Types.jpT);


        callFunction.addParameter(ClassName.get(ic.schemeType), ic.nameLowerCase);

        for (TypeElement subTe : ic.subclasses) {
            ClassName className = ClassName.get(subTe);
            ParameterizedTypeName fun = ParameterizedTypeName.get(Types.java_function, className, Types.jpT);
            callFunction.addParameter(fun, getSubClassParameterName(subTe));
        }

        List<String> subclassParamNames = ic.subclasses.stream().map(this::getSubClassParameterName).collect(Collectors.toList());
        String lls = subclassParamNames.stream().map(x -> "$L").collect(Collectors.joining(", "));

        CodeBlock.Builder builderCall = CodeBlock.builder()
                .add("return createFunction(" + lls + ")", subclassParamNames.toArray())
                .addStatement(".apply($L)", ic.nameLowerCase);

        callFunction.addCode(builderCall.build());
        return callFunction.build();
    }

    private MethodSpec buildCreateFunction(SubclassDispatchInfo ic) {
        ClassName c = ClassName.get(ic.schemeType);
        ParameterizedTypeName funCtoT = ParameterizedTypeName.get(Types.java_function, c, Types.jpT);

        MethodSpec.Builder createFunction = MethodSpec.methodBuilder("createFunction");
        createFunction.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        createFunction.returns(funCtoT);
        createFunction.addTypeVariable(Types.jpT);
        for (TypeElement subTe : ic.subclasses) {
            ClassName className = ClassName.get(subTe);
            ParameterizedTypeName fun = ParameterizedTypeName.get(Types.java_function, className, Types.jpT);
            createFunction.addParameter(fun, getSubClassParameterName(subTe));
        }

        CodeBlock.Builder builder = CodeBlock.builder()
                .beginControlFlow("return $L ->", ic.nameLowerCase);


        for (TypeElement subTe : ic.subclasses) {
            ClassName className = ClassName.get(subTe);
            builder
                    .beginControlFlow("if($L instanceof $T)", ic.nameLowerCase, className)
                    .addStatement("return handler" + subTe.getSimpleName().toString() + ".apply(($T)$L)", className, ic.nameLowerCase)
                    .endControlFlow();
        }

        builder
                .addStatement("throw new $T(\"Codegeneration missed subclass of type: \" + $L.getClass().getName())", IllegalArgumentException.class, ic.nameLowerCase)
                .endControlFlow("");

        createFunction.addCode(builder.build());
        return createFunction.build();
    }



    private MethodSpec buildCallConsumer(SubclassDispatchInfo ic) {
        MethodSpec.Builder callFunction = MethodSpec.methodBuilder("consume");
        callFunction.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        callFunction.addParameter(ClassName.get(ic.schemeType), ic.nameLowerCase);

        for (TypeElement subTe : ic.subclasses) {
            ClassName className = ClassName.get(subTe);
            ParameterizedTypeName fun = ParameterizedTypeName.get(Types.java_consumer, className);
            callFunction.addParameter(fun, getSubClassParameterName(subTe));
        }

        List<String> subclassParamNames = ic.subclasses.stream().map(this::getSubClassParameterName).collect(Collectors.toList());
        String lls = subclassParamNames.stream().map(x -> "$L").collect(Collectors.joining(", "));

        CodeBlock.Builder builderCall = CodeBlock.builder()
                .add("createConsumer(" + lls + ")", subclassParamNames.toArray())
                .addStatement(".accept($L)", ic.nameLowerCase);

        callFunction.addCode(builderCall.build());
        return callFunction.build();
    }

    private MethodSpec buildCreateConsumer(SubclassDispatchInfo ic) {
        ClassName c = ClassName.get(ic.schemeType);
        ParameterizedTypeName funCtoT = ParameterizedTypeName.get(Types.java_consumer, c);

        MethodSpec.Builder createFunction = MethodSpec.methodBuilder("createConsumer");
        createFunction.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        createFunction.returns(funCtoT);
        for (TypeElement subTe : ic.subclasses) {
            ClassName className = ClassName.get(subTe);
            ParameterizedTypeName fun = ParameterizedTypeName.get(Types.java_consumer, className);
            createFunction.addParameter(fun, getSubClassParameterName(subTe));
        }

        CodeBlock.Builder builder = CodeBlock.builder()
                .beginControlFlow("return $L ->", ic.nameLowerCase);


        for (TypeElement subTe : ic.subclasses) {
            ClassName className = ClassName.get(subTe);
            builder
                    .beginControlFlow("if($L instanceof $T)", ic.nameLowerCase, className)
                    .addStatement("handler" + subTe.getSimpleName().toString() + ".accept(($T)$L)", className, ic.nameLowerCase)
                    .endControlFlow();
        }

        builder
                .addStatement("throw new $T(\"Codegeneration missed subclass of type: \" + $L.getClass().getName())", IllegalArgumentException.class, ic.nameLowerCase)
                .endControlFlow("");

        createFunction.addCode(builder.build());
        return createFunction.build();
    }


    private String getSubClassParameterName(TypeElement subTe) {
        return "handler" + subTe.getSimpleName().toString();
    }


}
