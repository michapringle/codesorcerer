# BeanBuilder
## Introduction
Welcome to BeanBuilder, a tool to build fluent immutable beans from templates.

## Motivation
There are numerous advantages to writing immutable classes, but apart from the simplest classes, implementation
requires a lot of code, and for complex classes, updates are verbose. The result is a low signal-to-noise ratio, which
is really a nuisance when you are being conscientious and following best practices. This tool allows one use an
interface to define a template for a bean, and then generates an immutable implementation with a fluent API.

**1 Advantages of Immutable Objects**
- Immutable objects are simple (EJ Item 15)
- Immutable objects are inherently thread-safe; they require no synchronization. (EJ Item 15)
- Immutable objects can be shared freely. (EJ Item 15)
- The internals of immutable objects can be shared freely. (EJ Item 15)
- Immutable objects make great building blocks for other objects (EJ Item 15)

**2 Disadvantages of Immutable Objects**
- They require a separate object for each distinct value. (EJ Item 15)
- Implementing immutable objects can require a lot of code.
- Instantiating an new instance of an immutable class can require a lot of code.
- Updating an existing instance of an immutable class can require a lot of code.

This tool provides all the listed advantages, and eliminates the last 3 disadvantages.

## Usage
This tool is intended for generating implementations of beans, pojo's, or data classes. It can be used for simple [value
 classes] (https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html), it should not be used for
 service classes, classes designed to provide business logic, or algorithms, or any other type of class that is 
  designed as a pure data object.
  
### Getting started with Maven
Include the following dependencies in your project. When you deploy your artifact(s), the actual overhead of the 
BeanBuilder jar is about 4k.

``` 
<dependency>
   <groupId>ca.pandp</groupId>
   <artifactId>builder</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>
 
<dependency>
    <groupId>ca.pandp</groupId>
    <artifactId>processor</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

### Getting started with Gradle
This is not supported yet. Any takers?

### What Is Generated
The bean builder generates several classes based on your template. Suppose you name your template FooDef. Then the 
following classes are generated:
- Foo (this is the immutable implementation from the template)
- FooMutable (this is a mutable implementation from the template, so you can play nice with frameworks that require mutable beans)
- FooGuava (stuck on Java7 or under? This class provides pseudo-functional code a la Google Guava)
