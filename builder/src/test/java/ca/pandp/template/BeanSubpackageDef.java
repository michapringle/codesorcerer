package ca.pandp.template;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface BeanSubpackageDef
{
	public static final BeanSubpackage X = BeanSubpackage.newBeanSubpackage( "xx" );

	String getThing();
}
