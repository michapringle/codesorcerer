package ca.pandp;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static ca.pandp.template.BeanVal.buildBeanVal;
import static ca.pandp.template.BeanValGuava.EQUALS_MYBOOL;
import static ca.pandp.template.BeanValGuava.EQUALS_MYBOOL_WRAPPER;
import static ca.pandp.template.BeanValGuava.EQUALS_MYINTMAX100;
import static ca.pandp.template.BeanValGuava.ORDER_BY_MYINTMAX100;
import static ca.pandp.template.BeanValGuava.TO_MYINTMAX100;
import static ca.pandp.template.BeanValGuava.TO_MYSTRINGPATTERN;
import static ca.pandp.template.BeanValGuava.byMyIntMax100;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ca.pandp.bean.sub.FooGuava;
import ca.pandp.template.BeanVal;
import ca.pandp.template.BeanValGuava;
import ca.pandp.template.Complex;
import ca.pandp.template.ComplexGuava;
import ca.pandp.template.David;
import ca.pandp.template.Special;

import ca.pandp.template.BeanVal;
import ca.pandp.template.BeanValGuava;
import ca.pandp.template.Complex;
import ca.pandp.template.ComplexGuava;
import ca.pandp.template.David;

public class TestGuava
{

	private BeanVal okBean()
	{
		return buildBeanVal().myIntMax100( 1 ).myIntNotNull( 1 ).build();
	}

	@Test
	public void testOrdering()
	{
		
		Complex c = Complex.buildComplex()
			.title1("")
			.title2("")
			.title3("")
			.newSimple1()
				.name("x")
			.done()
			.newThing()
				.that("")
			.done()
		.build();
		
		
		Complex c2 = c.update()
						.title1("x")
					  .build();
		
		ImmutableList<Complex> x = ImmutableList.of(c, c2);
		
		ImmutableList<String> xx = FluentIterable
									.from(x)
									.transform(ComplexGuava.TO_TITLE1)
									.toList();
		
		David d = David.buildDavid().newSimple()
				.done()
				.build();
		d.getFoos();
		
		
		ImmutableMap<String, Complex> xxx = Maps.uniqueIndex(x, ComplexGuava.BY_TITLE1);
		
		c.update()
			.getSimple1()
				.name("y")
			.done()
		.build();
		
		
		
		
		BeanVal b1 = okBean();
		BeanVal b2 = okBean();
		BeanVal b2prime = okBean().withMyIntMax100( 2 );

		Assert.assertTrue( b1.equals( b2 ) );
		Assert.assertEquals( 0, ORDER_BY_MYINTMAX100.compare( b1, b2 ) );
		Assert.assertEquals( -1, ORDER_BY_MYINTMAX100.compare( b1, b2prime ) );
		Assert.assertEquals( 1, ORDER_BY_MYINTMAX100.compare( b2prime, b1 ) );
		Assert.assertEquals( -1, ORDER_BY_MYINTMAX100.compare( b1, null ) );
		Assert.assertEquals( 1, ORDER_BY_MYINTMAX100.compare( null, b1 ) );
		
		BeanValGuava.ORDER_BY_INTSHOULDBEORDERABLE.compare(null, null);
		BeanValGuava.ORDER_BY_BYTE2SHOULDBEORDERABLE.compare(null, null);
		BeanValGuava.ORDER_BY_BYTESHOULDBEORDERABLE.compare(null, null);
		BeanValGuava.ORDER_BY_DATESHOULDBEORDERABLE.compare(null, null);		
	}

	@Test
	public void testTo()
	{
		BeanVal b1 = okBean();

		Assert.assertNull( TO_MYSTRINGPATTERN.apply( b1 ) );
		Assert.assertEquals( 1, TO_MYINTMAX100.apply( b1 ).intValue() );
		Assert.assertNull( TO_MYINTMAX100.apply( null ) );
	}

	@Test
	public void testBy()
	{
		BeanVal b1 = okBean();

		Assert.assertTrue( byMyIntMax100( 1 ).apply( b1 ) );
		Assert.assertFalse( byMyIntMax100( 2 ).apply( b1 ) );
		Assert.assertFalse( byMyIntMax100( null ).apply( b1 ) );
	}

	@Test
	public void testByPredicate()
	{
		BeanVal b1 = okBean();
		BeanVal b2 = b1.withMyBool( true );

		b1.withMyBool( true );

		Assert.assertFalse( BeanValGuava.IS_MYBOOL.apply( null ) );
		Assert.assertFalse( BeanValGuava.IS_MYBOOL.apply( b1 ) );
		Assert.assertTrue( BeanValGuava.IS_MYBOOL.apply( b2 ) );
	}

	@Test
	public void testEquivalance()
	{
		BeanVal b1 = okBean();
		BeanVal b2 = b1.withMyIntMax100( 51 );
		BeanVal b3 = b1.withMyBool( true );

		Assert.assertTrue( EQUALS_MYBOOL.equivalent( b1, b2 ) );
		Assert.assertFalse( EQUALS_MYINTMAX100.equivalent( b1, b2 ) );
		Assert.assertFalse( EQUALS_MYBOOL.equivalent( b1, b3 ) );

		Set<Wrapper<BeanVal>> s = Sets.newHashSet();
		s.add( EQUALS_MYINTMAX100.wrap( b1 ) );
		s.add( EQUALS_MYINTMAX100.wrap( b2 ) );
		Assert.assertEquals( 2, s.size() );

		Set<Wrapper<BeanVal>> s2 = Sets.newHashSet();
		s2.add( EQUALS_MYBOOL.wrap( b1 ) );
		s2.add( EQUALS_MYBOOL.wrap( b2 ) );
		Assert.assertEquals( 1, s2.size() );

		Set<BeanVal> bs = ImmutableSet.of( b1, b2, b3 );
		HashSet<Wrapper<BeanVal>> bs2 = newHashSet( transform( bs, EQUALS_MYBOOL_WRAPPER ) );
		Assert.assertEquals( 2, bs2.size() );

		HashSet<BeanVal> bs3 = newHashSet( Iterables.transform( bs2, BeanValGuava.EQUALS_UNWRAPPER ) );
		Assert.assertEquals( 2, bs3.size() );

	}

}
