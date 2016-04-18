# BeanBuilder
## Introduction
Welcome to BeanBuilder, a tool to build fluent immutable beans from templates.

## Table of Contents
- Motivation
  - Advantages of BeanBuilder Objects
  - Disadvantages of BeanBuilder Objects
- Usage
- Getting started
  - With Maven
  - With Gradle
- Tutorial
  - What Is Generated
  - Creating a Simple Bean
  - Creating a Complex Bean
  - Creating an Inheritance Hierarchy
  - Creating a Cyclic Bean
  - Updating an Existing Instance
  - Adding Custom Code
  - Using Javax Validation Annotations
  - Using Guava Predicates
  - Using Guava Functions
  - Using Guava Equivalence
  - Using Guava Orderings
- Alternative Tools
- Authors  


## Motivation
There are numerous advantages to writing immutable classes, but apart from the simplest classes, implementation requires a lot of code, and for complex classes, updates are verbose. The result is a low signal-to-noise ratio, which is really a nuisance when you are being conscientious and following best practices. This tool allows one to define a template for a bean, and then generates an immutable implementation with a fluent API.

### Advantages of BeanBuilder Objects
- Requires very little code to define a bean.
- Fast implementation (no reflection or synchronization).
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

### Disadvantages of BeanBuilder Objects
- They require a separate object for each distinct value. (EJ Item 15)

## Usage
This tool is intended for generating implementations of beans, pojo's, or data classes. It can be used for simple [value classes] (https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html), it should not be used for service classes, classes designed to provide business logic or algorithms, or any other type of class that is not a (mostly) pure data object.
  
## Getting started
### With Maven
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
              <processor>ca.pandp.processor.BeanProcessor</processor>
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

### With Gradle
This is not supported yet. Any takers?

## Tutorial
This section has a continuing example to show how to use this tool. The examples in this tutorial can be cut/paste into your own test project, and run to test the tool. It is recommended to read this section once in its entirety, and in future refer to the sections of interest directly.

### What Is Generated
The bean builder generates several classes based on your template. Suppose you name your template FooDef. Then the  following classes are generated:
- Foo - This is the immutable implementation from the template.
- FooMutable - This is a mutable implementation from the template, so you can play nice with frameworks that require mutable beans. Methods are provided in Foo to easily convert to and from FooMutable.
- FooGuava - Stuck on Java7 or under? This class provides pseudo-functional code a la Google Guava.

### Creating a Simple Bean
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
There are several requirements here to make this class function as expected. David uses an inteface to define the **getters** for his new class. The interface must be annotated with the `@BeanTemplate` annotation, and the name of the interface must end with Def. Every method that has a required argument in the implementation must be annotated with @javax.annotation.Nonnull. 

Once David completes his template, he compiles the code, and can use the generated implementation. He can use a static factory that requires all fields to be supplied. He can do this because the `PersonDef` has 3 getter methods defined. With 4 or more getters, the static factory method is not available. This choice is appropriate when all fields are present.
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

David could use the fluent interface to create Bob Ross, the static factory is the recommended usage.
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

### Creating a Complex Bean
David decides that he wants to extract the name to a composed bean (sub-bean), add an `Address` sub-bean, add a `Sex` represented by an enum (Male, Female), and an occupation. He updates his `PersonDef` template as below.
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
Notice that sub-beans that are built using this tool must be referred to by the template name, for example, `NameDef` instead of `Name`.

Since there are more than 3 parameters, the tool allows only 1 way to create a `new Person`.
```java
final Person p = Person.buildPerson()
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

### Creating an Inheritance Hierarchy
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
    
    List<? extends ChildDef> getChildren();
}
```
David has removed the `@BeanTemplate` annotation from the `PersonDef`, and turned it into a `Person` interface. He has created an `AdultDef` and `ChildDef` that are BeanTemplates. Notice the return type of getChildren is `List<? extends ChildDef>`. This is necessary since `getChildren()` is a producer (PECS) of ChildDef's.

David can fluently create a `Child` or `Adult`. This principle applies no matter how deeply nested the hierarchy of BeanTemplates is.
```java
final Adult a = Adult.buildAdult()
                .newName()
                    .firstName("William")
                    .lastName("Shakespeare")
                .done()
                .newAddress()
                    .streetAddress("Stratford-upon-Avon, Warwickshire, West Midlands, England")
                .done()
                .sex(Sex.MALE)
                .children(Lists.newArrayList(susanna, hamnet, judith))
            .build();
            
final Child c = Child.buildChild()
    .newName()
        .firstName( "Hamnet" )
        .lastName("Shakespeare")
    .done()
    .newAddress()
        .streetAddress("Stratford-upon-Avon, Warwickshire, West Midlands, England")
    .done()
    .sex(Sex.MALE)
    .newMother()
        .newName()
            .firstName("Anne")
            .lastName("Hathaway")
        .done()
        .newAddress()
            .streetAddress("Stratford-upon-Avon, Warwickshire, West Midlands, England")
        .done()
        .sex(Sex.FEMALE)
    .done()
    .newFather()
        .newName()
            .firstName("William")
            .lastName("Shakespeare")
        .done()
        .newAddress()
            .streetAddress("Stratford-upon-Avon, Warwickshire, West Midlands, England")
        .done()
        .sex(Sex.MALE)
    .done()
.build();          
```
As expected, David cannot create a `Person` given the current class definitions. If David wanted to allow `Person` instances to exist, he could have kept the `Person` interface as a `@BeanTemplate`.

Although inheritance is supported, it is recommended to use composition over inheritance. Modelling Adult/Children as in the above example is not recommended.

## Creating a Cyclic Bean
**Never create a `@Nonnull` cycle in your `@BeanTemplate`.**
```java
@BeanTemplate
public interface CycleDef
{
    @Nonnull
    public CycleDef getCycle();
}

// needs an already existing cycle instance to complete
final Cycle c = Cycle.buildCycle()
                    .cycle(alreadyExistingCycleInstance)

// needs an already existing cycle instance to complete
final Cycle c = Cycle.buildCycle()
                    .newCycle()
                        .cycle(alreadyExistingCycleInstance)
										
// needs an already existing cycle instance to complete
final Cycle cycle = Cycle.newCycle(alreadyExistingCycleInstance);
```
If you do this, then then you cannot use the `buildCycle()` method without triggering an infinite cascade of `buildCycle()` calls. You can still use the `newCycle()` method, provided you have an existing `Cycle` instance, but how can you create it?

## Updating an Existing Instance
David decides he wants to update the child's mother's middle name. He starts with the update method, to indicate to the tool that he is interested in creating an altered copy of the original child.
```java
final Child child = c.update()
                .getMother()
                    .middleName("Mary")
                .done()
            .build();
```

When David has only 1 field to update, and an already existing Mother instance, he can use the `withMother()` method which provides the most concise syntax.
```java
final Child updatedChild = child.withMother(a);
```

**Think carefully before you do this.** If you update a sub-bean instead of the current instance, you are probably not doing what you want, and confusing the next developer.
```java
final Adult adult = child.getMother()
                .update()
                    .middleName( "Mary" )
                .build();
```
Notice here how the adult was updated and the updated copy returned. The child class is unaffected. This usage is not recommended.

Be aware of the implication of chaining `with...()` methods. The `with...()` method is shorthand for `update(). ... .build()`. Each time `with...()` is called, the entire instance is copied to a new instance with the single change.

This style results in 4 copies of `Child` instance child being created. While it is legible, it is not performant.
```java
final Adult adult = child.getMother().withMiddleName("Mary");
final Child avoid = child.withMother(adult).withFirstName("David" ).withMiddleName( "Bruce").withLastName( "Banner" );
```

This style results in 2 copies of the `Child` instance being created. Consider that this syntax is potentially confusing.
```java
final Adult adult = child.getMother().withMiddleName("Mary");
final Child idiom2 = child.withMother(adult)
        .update()
            .firstName("David")
            .middleName("Bruce")
            .lastName("Banner")
        .build();	
```

This style results in 1 copy of the `Adult` instance being created, and 1 copy of the `Child` instance being created. The syntax here is clear.
```java
final Child idiom1 = child.update()
        .getMother()
            .middleName("Mary")
        .done()
        .firstName("David")
        .middleName("Bruce")
        .LastName("Banner")
    .build();
```
This is the recommended syntax.

## Adding Custom Code
David decides to define some special behavior for his bean. He can do this by creating an abstract class with the custom implementation, instead of starting with an interface. Most methods defined in the abstract class are inherited by the generated implementation.
```java
public abstract class PersonDef {
    @Nonnull
    public abstract NameDef getName();

    @Nonnull
    public abstract AddressDef getAddress();

    @Nonnull
    public abstract Sex getSex();

    public abstract String getOccupation();

    public abstract long getBirthDate();

    public void sayHi() {
        System.out.println("Hi " + getName());
    }
}

@BeanTemplate
public abstract class AdultDef extends PersonDef
{
    public abstract String getOccupation();

    public abstract List<? extends ChildDef> getChildren();
}

@BeanTemplate
public abstract class ChildDef extends PersonDef
{
    @Nonnull
    public abstract AdultDef getMother();

    @Nonnull
    public abstract AdultDef getFather();

    public abstract int getCavities();
}
```
Notice this approach has the usual drawbacks of using an abstract class instead of an interface. As expected, David cannot create a `Person` given the current class definitions.

David now has access to a method called `sayHi()`.
```java
final Adult adult = Adult.buildAdult()
// rest of adult omitted 

adult.sayHello();
```

**Do not try to customize Object override methods**. Methods like `equals(Object o)`, `hashcode()` or `toString()` are never inherited, instead they are overridden by the custom implementation.
```java
@BeanTemplate
public abstract class IdDef
{
    @Nonnull
    public abstract Long getId();
    
    @Override
    public int hashCode()
    {
        return 42; 
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        return true;
    }
    
    @Override
    public String toString()
    {
        return "The Answer to the Ultimate Question of Life, the Universe, and Everything."
    } 
}
```
The custom implementations of the `@Override` methods are ignored by the tool, thus the output is not what one expects given this class definition.

## Using Javax Validation Annotations
The tool incorporates [validation annotations] (http://docs.oracle.com/javaee/6/api/javax/validation/package-summary.html). A trivial example is included here.

David decides a child has 0-40 cavities, and that the mother and father cannot be the same person.
```java
@BeanTemplate
public abstract class ChildDef implements PersonDef
{
    @Nonnull
    public abstract AdultDef getMother();

    @Nonnull
    public abstract AdultDef getFather();

    @Min(0)
    @Max(40)
    public abstract int getCavities();

    @AssertTrue(message="Class invariant violation : Mother and father cannot be the same person")
    private boolean isValid()
    {
        return !getMother().equals(getFather());
    }
}
```
The `isValid()` method is automatically called at construction time in the implementation, and therefore well suited to protect the invariants of this class.
 
David can now be sure that each child instance has 0-40 cavities. Instances c1 and c4 fail at runtime.
```java
final Child c1 = c.withCavities(-1);
final Child c2 = c.withCavities(0);
final Child c3 = c.withCavities(40);
final Child c4 = c.withCavities(41);
```
 
## Using Guava Predicates
The tool creates a separate class with static [Predicate methods] (https://code.google.com/p/guava-libraries/wiki/FunctionalExplained#Predicates). A trivial example is included here.

David can use a generated predicate to find all children with exactly 5 cavities. The predicate is created as a static method in a `ChildGuava` class.
```java
final List<Child> childList = Lists.newArrayList(
        c.update().cavities(3).sex(Sex.MALE).build(),
        c.update().cavities(2).sex(Sex.MALE).build(),
        c.update().cavities(0).sex(Sex.FEMALE).build(),
        c.update().cavities(2).sex(Sex.FEMALE).build());

final List<Child> filteredList = Lists.newArrayList(Iterables.filter(childList, ChildGuava.byCavities(5)));
```

## Using Guava Functions
The tool creates a separate class with static [Function methods] (https://code.google.com/p/guava-libraries/wiki/FunctionalExplained#Functions). A trivial example is included here.

David can use a function to transform the list of children into a list of number-of-cavities. The function is created as a static method in a `ChildGuava` class.
```java
final List<Child> childList = Lists.newArrayList(
        c.update().cavities(3).sex(Sex.MALE).build(),
        c.update().cavities(2).sex(Sex.MALE).build(),
        c.update().cavities(0).sex(Sex.FEMALE).build(),
        c.update().cavities(2).sex(Sex.FEMALE).build());

final List<Long> numberOfCavitiesList = Lists.newArrayList(Iterables.transform(childList, ChildGuava.BY_CAVITIES));
```

## Using Guava Equivalence
The tool creates a separate class with static [Equivalence] (http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Equivalence.html) and [Wrapper] (http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Equivalence.Wrapper.html). A trivial example is included here.

David can use any of the defined equivalences in place of `equals()` and `hashcode()`.
```java
final List<Child> childList = Lists.newArrayList(
        c.update().cavities(3).sex(Sex.MALE).build(),
        c.update().cavities(2).sex(Sex.MALE).build(),
        c.update().cavities(0).sex(Sex.FEMALE).build(),
        c.update().cavities(2).sex(Sex.FEMALE).build());

// output is 3, because 2 children have 2 cavities.
System.out.println(Sets.newHashSet(Iterables.transform(childList, ChildGuava.EQUALS_CAVITIES_WRAPPER)).size());

// output is 2, because 2 children are male, and 2 are female.
System.out.println(Sets.newHashSet(Iterables.transform(childList, ChildGuava.EQUALS_SEX_WRAPPER)).size());
```

## Using Guava Orderings
The tool creates a separate class with static [Ordering methods] (https://code.google.com/p/guava-libraries/wiki/OrderingExplained). A trivial example is included here.

David can sort or order lists. His `Ordering` is like a fluent replacement for `Comparator`.
```java
final List<Child> childList = Lists.newArrayList(
        c.update().cavities(3).sex(Sex.MALE).build(),
        c.update().cavities(2).sex(Sex.MALE).build(),
        c.update().cavities(0).sex(Sex.FEMALE).build(),
        c.update().cavities(2).sex(Sex.FEMALE).build());

// prints the children sorted by cavities, smallest to largest.
System.out.println(ChildGuava.ORDER_BY_CAVITIES.sortedCopy(childList));

// prints the children sorted by sex, male, then female.
System.out.println(ChildGuava.ORDER_BY_SEX .sortedCopy(childList));
```

## Alternative Tools
[Lombok] (https://projectlombok.org)

## Wish list
- Add gradle support.
- Methods that return a `List` should have a `...` setter.
- Rewrite in pure Java.
- Updates should allow updating any method without required orderings, since required fields already exist for the instance.

## Authors (in alphabetical order)
- David P Phillips
- Micha J Pringle
