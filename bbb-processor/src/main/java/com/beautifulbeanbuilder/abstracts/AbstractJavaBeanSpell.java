package com.beautifulbeanbuilder.abstracts;

import com.beautifulbeanbuilder.generators.def.BeanDefInfo;
import com.beautifulbeanbuilder.processor.CodeSorcererProcessor;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;

public abstract class AbstractJavaBeanSpell<T extends Annotation> extends AbstractJavaSpell<T, BeanDefInfo> {

    @Override
    public void write(CodeSorcererProcessor.Result<AbstractSpell<T, BeanDefInfo, TypeSpec.Builder>, BeanDefInfo, TypeSpec.Builder> result) throws Exception {
        JavaFile javaFile = JavaFile.builder(result.input.pkg, result.output.build()).build();
        javaFile.writeTo(filer);
        //System.out.println("Conjured " + result.input.pkg + "." + javaFile.typeSpec.name );
    }
}