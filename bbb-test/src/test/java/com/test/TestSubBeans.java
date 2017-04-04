package com.test;

import org.junit.Test;

import com.test.template.Complex;
import com.test.template.David;

public class TestSubBeans
{
	@Test
	public void testSubbeansShouldCompile()
	{
		// @formatter:off
		Complex c = Complex.buildComplex()
                    .title1("")
                    .title2("")
                    .title3("")
                    .newSimple1()
                        .name("")
                    .done()
                    .newSimple2()
                        .name("")
                    .done()
                    .newThing()
                        .that("")
                    .done()
    				.title4("")
				.build();

		c.update()
                .title2("1")
                .getSimple1()
                    .name("1")
                 .done()
             .build();

        c.update().title3("").build();

		David d = David.buildDavid()
				.newSimple()
			    	.name("")
				.done()
				.title1( "" )
			.build();

		David d2 = d.update()
				.newSimple()
			    	.name("")
				.done()
			.build();
        // @formatter:on
	}
}
