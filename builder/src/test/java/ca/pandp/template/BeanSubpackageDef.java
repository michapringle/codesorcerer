package ca.pandp.template;

import ca.pandp.builder.Bean;

@Bean
public interface BeanSubpackageDef
{
	public static final BeanSubpackage X = BeanSubpackage.newBeanSubpackage( "xx" );

	String getThing();
}
