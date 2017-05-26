package com.test.template;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.codesorcerer.BeautifulBean;
import com.test.bean.sub.FooDef;

@BeautifulBean
public interface DavidDef
{
	@Nonnull
	SimpleDef getSimple();

	String getTitle1();

	String getTitle2();
	
	Map<? extends FooDef, ? extends List<? extends Map<? extends FooDef, ? extends FooDef>>> getFoos();
}
