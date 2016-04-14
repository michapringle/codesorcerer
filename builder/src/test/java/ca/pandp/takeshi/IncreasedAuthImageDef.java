package ca.pandp.takeshi;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface IncreasedAuthImageDef {
	String getImageId();

	String getData();

	String getAltText();

	String getPath();

	String getHeight();

	String getWidth();
}
