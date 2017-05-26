package com.test;

import com.test.template.BeanAllNullable;
import org.junit.Assert;
import org.junit.Test;

import static com.test.template.BeanAllNullable.buildBeanAllNullable;
import static com.test.template.BeanAllNullable.newBeanAllNullable;
import static com.test.template.BeanAllNullableGuava.ORDER_BY_MYINT;
import static com.test.template.BeanAllNullableGuava.ORDER_BY_MYSTR;
import static com.test.template.BeanAllNullableGuava.TO_MYINT;
import static com.test.template.BeanAllNullableGuava.TO_MYSTR;
import static com.test.template.BeanAllNullableGuava.byMyInt;
import static com.test.template.BeanAllNullableGuava.byMyStr;

public class TestNullableStuff
{

	/**
	 * If this compiles, then its probably ok!
	 */
	@Test
	public void testAPI()
	{
		ORDER_BY_MYINT.reverse();
		ORDER_BY_MYSTR.reverse();
		TO_MYINT.apply( null );
		TO_MYSTR.apply( null );
		byMyInt( 1 );
		byMyStr( "a" );
	}

	@Test
	public void testCopy()
	{
		BeanAllNullable b1 = newBeanAllNullable( 1, "a", false );
		BeanAllNullable b2 = b1.update().myStr( "b" ).build();
		Assert.assertEquals( "b", b2.getMyStr() );
		Assert.assertEquals( new Integer( 1 ), b2.getMyInt() );
	}

	@Test
	public void test()
	{
		BeanAllNullable b1 = buildBeanAllNullable().build();
		Assert.assertNull( b1.getMyStr() );
		Assert.assertNull( b1.getMyInt() );
	}

	@Test
	public void testWithAllParams()
	{
		BeanAllNullable b1 = newBeanAllNullable( 1, "a", false );
		Assert.assertEquals( "a", b1.getMyStr() );
		Assert.assertEquals( new Integer( 1 ), b1.getMyInt() );
	}

	@Test
	public void testWithAll()
	{
		BeanAllNullable b1 = buildBeanAllNullable().myStr( "a" ).build();
		Assert.assertEquals( "a", b1.getMyStr() );
		Assert.assertNull( b1.getMyInt() );
	}
}
