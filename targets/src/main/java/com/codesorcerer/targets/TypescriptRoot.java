package com.codesorcerer.targets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public @interface TypescriptRoot {
    String version() default "1.0.0";
}
