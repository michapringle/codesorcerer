package com.codesorcerer.generators.def.spells;

import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.codesorcerer.generators.def.BeanDefInfo;
import com.codesorcerer.targets.BBBJson;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collection;

public class JacksonSpell extends AbstractJavaBeanSpell<BBBJson> {
    @Override
    public int getRunOrder() {
        return 300;
    }

    @Override
    public void build(Result<AbstractSpell<BBBJson, BeanDefInfo, TypeSpec.Builder>, BeanDefInfo, TypeSpec.Builder> result) throws Exception {
    }

    @Override
    public void modify(Result<AbstractSpell<BBBJson, BeanDefInfo, TypeSpec.Builder>, BeanDefInfo, TypeSpec.Builder> result, Collection<Result> results) throws Exception {
        BeanDefInfo ic = result.input;
        ClassName typeJackson = ClassName.get(ic.pkg, ic.immutableClassName + "Jackson");
        addJsonSerializationAnnotations(ic, getResult(ImmutableSpell.class, result.te, results).output, typeJackson);
    }

    private void addJsonSerializationAnnotations(BeanDefInfo ic, TypeSpec.Builder classBuilder, ClassName typeJackson) {
        classBuilder.addAnnotation(AnnotationSpec.builder(JsonDeserialize.class)
                .addMember("builder", ic.immutableClassName + ".BeanBuilder.class")
                .build());

        classBuilder.addAnnotation(AnnotationSpec.builder(JsonTypeInfo.class)
                .addMember("use", "$T.Id.CLASS", JsonTypeInfo.class)
                .addMember("include", "$T.As.PROPERTY", JsonTypeInfo.class)
                .addMember("property", "\"clazz\"")
                .build());
    }

}
