package com.test;

import org.junit.Assert;
import org.junit.Test;

import com.test.takeshi.IncreasedAuthData;


public class TestTakeshi {

	@Test
	public void test1() throws Exception{
		IncreasedAuthData x = IncreasedAuthData.buildIncreasedAuthData()
				.build();
		
		String returnType = x.getClass().getMethod("getChallengeQuestions").getGenericReturnType().toString(); 
		Assert.assertTrue(returnType.contains("List"));
		Assert.assertTrue(returnType.contains("IncreasedAuthChallengeQuestion"));
		Assert.assertFalse(returnType.contains("Def"));
	}
}