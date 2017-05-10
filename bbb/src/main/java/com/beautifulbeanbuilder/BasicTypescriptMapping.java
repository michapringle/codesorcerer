package com.beautifulbeanbuilder;

//Primitives
@TypescriptMapping(javaClassName = "int", typescriptClassName = "number")
@TypescriptMapping(javaClassName = "long", typescriptClassName = "number")
@TypescriptMapping(javaClassName = "boolean", typescriptClassName = "boolean")

//Basic Java types
@TypescriptMapping(javaClassName = "java.lang.String", typescriptClassName = "string")
@TypescriptMapping(javaClassName = "java.lang.Boolean", typescriptClassName = "boolean")
@TypescriptMapping(javaClassName = "java.lang.Integer", typescriptClassName = "number")
@TypescriptMapping(javaClassName = "java.lang.Long", typescriptClassName = "number")
@TypescriptMapping(javaClassName = "java.math.BigDecimal", typescriptClassName = "number")
@TypescriptMapping(javaClassName = "java.math.BigInteger", typescriptClassName = "number")

//Standard library types
@TypescriptMapping(javaClassName = "org.joda.money.Money", typescriptClassName = "string")
@TypescriptMapping(javaClassName = "io.reactivex.Observable", typescriptClassName = "Observable", typescriptImportLocation = "./rxjs")
@TypescriptMapping(javaClassName = "io.reactivex.Single", typescriptClassName = "Observable", typescriptImportLocation = "./rxjs")

public @interface BasicTypescriptMapping {
}
