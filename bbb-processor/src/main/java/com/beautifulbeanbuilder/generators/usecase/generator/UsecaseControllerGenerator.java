package com.beautifulbeanbuilder.generators.usecase.generator;

import com.beautifulbeanbuilder.BBBJson;
import com.beautifulbeanbuilder.generators.usecase.UsecaseInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.central1.leanannotations.LeanEntryPoint;
import com.central1.leanannotations.LeanUsecase;
import com.google.common.collect.Lists;
import com.squareup.javapoet.*;
import io.reactivex.Observable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.removeEnd;

public class UsecaseControllerGenerator extends AbstractJavaGenerator<LeanUsecase, UsecaseInfo>
{
	@Override
	public void write( UsecaseInfo info, TypeSpec.Builder objectToWrite, ProcessingEnvironment processingEnv ) throws IOException
	{
		if ( objectToWrite != null )
		{
			JavaFile javaFile = JavaFile.builder( getControllerPackage( info ), objectToWrite.build() ).build();
			System.out.println( "Writing out object " + javaFile.packageName + "." + javaFile.typeSpec.name );
			javaFile.writeTo( processingEnv.getFiler() );
		}
	}

	private String getControllerName( UsecaseInfo info )
	{
		return info.typeElement.getSimpleName().toString().replace( "Usecase", "" ) + "Controller";
	}

	private String getEntitiesPackage( UsecaseInfo info )
	{
		return info.typePackage.replace( "usecases", "entities" );
	}

	private String getControllerPackage( UsecaseInfo info )
	{
		return info.typePackage.replace( "usecases", "controllers" );
	}

	@Override
	public TypeSpec.Builder build( UsecaseInfo ic, Map<AbstractGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnv )
			throws IOException
	{
		final ClassName controller = ClassName.get( getControllerPackage( ic ), getControllerName( ic ) );
		final TypeSpec.Builder classBuilder = buildClass( controller );

		classBuilder.addAnnotation( RestController.class );
		classBuilder.addAnnotation( LeanEntryPoint.class );

		final FieldSpec usecaseField = FieldSpec.builder( TypeName.get( ic.typeElement.asType() ), "usecase", Modifier.FINAL, Modifier.PRIVATE ).build();
		classBuilder.addField( usecaseField );

		process( ic, classBuilder, processingEnv );

		MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers( Modifier.PUBLIC );
		classBuilder.build().fieldSpecs.forEach(
				f ->
				{
					constructorBuilder.addParameter( ParameterSpec.builder( f.type, f.name ).build() );
					constructorBuilder.addStatement( "this." + f.name + "= " + f.name );
				}
		);
		classBuilder.addMethod( constructorBuilder.addModifiers( Modifier.PUBLIC ).build() );

		addEntityMapperMethod( classBuilder );

		return classBuilder;
	}

	private void addEntityMapperMethod( TypeSpec.Builder classBuilder )
	{
		TypeVariableName t = TypeVariableName.get( "T" );
		TypeVariableName r = TypeVariableName.get( "R", ClassName.get( "com.central1.lean.entities", "EntityRef" ) );

		ClassName obClassName = ClassName.bestGuess( Observable.class.getName() );
		ClassName listClassName = ClassName.bestGuess( List.class.getName() );

		ParameterizedTypeName returnType = ParameterizedTypeName.get( obClassName, ParameterizedTypeName.get( listClassName, t ) );
		ParameterizedTypeName param1Type = ParameterizedTypeName.get( obClassName, ParameterizedTypeName.get( listClassName, r ) );
		ParameterizedTypeName param2Type = ParameterizedTypeName.get( ClassName.get( "com.central1.lean.mapping", "Mapper" ), r, t );

		MethodSpec spec = MethodSpec.methodBuilder("getListObservable")
				.addModifiers( Modifier.PRIVATE )
				.addTypeVariable( t )
				.addTypeVariable( r )
				.addParameter( param1Type, "refList" )
				.addParameter( param2Type,"mapper" )
				.returns( returnType )
				.addStatement( "return refList.switchMap( refs -> {\n"
						+ "\t\t\tfinal $T<Observable<T>> iterable = refs.stream().map( mapper::getEntity )::iterator;\n"
						+ "\t\t\treturn Observable.combineLatest( iterable, arr -> $T.asList( (T[]) arr ) );\n"
						+ "\t\t} )", Iterable.class, Arrays.class )
				.build();

		classBuilder.addMethod( spec );
	}

	private TypeMirror getTypeMirror( String className, ProcessingEnvironment processingEnv )
	{
		return processingEnv.getTypeUtils().erasure(processingEnv.getElementUtils().getTypeElement( className ).asType());
	}

	private void process( UsecaseInfo ic, TypeSpec.Builder classBuilder, ProcessingEnvironment processingEnv )
	{
		String stompReadPrefix = "/queue/" + ic.typeElement.getQualifiedName().toString() + "/";
		String stompPocPrefix = "/stomp-poc/" + ic.typeElement.getQualifiedName().toString() + "/";

		for ( ExecutableElement e : ic.getAllMethodsExposed() )
		{
			TypeMirror returnType = e.getReturnType();

			// Check wether returnType is Observable.
			TypeMirror obType = getTypeMirror( Observable.class.getName(), processingEnv );
			if ( processingEnv.getTypeUtils().isAssignable( returnType, obType ) && TypeKind.DECLARED.equals( returnType.getKind() ) )
			{
				TypeMirror obParamType = ( (DeclaredType) returnType ).getTypeArguments().get( 0 );
				TypeMirror listType = getTypeMirror( List.class.getName(), processingEnv );
				if( processingEnv.getTypeUtils().isAssignable( obParamType, listType ) && TypeKind.DECLARED.equals( obParamType.getKind() ))
				{
					//It is Observable of list. Need to get the list's entity type.
					TypeMirror entityRefType = ( (DeclaredType) obParamType ).getTypeArguments().get( 0 );
					processStompMethod( ic, e, entityRefType, stompReadPrefix, true, classBuilder );
				}
				else
				{
					processStompMethod( ic, e, obParamType, stompReadPrefix, false, classBuilder );
				}
			}
			else
			{
				processPostMethods( ic, e, stompPocPrefix, classBuilder );
			}
		}
	}

	private void processStompMethod( UsecaseInfo info, ExecutableElement e,  TypeMirror entityRefTm,
			String stompPrefix, boolean isList, TypeSpec.Builder classBuilder )
	{
		//It is Observable of list. Need to get the list's entity type.
		//TypeMirror entityRefType = ( (DeclaredType) obParamType ).getTypeArguments().get( 0 );
		if ( StringUtils.endsWith( entityRefTm.toString(), "Ref" ) )
		{
			final ClassName entityType = ClassName.bestGuess( getFQEntityName( info, removeEnd( entityRefTm.toString(), "Ref") ) );
			final ClassName entityRefType = ClassName.bestGuess( getFQEntityName( info, entityRefTm.toString() ) );
			final ClassName obClassName = ClassName.bestGuess( Observable.class.getName() );

			// Create a corresponding mapper class field
			final ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
					ClassName.get( "com.central1.lean.mapping", "Mapper" ),
					entityRefType, entityType
			);
			final String eRefName = entityRefTm.toString().substring( entityRefTm.toString().lastIndexOf( '.' ) + 1 );
			final String mapperName = eRefName.substring( 0, 1 ).toLowerCase() + eRefName.substring( 1 ) + "Mapper";
			final FieldSpec.Builder fieldBuilder = FieldSpec.builder( parameterizedTypeName, mapperName, Modifier.PRIVATE, Modifier.FINAL );

			if( classBuilder.build().fieldSpecs.stream().noneMatch( f -> f.type.equals( fieldBuilder.build().type ) ) )
			{
				classBuilder.addField( fieldBuilder.build() );

				// Automatically generate a mapper method for the entity
				if( isList )
				{
					ParameterizedTypeName stompEntityReturnType = ParameterizedTypeName.get( obClassName, entityType );
					String methodName = "get" + entityType.simpleName();
					MethodSpec.Builder stompEntityMethod = MethodSpec.methodBuilder( methodName )
							.addModifiers( Modifier.PUBLIC )
							.returns( stompEntityReturnType )
							.addAnnotation( AnnotationSpec.builder( SubscribeMapping.class )
									.addMember( "value", "\"$L/{$L}\"", stompPrefix + methodName, "ref" )
									.build() )
							.addParameter( entityRefType, "ref" )
							.addStatement( "return " + mapperName + ".getEntity( ref )" );
					classBuilder.addMethod( stompEntityMethod.build() );
				}
			}

			// Create stomp method
			ParameterizedTypeName stompReturnType;
			if ( isList )
			{
				stompReturnType = ParameterizedTypeName.get( obClassName, ParameterizedTypeName.get( ClassName.bestGuess( List.class.getName() ), entityType ) );
			}
			else
			{
				stompReturnType = ParameterizedTypeName.get( obClassName, entityType );
			}

			MethodSpec.Builder stompMethod = MethodSpec.methodBuilder( e.getSimpleName().toString() )
					.addModifiers( Modifier.PUBLIC )
					.returns( stompReturnType );

			// This only handles read method with at most one parameter
			if ( e.getParameters().size() == 1 )
			{
				VariableElement p = e.getParameters().get( 0 );
				String pName = p.getSimpleName().toString();
				stompMethod
						.addAnnotation( AnnotationSpec.builder( SubscribeMapping.class )
								.addMember( "value", "\"$L/{$L}\"", stompPrefix + e.getSimpleName(), pName )
								.build() )
						.addParameter( ParameterSpec.builder( getFQParameterClassName( info, p ), pName )
								.addAnnotation( AnnotationSpec.builder( DestinationVariable.class ).addMember( "value", "$S", pName )
										.build() )
								.build() );
				if( isList )
				{
					stompMethod.addStatement( "return getListObservable( this.usecase." + e.getSimpleName() + "(" + pName + "), " + mapperName + ")" );
				}
				else
				{
					stompMethod.addStatement( "return this.usecase." + e.getSimpleName() + "(" + pName + ").flatMap( " + mapperName + "::getEntity  )" );
				}
			}
			else
			{
				stompMethod
						.addAnnotation( AnnotationSpec.builder( SubscribeMapping.class )
								.addMember( "value", "$S", stompPrefix + e.getSimpleName() )
								.build() );
				if ( isList )
				{
					stompMethod.addStatement( "return getListObservable( this.usecase." + e.getSimpleName() + "(), " + mapperName +" )" );
				}
				else
				{
					stompMethod.addStatement( "return this.usecase." + e.getSimpleName() + "().flatMap( " + mapperName + "::getEntity  )" );
				}
			}
			classBuilder.addMethod( stompMethod.build() );

		}
		else
		{
			throw new IllegalArgumentException( "Usecase should only return Observable of EntityRef" );
		}
	}

	private void processPostMethods( UsecaseInfo info, ExecutableElement e, String stompPrefix, TypeSpec.Builder classBuilder )
	{
		// Add inner class for request body bean def
		String methodName = e.getSimpleName().toString();
		String requestBodyName = methodName + "Request";
		String requestBeanName = requestBodyName.substring( 0,1 ).toUpperCase() + requestBodyName.substring( 1 );
		String requestVar = "requestBean";

		TypeSpec.Builder rbeanBuilder = TypeSpec.interfaceBuilder( requestBeanName + "Def" ).addAnnotation( BBBJson.class );
		List<String> paramCalls = Lists.newArrayList();
		e.getParameters().forEach( p ->
		{
			String getterName = "get" + p.getSimpleName().toString().substring( 0, 1 ).toUpperCase() + p.getSimpleName().toString().substring( 1 );
			MethodSpec.Builder getter = MethodSpec.methodBuilder( getterName )
					.addModifiers( Modifier.PUBLIC, Modifier.ABSTRACT )
					.returns( getFQParameterClassName( info,  p ) );
			rbeanBuilder.addMethod( getter.build() );

			paramCalls.add( requestVar + "." + getterName + "()" );
		});
		classBuilder.addType( rbeanBuilder.build() );

		MethodSpec.Builder postMethod = MethodSpec.methodBuilder( methodName )
				.addAnnotation( AnnotationSpec.builder(RequestMapping.class )
								.addMember( "value",  "$S", stompPrefix + e.getSimpleName() )
								.addMember( "method",  "$T.POST", RequestMethod.class )
								.build()  )
				.returns( TypeName.get( e.getReturnType() ) )
				.addParameter( ParameterSpec.builder( ClassName.bestGuess( requestBeanName ), requestVar ).addAnnotation( RequestBody.class ).build() )
				.addStatement( "return this.usecase." + methodName + "(" + StringUtils.join( paramCalls, ", " ) + ")" );
		classBuilder.addMethod( postMethod.build() );

	}

	private ClassName getFQParameterClassName( UsecaseInfo info, VariableElement p )
	{
		return ClassName.bestGuess( getFQEntityName( info, p.asType().toString() ) );
	}

	private String getFQEntityName( UsecaseInfo info, String bestGuessClassName ) {
		return ( bestGuessClassName.indexOf( '.' ) == -1 ) ? getEntitiesPackage( info ) + "." + bestGuessClassName : bestGuessClassName;
	}
}
