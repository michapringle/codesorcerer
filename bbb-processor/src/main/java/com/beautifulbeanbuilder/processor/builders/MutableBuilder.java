package com.beautifulbeanbuilder.processor.builders;

import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;

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

        return classBuilder;
    }

}
