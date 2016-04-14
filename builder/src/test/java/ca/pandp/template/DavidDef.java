package ca.pandp.template;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ca.pandp.bean.sub.FooDef;
import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface DavidDef
{
	@Nonnull
	SimpleDef getSimple();

	String getTitle1();

	String getTitle2();
	
	Map<? extends FooDef, ? extends List<? extends Map<? extends FooDef, ? extends FooDef>>> getFoos();
}
