package com.test;

import com.codesorcerer.targets.BeautifulBean;

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
