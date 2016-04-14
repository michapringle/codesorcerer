package ca.pandp.takeshi;

import ca.pandp.builder.Bean;

@Bean
public interface IncreasedAuthImageDef {
	String getImageId();

	String getData();

	String getAltText();

	String getPath();

	String getHeight();

	String getWidth();
}
