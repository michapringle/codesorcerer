package com.test;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import com.test.template.Complex;

/**
 * <p/>
 * Created by Micha "Micha did it!" Pringle on November 25, 2014.
 */
@javax.annotation.concurrent.Immutable
@javax.annotation.concurrent.ThreadSafe
public class TestDataIntegrity
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass( Complex.class )
				.verify();
	}
}
