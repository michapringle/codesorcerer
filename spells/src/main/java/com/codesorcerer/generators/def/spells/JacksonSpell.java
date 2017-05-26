package com.codesorcerer.generators.def.spells;

import com.codesorcerer.targets.BBBJson;
import com.codesorcerer.abstracts.AbstractJavaBeanSpell;
import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.codesorcerer.generators.def.BeanDefInfo;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collection;

public class JacksonSpell extends AbstractJavaBeanSpell<BBBJson>
{
    @Override
    public int getRunOrder() {
        return 300;
    }

    @Override
    public void build(Result<AbstractSpell<BBBJson, BeanDefInfo, TypeSpec.Builder>, BeanDefInfo, TypeSpec.Builder> result) throws Exception {
        BeanDefInfo ic = result.input;

        ClassName typeJackson = ClassName.get(ic.pkg, ic.immutableClassName + "Jackson");

        //TODO: dont depend on mutable
        ClassName typeMutable = ClassName.get(ic.pkg, ic.immutableClassName + "Mutable");

        final TypeSpec.Builder classBuilder = buildClass(typeJackson);
        classBuilder.addType(buildSerializer(ic).build());
        classBuilder.addType(buildDeserializer(ic, typeMutable).build());

        result.output = classBuilder;
    }

    @Override
    public void modify(Result<AbstractSpell<BBBJson, BeanDefInfo, TypeSpec.Builder>, BeanDefInfo, TypeSpec.Builder> result, Collection<Result> results) throws Exception {
        BeanDefInfo ic = result.input;
        ClassName typeJackson = ClassName.get(ic.pkg, ic.immutableClassName + "Jackson");
        addJsonSerializationAnnotations(ic, getResult(ImmutableSpell.class, result.te, results).output, typeJackson);
    }

    private void addJsonSerializationAnnotations(BeanDefInfo ic, TypeSpec.Builder classBuilder, ClassName typeJackson) {
        classBuilder.addAnnotation(AnnotationSpec.builder(JsonDeserialize.class)
                .addMember("using", typeJackson.simpleName() + ".Deserializer.class")
                .build());

        classBuilder.addAnnotation(AnnotationSpec.builder(JsonSerialize.class)
                .addMember("using", typeJackson.simpleName() + ".Serializer.class")
                .build());
    }



    private TypeSpec.Builder buildDeserializer(BeanDefInfo ic, ClassName typeMutable) {
        final TypeSpec.Builder classBuilder = buildClass(ClassName.bestGuess("Deserializer"));
        classBuilder.addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        //Extends
        classBuilder.superclass(ParameterizedTypeName.get(ClassName.get(StdDeserializer.class), ic.typeImmutable));

        //Constructor
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
        constructor.addModifiers(Modifier.PUBLIC);
        constructor.addStatement("super($T.class)", ic.typeImmutable);
        classBuilder.addMethod(constructor.build());

        //Serialize
        MethodSpec.Builder m = MethodSpec.methodBuilder("deserialize");
        m.addModifiers(Modifier.PUBLIC);
        m.returns(ic.typeImmutable);
        m.addParameter(ClassName.get(JsonParser.class), "jp");
        m.addParameter(ClassName.get(DeserializationContext.class), "dc");
        m.addException(ClassName.get(IOException.class));
        m.addException(ClassName.get(JsonProcessingException.class));

        m.addStatement("return jp.readValueAs($T.class).toImmutable()", typeMutable);

        classBuilder.addMethod(m.build());
        return classBuilder;
    }

    private TypeSpec.Builder buildSerializer(BeanDefInfo ic) {
        final TypeSpec.Builder classBuilder = buildClass(ClassName.bestGuess("Serializer"));
        classBuilder.addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        //Extends
        classBuilder.superclass(ParameterizedTypeName.get(ClassName.get(StdSerializer.class), ic.typeImmutable));


        //Constructor
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
        constructor.addModifiers(Modifier.PUBLIC);
        constructor.addStatement("super($T.class)", ic.typeImmutable);
        classBuilder.addMethod(constructor.build());

        //Serialize
        MethodSpec.Builder m = MethodSpec.methodBuilder("serialize");
        m.addModifiers(Modifier.PUBLIC);
        m.addParameter(ic.typeImmutable, "o");
        m.addParameter(ClassName.get(JsonGenerator.class), "spell");
        m.addParameter(ClassName.get(SerializerProvider.class), "sp");
        m.addException(ClassName.get(IOException.class));
        m.addException(ClassName.get(JsonGenerationException.class));

        m.addStatement("spell.writeObject(o.toMutable())");

        classBuilder.addMethod(m.build());
        return classBuilder;
    }

}
