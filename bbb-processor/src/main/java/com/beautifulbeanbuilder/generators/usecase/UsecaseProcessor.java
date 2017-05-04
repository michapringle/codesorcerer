package com.beautifulbeanbuilder.generators.usecase;

import com.beautifulbeanbuilder.processor.AbstractProcessor;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion( SourceVersion.RELEASE_8)
public class UsecaseProcessor extends AbstractProcessor<UsecaseInfo>
{
	@Override
	public UsecaseInfo buildInput ( TypeElement te, String currentTypeName, String currentTypePackage){
		return new UsecaseInfo( te, currentTypePackage );
	}
}
