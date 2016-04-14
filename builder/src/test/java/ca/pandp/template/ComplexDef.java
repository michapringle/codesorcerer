package ca.pandp.template;

import javax.annotation.Nonnull;

import ca.pandp.builder.BeanTemplate;
import ca.pandp.template.BNN_SBNN.Simple2Def;

@BeanTemplate
public interface ComplexDef
{
	@Nonnull
	public String getTitle1();

	@Nonnull
	public String getTitle2();

	@Nonnull
	public String getTitle3();

	public String getTitle4();

	public String getTitle5();

	public String getTitle6();

	public SimpleDef getSimple1();

	public Simple2Def getSimple21();

	public SimpleDef getSimple2();

	public ThingDef getThing();
}
