package com.test;

import javax.annotation.Nonnull;

import com.codesorcerer.BeautifulBean;

@BeautifulBean
public interface InteracOnlineDef {
	@Nonnull
	String getFiId();

	@Nonnull
	String getFiUserId();

	String getContactId();

}
