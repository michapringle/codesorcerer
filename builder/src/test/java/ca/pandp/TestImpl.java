package ca.pandp;

import java.io.Serializable;

import ca.pandp.bean.sub.Person;
import ca.pandp.bean.sub.PersonMutable;
import ca.pandp.bean.sub.Sex;
import ca.pandp.template.BeanAllNullable;
import org.junit.Assert;
import org.junit.Test;

import static ca.pandp.template.BeanAllNullable.buildBeanAllNullable;

public class TestImpl
{
	@Test
	public void example()
	{
		Person p = Person.buildPerson()
				.newName()
					.firstName("Bob")
					.lastName("Ross")
				.done()
				.newAddress()
					.streetAddress("221B Baker Street")
				.done()
				.sex(Sex.MALE)
				.build();

		Person p2 = p.update()
				.newAddress()
					.streetAddress("221A Baker Street")
				.done()
				.build();
	}

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
