package com.test;

import javax.annotation.Nonnull;

import com.codesorcerer.targets.BeautifulBean;

@BeautifulBean
public abstract class MyAbstractClassDef
{
	@Nonnull
	public abstract String getThing();
	
	public int getIt() {
		return 3;
	}
}
