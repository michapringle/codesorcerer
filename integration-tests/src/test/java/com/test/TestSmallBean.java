package com.test;

import com.test.template.SmallBean;
import org.junit.Test;

public class TestSmallBean
{

	@Test
	public void testInlineNew()
	{
		SmallBean.newSmallBean("hi");
	}

}
