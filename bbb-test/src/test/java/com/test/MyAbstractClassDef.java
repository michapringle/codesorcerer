package com.test;

import javax.annotation.Nonnull;

import com.beautifulbeanbuilder.BeautifulBean;

@BeautifulBean
public abstract class MyAbstractClassDef
{
	@Nonnull
	public abstract String getThing();
	
	public int getIt() {
		return 3;
	}
}
