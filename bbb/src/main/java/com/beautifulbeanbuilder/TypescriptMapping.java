package com.beautifulbeanbuilder;

import java.lang.annotation.*;

@Repeatable(TypescriptMappings.class)
public @interface TypescriptMapping {
        //This or that...
        String javaClassName() default "";
        Class javaClass() default void.class;

        String typescriptClassName();
        String typescriptImportLocation() default "";

}
