package ca.pandp;

import javax.annotation.Nonnull;

import ca.pandp.builder.Bean;

@Bean
public interface InteracOnlineDef {
	@Nonnull
	String getFiId();

	@Nonnull
	String getFiUserId();

	String getContactId();

}
