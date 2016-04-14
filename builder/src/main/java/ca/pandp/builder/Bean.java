package ca.pandp.builder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.SOURCE )
@Target( java.lang.annotation.ElementType.TYPE )
public @interface Bean
{
}
