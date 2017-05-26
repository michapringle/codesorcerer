package com.codesorcerer.targets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

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

@TypescriptMapping(javaClass = List.class, typescriptClassName = "Array")
@TypescriptMapping(javaClass = ArrayList.class, typescriptClassName = "Array")
@TypescriptMapping(javaClass = LinkedList.class, typescriptClassName = "Array")

@TypescriptMapping(javaClass = Map.class, typescriptClassName = "Map")
@TypescriptMapping(javaClass = HashMap.class, typescriptClassName = "Set")

@TypescriptMapping(javaClass = Set.class, typescriptClassName = "Set")
@TypescriptMapping(javaClass = HashSet.class, typescriptClassName = "Set")

public @interface BasicTypescriptMapping {
}
