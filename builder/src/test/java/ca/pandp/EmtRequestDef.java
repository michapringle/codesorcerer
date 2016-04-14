package ca.pandp;

import java.io.Serializable;

import javax.annotation.Nonnull;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface EmtRequestDef extends Serializable
{
	@Nonnull
	String getRequest();

	@Nonnull
	InteracOnlineDef getInteracOnline();
}
