package com.test.template;

import javax.annotation.Nonnull;

import com.codesorcerer.targets.BeautifulBean;

@BeautifulBean
public interface BeanNonNullDef
{

	public static final String CONST = "abc"; //Should not crash BBB

	int getAuthReq();  //Primitives ok too

	String getMyStr();

	byte[] getStuff();

	@Nonnull
	Integer getMyInt();

}
