package com.beautifulbeanbuilder.generators.entity.generator;

import com.beautifulbeanbuilder.generators.entity.EntityDefInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.central1.leanannotations.LeanEntity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.endsWith;

public class EntityRefGenerator extends AbstractJavaGenerator<LeanEntity, EntityDefInfo>
{
	@Override
	public void write( EntityDefInfo info, TypeSpec.Builder objectToWrite, ProcessingEnvironment processingEnv ) throws IOException
	{
		if ( objectToWrite != null )
		{
			JavaFile javaFile = JavaFile.builder( info.typePackage, objectToWrite.build() ).build();
			System.out.println( "Writing out object " + javaFile.packageName + "." + javaFile.typeSpec.name );
			javaFile.writeTo( new File( "generated" ) );
			javaFile.writeTo( processingEnv.getFiler() );
		}
	}

	private String getRefName( EntityDefInfo info )
	{
		if (endsWith(info.typeElement.toString(), "Def")) {

			String s = info.typeElement.getSimpleName().toString();
			return StringUtils.replacePattern( s, "Def$", "Ref" );
		}
		throw new IllegalArgumentException( "Invalid EntitiyDefInfo" );
	}

	@Override
	public TypeSpec.Builder build( EntityDefInfo info, Map<AbstractGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnv )
			throws IOException
	{
		final ClassName entityRefClass = ClassName.get( info.typePackage,  getRefName( info ));
		final TypeSpec.Builder classBuilder = buildClass( entityRefClass );

		classBuilder.superclass( ClassName.get( "com.central1.lean.entities", "EntityRef" ) );

		MethodSpec.Builder constructorbuilder = MethodSpec.constructorBuilder().addModifiers( Modifier.PUBLIC );
		constructorbuilder.addAnnotation( JsonCreator.class )
				.addParameter( ParameterSpec.builder( String.class, "id" )
								.addAnnotation( AnnotationSpec.builder( JsonProperty.class ).addMember( "value", "$S", "id" ).build() )
								.build() )
				.addStatement( "super( id )" );
		classBuilder.addMethod( constructorbuilder.build() );
		return classBuilder;
	}
}
