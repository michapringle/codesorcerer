package com.test;

import com.test.template.BeanNonNull;
import org.junit.Assert;
import org.junit.Test;

import static com.test.template.BeanNonNull.buildBeanNonNull;

public class TestNonNullStuff
{

	@Test
	public void testCopy()
	{
		BeanNonNull b1 = buildBeanNonNull().myInt( 1 ).build();
		BeanNonNull b2 = b1.update().myInt( 2 ).build();
		Assert.assertNull( b2.getMyStr() );
		Assert.assertEquals( new Integer( 2 ), b2.getMyInt() );
	}

	@Test
	public void testBuildInterface()
	{
		BeanNonNull b1 = buildBeanNonNull().myInt( 1 ).myStr( "a" ).build();
		Assert.assertEquals( "a", b1.getMyStr() );
		Assert.assertEquals( new Integer( 1 ), b1.getMyInt() );
	}

	@Test( expected = NullPointerException.class )
	public void testNullVal()
	{
		buildBeanNonNull().myInt( null );
	}
}
