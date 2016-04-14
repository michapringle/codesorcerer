package ca.pandp.template.BNN_SBNN;

import javax.annotation.Nonnull;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface Complex2Def
{
	@Nonnull
	public Simple2Def getSimple2();
}
