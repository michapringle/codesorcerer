# BeanBuilder
## Introduction
Welcome to BeanBuilder, a tool to build fluent immutable beans from templates.

## Motivation
There are numerous advantages to writing immutable classes, but apart from the simplest classes, implementation
requires a lot of code, and for complex classes, updates are verbose. The result is a low signal-to-noise ratio, which
is really a nuisance when you are being conscientious and following best practices. This tool allows one use an
interface to define a template for a bean, and then generates an immutable implementation with a fluent API.

**1 Advantages of BeanBuilder Objects**
- Requires very little code to define a bean.
- Fast implementation (no reflection).
- They are immutable.
  - Immutable objects are simple (EJ Item 15)
  - Immutable objects are inherently thread-safe; they require no synchronization. (EJ Item 15)
  - Immutable objects can be shared freely. (EJ Item 15)
  - The internals of immutable objects can be shared freely. (EJ Item 15)
  - Immutable objects make great building blocks for other objects (EJ Item 15)
- Create new immutable objects fluently.
- Create updated copies of immutable objects fluently.
- Support inheritance of immutable objects.
- Support for composed immutable objects.
- Eliminates accidental setting of required fields more than once.
- Support for custom methods.
- Support for [Javax validation] (http://docs.oracle.com/javaee/6/api/javax/validation/package-summary.html).
- Support for [Guava equivalence] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).
- Support for [Guava predicates] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).
- Support for [Guava functions] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).
- Support for [Guava orderings] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).

**2 Disadvantages of BeanBuilder Objects**
- They require a separate object for each distinct value. (EJ Item 15)

## Usage
This tool is intended for generating implementations of beans, pojo's, or data classes. It can be used for simple [value
 classes] (https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html), it should not be used for
 service classes, classes designed to provide business logic or algorithms, or any other type of class that is 
  not a (mostly) pure data object.
  
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

This additional configuration may not be necessary. Need to investigate.
```
<build>
   <plugins>
    <plugin>
      <groupId>org.bsc.maven</groupId>
      <artifactId>maven-processor-plugin</artifactId>
      <version>2.2.4</version>
      <configuration>
         <failOnError>false</failOnError>
         <outputDiagnostics>false</outputDiagnostics>
         <processors>
              <processor>com.central1.beautifulbeanbuilder.BeanProcessor</processor>
         </processors>
         <includes><include>**/*Def.java</include></includes>
      </configuration>
      <executions>
        <execution>
          <id>process</id>
          <goals>
            <goal>process</goal>
          </goals>
          <phase>generate-sources</phase>
        </execution>
        <execution>
          <id>process-test</id>
          <goals>
            <goal>process-test</goal>
          </goals>
          <phase>generate-test-sources</phase>
        </execution>
      </executions>
    </plugin>
    <plugin>
       <artifactId>maven-compiler-plugin</artifactId>
       <version>2.3.2</version>
       <configuration>
           <compilerArgument>-proc:none</compilerArgument>
       </configuration>
    </plugin>
  </plugins>
</build>
```

### Getting started with Gradle
This is not supported yet. Any takers?

### What Is Generated
The bean builder generates several classes based on your template. Suppose you name your template FooDef. Then the 
following classes are generated:
- Foo - This is the immutable implementation from the template
- FooMutable - This is a mutable implementation from the template, so you can play nice with frameworks that require mutable beans
- FooGuava - Stuck on Java7 or under? This class provides pseudo-functional code a la Google Guava
