package ca.pandp;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import ca.pandp.template.Complex;

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
