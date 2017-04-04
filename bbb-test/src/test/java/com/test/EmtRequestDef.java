package com.test;

import com.beautifulbeanbuilder.BeautifulBean;

import javax.annotation.Nonnull;
import java.io.Serializable;

@BeautifulBean
public interface EmtRequestDef extends Serializable
{
	@Nonnull
	String getRequest();

	@Nonnull
	InteracOnlineDef getInteracOnline();
}
