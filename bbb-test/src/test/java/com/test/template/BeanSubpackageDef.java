package com.test.template;

import com.beautifulbeanbuilder.BeautifulBean;

@BeautifulBean
public interface BeanSubpackageDef
{
	public static final BeanSubpackage X = BeanSubpackage.newBeanSubpackage( "xx" );

	String getThing();
}
