package com.beautifulbeanbuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

//Primitives
@TypescriptMapping(javaClass = int.class, typescriptClassName = "number")
@TypescriptMapping(javaClass = long.class, typescriptClassName = "number")
@TypescriptMapping(javaClass = boolean.class, typescriptClassName = "boolean")

//Basic Java types
@TypescriptMapping(javaClass = String.class, typescriptClassName = "string")
@TypescriptMapping(javaClass = Boolean.class, typescriptClassName = "boolean")
@TypescriptMapping(javaClass = Integer.class, typescriptClassName = "number")
@TypescriptMapping(javaClass = Long.class, typescriptClassName = "number")
@TypescriptMapping(javaClass = BigDecimal.class, typescriptClassName = "number")
@TypescriptMapping(javaClass = BigInteger.class, typescriptClassName = "number")

public @interface BasicTypescriptMapping {
}
