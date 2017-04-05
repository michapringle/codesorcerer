package com.beautifulbeanbuilder.processor.builders;

import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public class JacksonBuilder extends AbstractBuilder {

    public TypeSpec.Builder buildJacksonSerializers(InfoClass ic) throws IOException {
        final TypeSpec.Builder classBuilder = buildClass(ic.typeJackson);
        classBuilder.addType(buildSerializer(ic).build());
        classBuilder.addType(buildDeserializer(ic).build());
        return classBuilder;
    }

    private TypeSpec.Builder buildDeserializer(InfoClass ic) {
        final TypeSpec.Builder classBuilder = buildClass(ClassName.bestGuess("Deserializer"));
        classBuilder.addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        //Extends
        classBuilder.superclass(ParameterizedTypeName.get(ClassName.get(StdDeserializer.class), ic.typeImmutable));


        //Constructor
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
        constructor.addModifiers(Modifier.PUBLIC);
        constructor.addStatement("super($T.class)", ic.typeImmutable);
        classBuilder.addMethod(constructor.build());

//    public SWEngineer deserialize(JsonParser jp, DeserializationContext dc)
        //           throws IOException, JsonProcessingException {

        //Serialize
        MethodSpec.Builder m = MethodSpec.methodBuilder("deserialize");
        m.addModifiers(Modifier.PUBLIC);
        m.returns(ic.typeImmutable);
        m.addParameter(ClassName.get(JsonParser.class), "jp");
        m.addParameter(ClassName.get(DeserializationContext.class), "dc");
        m.addException(ClassName.get(IOException.class));
        m.addException(ClassName.get(JsonProcessingException.class));

        m.addStatement("return jp.readValueAs($T.class).toImmutable()", ic.typeMutable);

        classBuilder.addMethod(m.build());
        return classBuilder;
    }

    private TypeSpec.Builder buildSerializer(InfoClass ic) {
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
        m.addParameter(ClassName.get(JsonGenerator.class), "gen");
        m.addParameter(ClassName.get(SerializerProvider.class), "sp");
        m.addException(ClassName.get(IOException.class));
        m.addException(ClassName.get(JsonGenerationException.class));

        m.addStatement("gen.writeObject($T.fromImmutable(o))", ic.typeMutable);

        classBuilder.addMethod(m.build());
        return classBuilder;
    }

}
