package com.beautifulbeanbuilder.generators.beandef.generators;

import com.beautifulbeanbuilder.BBBJson;
import com.beautifulbeanbuilder.BBBMutable;
import com.beautifulbeanbuilder.generators.beandef.BeanDefInfo;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
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
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class JacksonGenerator extends AbstractJavaGenerator<BBBJson> {

    @Override
    public List<Class<? extends Annotation>> requires() {
        return ImmutableList.of(BBBMutable.class);
    }

    public TypeSpec.Builder build(BeanDefInfo ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnvironment) throws IOException {
        ClassName typeJackson = ClassName.get(ic.pkg, ic.immutableClassName + "Jackson");

        //TODO: dont depend on mutable
        ClassName typeMutable = ClassName.get(ic.pkg, ic.immutableClassName + "Mutable");

        final TypeSpec.Builder classBuilder = buildClass(typeJackson);
        classBuilder.addType(buildSerializer(ic).build());
        classBuilder.addType(buildDeserializer(ic, typeMutable).build());

        addJsonSerializationAnnotations(ic, getTypeBuilder(ImmutableGenerator.class, generatorBuilderMap), typeJackson);

        return classBuilder;
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
        m.addParameter(ClassName.get(JsonGenerator.class), "gen");
        m.addParameter(ClassName.get(SerializerProvider.class), "sp");
        m.addException(ClassName.get(IOException.class));
        m.addException(ClassName.get(JsonGenerationException.class));

        m.addStatement("gen.writeObject(o.toMutable())");

        classBuilder.addMethod(m.build());
        return classBuilder;
    }

}
