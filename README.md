# BeanBuilder
## Introduction
Welcome to BeanBuilder, a tool to build fluent immutable beans from templates.

## Motivation
There are numerous advantages to writing immutable classes, but apart from the simplest classes, implementation requires a lot of code, and for complex classes, updates are verbose. The result is a low signal-to-noise ratio, which is really a nuisance when you are being conscientious and following best practices. This tool allows one to define a template for a bean, and then generates an immutable implementation with a fluent API.

**1 Advantages of BeanBuilder Objects**
- Requires very little code to define a bean.
- Fast implementation (no reflection).
- Immutable.
  - Immutable objects are simple. (EJ Item 15)
  - Immutable objects are inherently thread-safe; they require no synchronization. (EJ Item 15)
  - Immutable objects can be shared freely. (EJ Item 15)
  - The internals of immutable objects can be shared freely. (EJ Item 15)
  - Immutable objects make great building blocks for other objects. (EJ Item 15)
- Create new immutable objects fluently.
- Create updated copies of immutable objects fluently.
- Support inheritance of immutable objects.
- Support for composed immutable objects.
- Enforces setting of required fields exactly once.
- Support for custom methods.
- Support for [Javax validation] (http://docs.oracle.com/javaee/6/api/javax/validation/package-summary.html).
- Support for [Guava equivalence] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).
- Support for [Guava predicates] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).
- Support for [Guava functions] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).
- Support for [Guava orderings] (http://docs.guava-libraries.googlecode.com/git/javadoc/index.html).

**2 Disadvantages of BeanBuilder Objects**
- They require a separate object for each distinct value. (EJ Item 15)

## Usage
This tool is intended for generating implementations of beans, pojo's, or data classes. It can be used for simple [value classes] (https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html), it should not be used for service classes, classes designed to provide business logic or algorithms, or any other type of class that is not a (mostly) pure data object.
  
### Getting started with Maven
Include the following dependencies in your project. When you deploy your artifact(s), the actual overhead of the BeanBuilder jar is about 4k.
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
The bean builder generates several classes based on your template. Suppose you name your template FooDef. Then the  following classes are generated:
- Foo - This is the immutable implementation from the template.
- FooMutable - This is a mutable implementation from the template, so you can play nice with frameworks that require mutable beans. Methods are provided in Foo to easily convert to and from FooMutable.
- FooGuava - Stuck on Java7 or under? This class provides pseudo-functional code a la Google Guava.

### Tutorial
This section has a continuing example to show how to use this tool. It is recommended to read it once in its entirety, and in future refer to the sections of interest directly.

#### Creating a simple bean
David requires a person class for a project he is working on. Initially his person is very simple, having only 3 fields. He chooses the BeanBuilder tool because he expects his class to get more complex as the project grows.
```java
@BeanTemplate
public interface PersonDef
{
    @Nonnull
    String getFirstName();
    
    String getMiddleName();

    @Nonnull
    String getLastName();
}
```
There are several requirements here to make this class function as expected. David uses an inteface to define the **getters** for his new class. The interface must be annotated with the @BeanTemplate annotation, and the name of the interface must end with Def. Every method that has a required argument in the implementation must be annotated with @javax.annotation.Nonnull. 

Once David completes his template, he compiles the code, and can use the generated implementation. He can use a static factory that requires all fields to be supplied. He can do this because the PersonDef has 3 getter methods defined. With 4 or more getters, the static factory method is not available. This choice is appropriate when all fields are present.
```java
final Person p = Person.newPerson( "Bob", "Rip", "Ross");
```
Notice that the order of the method arguments are as defined in the BeanTemplate.

David creates another person using the fluent interface. This is the right choice when the person has no middle name.
```java
final Person jd = Person.buildPerson()
                .firstName( "Judy" )
                .lastName( "Dench" )
             .build();
```
If you try this example, you will notice that the tool forces the @Nonnull fields to be specified first, in the order defined in the BeanTemplate.

David could use the fluent interface to create Bob Ross, we recommend the static factory.
```java
final Person p = Person.buildPerson()
                .firstName( "Bob" )
                .lastName( "Ross" )
                .middleName( "Rip" )
             .build();
```
Notice the middle name appears after all the required fields in the interface.

**Never add a Nonnull parameter more than once.** The BeanTemplate tool cannot control how many times you can add an optional parameter, therefore, you should be careful that you specify optional parameters only once.
```java
final Person p = Person.buildPerson()
                .firstName( "Bob" )
                .lastName( "Ross" )
                .middleName( "Rip" )
                .middleName( "Drat" )
             .build();
```

#### Creating a larger bean
David decides that he wants to extract the name to a composed bean (subbean), add an Address subbean, add a Sex represented by an enum (Male, Female), and an occupation. He updates his PersonDef template as below.
```java
@BeanTemplate
public interface PersonDef
{
    @Nonnull
    NameDef getName();

    @Nonnull
    AddressDef getAddress();

    @Nonnull
    Sex getSex();

    String getOccupation();
}
```
Notice that subbeans that are built using this tool must be referred to by the template name, for example, NameDef instead of Name.

Since there are more than 3 parameters, the tool allows only 1 way to create a new Person.
```java
Person p = Person.buildPerson()
				.newName()
					.firstName("Sherlocke")
					.lastName("Holmes")
				.done()
				.newAddress()
					.streetAddress("221B Baker Street")
				.done()
				.sex(Sex.MALE)
			.build();
```
Notice that the occupation is optional, and was not included in this instantiation.

#### Creating an inheritance hierarchy
David decides he wants to model adults and children differently. He refactors his code.
```java
public interface Person
{
     @Nonnull
     NameDef getName();
 
     @Nonnull
     AddressDef getAddress();
 
     @Nonnull
     Sex getSex();

}

@BeanTemplate
public interface ChildDef extends Person
{
    @Nonnull
    AdultDef getMother();
  
    @Nonnull
    AdultDef getFather();

    int getCavities();
}

@BeanTemplate
public interface AdultDef extends Person
{
    String getOccupation();
    
    List<? extends ChildDef> getChildren(); //We also need the ? extends syntax.
}
```
David has removed the @BeanTemplate annotation from the PersonDef, and turned it into a Person interface. He has created an AdultDef and ChildDef that are BeanTemplates. David can fluently create the child's mother. This principle applies no matter how deeply nested the hierarchy of BeanTemplates is.

As expected, David cannot create a Person given the current class definitions. If David wanted to allow Person instances to exist, he could have kept the Person interface as a BeanTemplate.

Notice the return type of getChildren is List<? extends ChildDef>. This is necessary since getChildren is a producer (PECS) of ChildDef's.
