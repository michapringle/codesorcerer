package ca.pandp;

import ca.pandp.template.SmallBean;
import org.junit.Test;

public class TestSmallBean
{

	@Test
	public void testInlineNew()
	{
		SmallBean.newSmallBean("hi");
	}

}
