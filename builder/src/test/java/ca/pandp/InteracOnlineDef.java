package ca.pandp;

import javax.annotation.Nonnull;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface InteracOnlineDef {
	@Nonnull
	String getFiId();

	@Nonnull
	String getFiUserId();

	String getContactId();

}
