package com.beautifulbeanbuilder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.SOURCE )
@Target( java.lang.annotation.ElementType.TYPE )
public @interface BeautifulBean
{
    String[] plugins() default {
            "com.beautifulbeanbuilder.processor.builders.ImmutableBuilder"
    };

}
