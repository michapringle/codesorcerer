package com.test.template;

import javax.annotation.Nonnull;

import com.beautifulbeanbuilder.BeautifulBean;
import com.test.template.bnn_sbnn.Simple2Def;

@BeautifulBean
public interface Complex2Def
{
	@Nonnull
	Simple2Def getSimple2();
}
