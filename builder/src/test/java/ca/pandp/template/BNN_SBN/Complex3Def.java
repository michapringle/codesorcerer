package ca.pandp.template.BNN_SBN;

import javax.annotation.Nonnull;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface Complex3Def
{
	@Nonnull
	public Simple3Def getSimple3();
}
