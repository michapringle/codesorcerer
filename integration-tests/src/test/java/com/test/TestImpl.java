package com.test;

import java.io.Serializable;

import com.test.template.BeanAllNullable;
import org.junit.Assert;
import org.junit.Test;

import static com.test.template.BeanAllNullable.buildBeanAllNullable;

public class TestImpl
{

	@Test
	public void testEquals()
	{
		BeanAllNullable b1 = BeanAllNullable.newBeanAllNullable( 1, "a", false );
		BeanAllNullable b2 = BeanAllNullable.newBeanAllNullable( 1, "a", false );
		Assert.assertTrue( b1.equals( b2 ) );
	}

	@Test
	public void testIsSerialzable()
	{
		Assert.assertTrue( BeanAllNullable.newBeanAllNullable( 1, null, false ) instanceof Serializable );
	}

	@Test
	public void testWiths()
	{
		BeanAllNullable b1 = buildBeanAllNullable().myStr( "a" ).build();

		// SUT
		BeanAllNullable b2 = b1.withMyStr( "b" );

		Assert.assertEquals( "a", b1.getMyStr() );
		Assert.assertEquals( "b", b2.getMyStr() );
	}

	@Test
	public void testTodoneer()
	{
		BeanAllNullable b1 = buildBeanAllNullable().myStr( "a" ).build();

		// SUT
		BeanAllNullable b2 = b1.update().myStr( "b" ).build();

		Assert.assertEquals( "a", b1.getMyStr() );
		Assert.assertEquals( "b", b2.getMyStr() );
	}

	@Test
	public void testToString()
	{
		Assert.assertEquals( "BeanAllNullable{MyInt=1, MyStr=a, MyBool=false}",
				BeanAllNullable.newBeanAllNullable( 1, "a", false ).toString() );
	}

	@Test
	public void testHashcode()
	{
		Assert.assertEquals( 34996, BeanAllNullable.newBeanAllNullable( 1, "a", false )
				.hashCode() );
	}
}
