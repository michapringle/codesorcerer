package ca.pandp.template;

import javax.annotation.Nonnull;

import ca.pandp.builder.Bean;
import ca.pandp.template.BNN_SBNN.Simple2Def;

@Bean
public interface Complex2Def
{
	@Nonnull
	public Simple2Def getSimple2();
}
