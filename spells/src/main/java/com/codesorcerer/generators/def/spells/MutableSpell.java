package com.codesorcerer.generators.def.spells;

import com.codesorcerer.BBBMutable;
import com.codesorcerer.abstracts.AbstractJavaBeanSpell;
import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.codesorcerer.generators.def.BeanDefInfo;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.Collection;

public class MutableSpell extends AbstractJavaBeanSpell<BBBMutable> {


    @Override
    public int getRunOrder() {
        return 200;
    }

    @Override
    public void build(Result<AbstractSpell<BBBMutable, BeanDefInfo, TypeSpec.Builder>, BeanDefInfo, TypeSpec.Builder> result) throws Exception {
        BeanDefInfo bi = result.input;
        ClassName typeMutable = ClassName.get(bi.pkg, bi.immutableClassName + "Mutable");

        final TypeSpec.Builder classBuilder = buildClass(typeMutable);
        addSerialVersionUUID(classBuilder);

        //Members
        bi.beanDefFieldInfos.forEach(i -> {
            final FieldSpec f = i.buildField(Modifier.PRIVATE);
            classBuilder.addField(f);
        });

        //Getters
        bi.beanDefFieldInfos.forEach(i -> {
            final MethodSpec getter = i.buildGetter();
            classBuilder.addMethod(getter);
        });

        //Setters
        bi.beanDefFieldInfos.forEach(i -> {
            final MethodSpec setter = i.buildSetter();
            classBuilder.addMethod(setter);
        });

        addToImmutable(bi, classBuilder);

        result.output = classBuilder;
    }

    @Override
    public void modify(Result<AbstractSpell<BBBMutable, BeanDefInfo, TypeSpec.Builder>, BeanDefInfo, TypeSpec.Builder> result, Collection<Result> results) throws Exception {
        BeanDefInfo bi = result.input;
        ClassName typeMutable = ClassName.get(bi.pkg, bi.immutableClassName + "Mutable");

        addToMutable(bi, getResult(ImmutableSpell.class, result.te, results).output, typeMutable);
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
