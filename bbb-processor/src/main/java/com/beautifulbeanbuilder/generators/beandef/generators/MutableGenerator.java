package com.beautifulbeanbuilder.generators.beandef.generators;

import com.beautifulbeanbuilder.BBBImmutable;
import com.beautifulbeanbuilder.BBBMutable;
import com.beautifulbeanbuilder.generators.beandef.BeanDefInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaBeanGenerator;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MutableGenerator extends AbstractJavaBeanGenerator<BBBMutable>
{


    @Override
    public List<Class<? extends Annotation>> requires() {
        return Collections.singletonList(BBBImmutable.class);
    }

    @Override
    public TypeSpec.Builder build(BeanDefInfo ic, Map<AbstractGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnvironment) throws IOException {
        ClassName typeMutable = ClassName.get(ic.pkg, ic.immutableClassName + "Mutable");

        final TypeSpec.Builder classBuilder = buildClass(typeMutable);
        addSerialVersionUUID(classBuilder);

        //Members
        ic.beanDefFieldInfos.forEach(i -> {
            final FieldSpec f = i.buildField(Modifier.PRIVATE);
            classBuilder.addField(f);
        });

        //Getters
        ic.beanDefFieldInfos.forEach(i -> {
            final MethodSpec getter = i.buildGetter();
            classBuilder.addMethod(getter);
        });

        //Setters
        ic.beanDefFieldInfos.forEach(i -> {
            final MethodSpec setter = i.buildSetter();
            classBuilder.addMethod(setter);
        });

        addToImmutable(ic, classBuilder);
        addToMutable(ic, getTypeBuilder(ImmutableGenerator.class, generatorBuilderMap), typeMutable);

        return classBuilder;
    }

    private void addToMutable(BeanDefInfo ic, TypeSpec.Builder classBuilder, ClassName typeMutable) {
        MethodSpec.Builder toMutable1 = MethodSpec.methodBuilder("toMutable");
        toMutable1.addModifiers(Modifier.PUBLIC);
        toMutable1.returns(typeMutable);
        toMutable1.addStatement("return toMutable(this)");
        classBuilder.addMethod(toMutable1.build());

        MethodSpec.Builder toMutable = MethodSpec.methodBuilder("toMutable");
        toMutable.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        toMutable.returns(typeMutable);
        toMutable.addParameter(ic.typeImmutable, "immutable");
        toMutable.addStatement("$T mutable = new $T()", typeMutable, typeMutable);
        toMutable.addStatement("if(immutable != null) {");
        ic.beanDefFieldInfos.forEach(i -> toMutable.addStatement("mutable.set" + i.nameUpper + "(immutable." + i.prefix + i.nameUpper + "())"));
        toMutable.addStatement("}");
        toMutable.addStatement("return mutable");
        classBuilder.addMethod(toMutable.build());

    }

    private void addToImmutable(BeanDefInfo ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder toMutable1 = MethodSpec.methodBuilder("toImmutable");
        toMutable1.addModifiers(Modifier.PUBLIC);
        toMutable1.returns(ic.typeImmutable);

        CodeBlock.Builder cb = CodeBlock.builder();
        cb.add("return $T.build" + ic.immutableClassName + "()", ic.typeImmutable);
        cb.indent();
        ic.nonNullBeanDefFieldInfos.forEach(i -> cb.add("." + i.nameMangled + "(" + i.nameMangled + ")"));
        ic.nullableBeanDefFieldInfos.forEach(i -> cb.add("." + i.nameMangled + "(" + i.nameMangled + ")"));
        cb.add(".build();");
        cb.unindent();
        toMutable1.addCode(cb.build());

        classBuilder.addMethod(toMutable1.build());
    }
}
