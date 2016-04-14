package ca.pandp;

import ca.pandp.template.BeanVal;
import org.junit.Assert;
import org.junit.Test;

import static ca.pandp.template.BeanVal.buildBeanVal;

public class TestValidationStuff
{

	private BeanVal okBean()
	{
		return buildBeanVal( ).myIntMax100( 1 ).myIntNotNull( 1 ).myStringPattern( "5d" ).build();
	}

	@Test
	public void testSuccess()
	{
		okBean();
	}

	@Test
	public void testMaxSuccess2()
	{
		okBean().withMyIntMax100( 100 );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testMaxFailure()
	{
		okBean().withMyIntMax100( 101 );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testPatternFailure()
	{
		okBean().withMyStringPattern( "aa" );
	}

	@Test
	public void testNull()
	{
		okBean().update().myIntNotNull( null );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testNullFailure()
	{
		okBean().withMyIntNotNull( null );
	}

	public void testClassLevelValidation()
	{
		try
		{
			okBean().withMyIntMax100( 50 );
			Assert.fail();
		}
		catch ( IllegalArgumentException e )
		{
			Assert.assertTrue( e.getMessage().contains( "Borked" ) );
		}
	}

}
