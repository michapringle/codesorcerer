package com.beautifulbeanbuilder.generators.entity.generator;

import com.beautifulbeanbuilder.LeanEntityRefTypescript;
import com.beautifulbeanbuilder.generators.beandef.generators.TypescriptGenerator;
import com.beautifulbeanbuilder.generators.entity.EntityDefInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import org.apache.commons.io.FileUtils;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.replacePattern;

public class EntityRefTypescriptGenerator extends AbstractGenerator<LeanEntityRefTypescript, EntityDefInfo, String>
{

	@Override
	public void processingOver( Collection<String> objects, ProcessingEnvironment processingEnv )
	{
	}

	@Override
	public void write( EntityDefInfo ic, String objectToWrite, ProcessingEnvironment processingEnv ) throws IOException
	{
		File file = new File( TypescriptGenerator.DIR, ic.typePackage + "." + getRefName( ic ) + ".ts" );
		System.out.println( "Writing out object " + file.getName() );
		FileUtils.write( file, objectToWrite, Charset.defaultCharset() );
	}

	private String getRefName( EntityDefInfo ic )
	{
		return replacePattern( ic.typeElement.getSimpleName().toString(), "Def$", "Ref" );
	}

	@Override
	public String build( EntityDefInfo ic, Map<AbstractGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnv )
			throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "import {Injectable} from '@angular/core';\n" );
		sb.append( "import {EntityRef} from './entity.ref';\n" );
		sb.append( "\n" );
		sb.append( "@Injectable()\n" );
		sb.append( "export class " + getRefName( ic ) + " extends EntityRef {\n" );
		sb.append( "//-----------------Constructor\n" );
		sb.append( "     constructor( id:String ) { super(id); }\n" );
		sb.append( "\n" );
		sb.append( " }\n" );
		sb.append( "\n" );

		return sb.toString();
	}
}
