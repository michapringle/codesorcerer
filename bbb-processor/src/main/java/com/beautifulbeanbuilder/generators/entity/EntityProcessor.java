package com.beautifulbeanbuilder.generators.entity;

import com.beautifulbeanbuilder.processor.AbstractProcessor;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion( SourceVersion.RELEASE_8)
public class EntityProcessor extends AbstractProcessor<EntityDefInfo>
{
	@Override
	public EntityDefInfo buildInput ( TypeElement te, String currentTypeName, String currentTypePackage){
		return new EntityDefInfo( te, currentTypePackage );
	}
}
