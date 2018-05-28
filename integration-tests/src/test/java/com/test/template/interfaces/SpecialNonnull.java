package com.test.template.interfaces;

import javax.annotation.Nonnull;

/**
 * <p/>
 * Created by Micha "Micha did it!" Pringle on December 18, 2014.
 */
@javax.annotation.concurrent.Immutable
@javax.annotation.concurrent.ThreadSafe
public interface SpecialNonnull
{
	@Nonnull
	public String getSpecial();
}
