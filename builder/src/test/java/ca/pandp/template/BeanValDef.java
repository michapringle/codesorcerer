package ca.pandp.template;

import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface BeanValDef
{
	@Max( 100 )
	Integer getMyIntMax100();

	@Pattern( regexp = "\\d\\w" )
	String getMyStringPattern();

	@NotNull
	Integer getMyIntNotNull();

	Boolean getMyBool();
	
	int getIntShouldBeOrderable();	
	byte getByteShouldBeOrderable();
	Byte getByte2ShouldBeOrderable();
	Date getDateShouldBeOrderable();
	
	Object getObjectShouldNOTBeOrderable();
}
