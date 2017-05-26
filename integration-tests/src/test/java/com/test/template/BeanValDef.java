package com.test.template;

import com.codesorcerer.targets.BeautifulBean;

import java.util.Date;

//import javax.validation.constraints.NotNull;

@BeautifulBean
public interface BeanValDef
{
//	@Max( 100 )
	Integer getMyIntMax100();

//	@Pattern( regexp = "\\d\\w" )
	String getMyStringPattern();

	//@NotNull
	Integer getMyIntNotNull();

	Boolean getMyBool();
	
	int getIntShouldBeOrderable();	
	byte getByteShouldBeOrderable();
	Byte getByte2ShouldBeOrderable();
	Date getDateShouldBeOrderable();
	
	Object getObjectShouldNOTBeOrderable();
}
