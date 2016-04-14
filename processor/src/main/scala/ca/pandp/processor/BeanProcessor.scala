package ca.pandp.processor;

import java.io.PrintWriter
import java.util.Date
import javax.annotation.Nonnull
import javax.annotation.processing.{ AbstractProcessor, RoundEnvironment, SupportedAnnotationTypes, SupportedSourceVersion }
import javax.lang.model.SourceVersion
import javax.lang.model.`type`.{ DeclaredType, ExecutableType }
import javax.lang.model.element.{ Element, Modifier, TypeElement }
import javax.lang.model.util.SimpleTypeVisitor6
import scala.collection.JavaConversions._
import scala.util.Try
import javax.lang.model.`type`.ErrorType
import javax.tools.Diagnostic

@SupportedAnnotationTypes(Array("ca.pandp.builder.BeanTemplate"))
@SupportedSourceVersion(SourceVersion.RELEASE_6)
class BeanProcessor extends AbstractProcessor {

  val ANNOTATION_CLASS = "ca.pandp.builder.BeanTemplate"

  override def process(annotations: java.util.Set[_ <: TypeElement], env: RoundEnvironment): Boolean = {
    try {
      if (!env.processingOver()) {
        env.getRootElements()
          .filter(_.isInstanceOf[TypeElement])
          .map(_.asInstanceOf[TypeElement])
          .filter(hasBBAnnotation)
          .foreach { e =>
            Context.printHeader(e)
            processIt(e, env)
          }
      } else {
        Context.printFooter()
      }
      true
    } catch {
      case e: Throwable =>
        println("Dammit - BBB crashed!")
        e.printStackTrace()
        true
    }
  }

  def hasBBAnnotation(element: TypeElement): Boolean = {
    element
      .getAnnotationMirrors()
      .exists(_.getAnnotationType.toString == ANNOTATION_CLASS)
  }

case class info(
    prefix: String,
    nameUpper: String,
    name: String,
    nameMangled: String,
    returnTypeFQ: String,
    returnType: String,
    boxed: String,
    nonNull: Boolean,
    isAbstract: Boolean,
    isPrimitive: Boolean,
    isBB: Boolean,
    annotations: String,
    comparable: Boolean) {
    def field() = returnTypeFQ + " " + nameMangled
    def param() = (if (nonNull) "@Nonnull " else "") + field()
    def precond(className: String) = if (nonNull) s"""Preconditions.checkNotNull(${nameMangled}, "${className}.${name} cannot be null");""" else ""
  }

  def processIt(e: TypeElement, env: RoundEnvironment):Unit = {

    //Preconditions...
    if (!Context.interfaceName(e).endsWith("Def")) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Must end in Def", e)
    }

    //Must be comparable to be in map
    val boxMap = Map(
      ("int" -> "Integer"),
      ("long" -> "Long"),
      ("double" -> "Double"),
      ("float" -> "Float"),
      ("boolean" -> "Boolean"),
      ("char" -> "Character"),
      ("byte" -> "Byte"),
      ("void" -> "Void"),
      ("short" -> "Short"),
      ("string" -> "String"))

    val keywords = Set("abstract", "continue", "for", "new", "switch",
      "assert", "default", "goto", "package", "synchronized",
      "boolean", "do", "if", "private", "this",
      "break", "double", "implements", "protected", "throw",
      "byte", "else", "import", "public", "throws",
      "case", "enum", "instanceof", "return", "transient",
      "catch", "extends", "int", "short", "try",
      "char", "final", "interface", "static", "void",
      "class", "finally", "long", "strictfp", "volatile",
      "const", "float", "native", "super", "while")

    def fillInfo(t: Element) = {
      val ttt = t.getEnclosingElement().asInstanceOf[TypeElement]

      val beanPartsRegex = """(get|set|is)(.*)""".r
      t.getSimpleName match {
        case beanPartsRegex(prefix, nameUpper) =>
          val name = nameUpper.charAt(0).toLower + nameUpper.drop(1)
          val nonNull = t.getAnnotation(classOf[Nonnull]) != null

          val returnTypeVisitor = new SimpleTypeVisitor6[(TypeElement, String, Boolean, Integer), Object]() {
            override def visitExecutable(tt: ExecutableType, o: Object) = {
              val x = if (tt.getReturnType().isInstanceOf[DeclaredType]) {
                val dt = tt.getReturnType().asInstanceOf[DeclaredType]
                val te = dt.asElement.asInstanceOf[TypeElement]

                tt.getReturnType().asInstanceOf[DeclaredType].asElement.asInstanceOf[TypeElement]
              } else {
                null
              }

              (x, tt.getReturnType().toString, tt.getReturnType().getKind().isPrimitive, tt.getParameterTypes().size)
            }
          };

          val (tt, returnTypeFQWithDef, isPrimative, numParameters) = t.asType().accept(returnTypeVisitor, null)

           
          
          val returnTypeFQ = (if (returnTypeFQWithDef.endsWith("Def"))
            returnTypeFQWithDef
            .dropRight(3)
          else
            returnTypeFQWithDef)
            .replaceAll(".def.", ".")
            .replaceAll("\\? extends (.*?)Def", "$1")
            .replaceAll("(.*?)Def", "$1")
            

          val isBBx = returnTypeFQWithDef.endsWith("Def")

          val comparableType = processingEnv.getTypeUtils().getDeclaredType(
              processingEnv.getElementUtils().getTypeElement("java.lang.Comparable"),
              processingEnv.getTypeUtils().getWildcardType(null, null))
              
          //TODO: If it is a subbean and its not a Def, then its a compile error (same with generic lists/maps/etc)
          def printOutDefMissingErrors(s:String) = {
            val tps = s.split(Array('<', '>',',','[',']')).map(_.replaceAllLiterally("? extends ", "").trim).toList            
            tps.foreach{ ts =>
            	val t = processingEnv.getElementUtils().getTypeElement(ts)
            	if(t == null && !boxMap.contains(ts)) {
		            println("*         ERROR: " + s + " should reference the subbean's Def interface, not the generated class")            	  
            	}
            	
            	Try {            
		            val s = t.asType.getClass().toString()
		            val res = !s.contains("ErrorType")
			        if( !res) {
			            println("*         ERROR: " + t + " should reference the subbean's Def interface, not the generated class")
			        }
		            res	            
            	}.getOrElse(true)
          	}            
          }    
          
          printOutDefMissingErrors(returnTypeFQWithDef)
          
           
          val comparable = if (tt == null)
            (isPrimative && !isBBx)
          else !isBBx && processingEnv.getTypeUtils().isSubtype(tt.asType(), comparableType)


          
          val returnTypeFormatted = if (returnTypeFQ.startsWith("java.lang."))
            returnTypeFQ.drop(10)
          else
            returnTypeFQ
          val boxed = boxMap.get(returnTypeFormatted).getOrElse(returnTypeFormatted)
          val abstracts = isAbstract(t) && isPublic(t) && !isFinal(t) && (numParameters == 0)

          //Lets just do the mangling for all, regardless if its a keyword!!!
          val nameMangled = if (keywords.contains(name)) name + "_" else name

          val annotationString = "" //calcAnnotation(t.getAnnotationMirrors.toList)

          info(prefix, nameUpper, name, nameMangled, returnTypeFQ, returnTypeFormatted, boxed, nonNull, abstracts, isPrimative, isBBx, annotationString, comparable)
      }
    }

    def isAbstract(e: Element) = e.getModifiers().contains(Modifier.ABSTRACT);
    def isPublic(e: Element) = e.getModifiers.contains(Modifier.PUBLIC);
    def isFinal(e: Element) = e.getModifiers.contains(Modifier.FINAL);
    def isBeanAccessor(e: Element) = e.getSimpleName().toString.matches("(get|is).*") && isAbstract(e)

    val interface = Context.interfaceName(e)

    val pakage = Context.packageName(e)

    val className = interface.dropRight(3)
    val classNameGuava = className + "Guava"
    val classNameMutable = className + "Mutable"
    val fqClassName = pakage + "." + className
    val fqClassNameGuava = pakage + "." + classNameGuava
    val fqClassNameMutable = pakage + "." + classNameMutable

    val allElements: List[Element] = Context.allGetters(e)

    val classAnnotation = "" //calcAnnotation(e.getAnnotationMirrors.toList)

    val infos: List[info] = allElements.
      filter(isBeanAccessor).
      map(fillInfo).
      toList

    val (abstracts, nonAbstracts) = infos.partition(_.isAbstract)
    val (nonNullInfo, nullableInfo) = abstracts.partition(_.nonNull)
    val (subbeans, nonsubbeans) = abstracts.partition(_.isBB)
    val format = new java.text.SimpleDateFormat("MMM dd yyyy HH:mm:sss")

    def w(s: String)(implicit w: PrintWriter) = w.println(s.stripMargin('|'))

    def ws[T](data: Traversable[T])(s: T => String)(implicit w: PrintWriter) = {
      data.map(i => s(i).stripMargin('|')).mkString("\n")
    }

    def wsOld[T](data: Traversable[T])(s: T => String)(implicit w: PrintWriter) = {
      w.println(ws(data)(s))
    }

    def ws2[T](data: Traversable[T])(s: (Option[T], Option[T]) => String)(implicit w: PrintWriter) = {
      data
        .toIterator
        .sliding(2)
        .withPartial(true)
        .collect {
          case a :: b :: _ => s(Some(a), Some(b)).stripMargin('|')
        }
        .foreach(w.println)

      w.println(s(data.lastOption, None).stripMargin('|'))
    }

    def blankLine(implicit pw: PrintWriter): Unit = {
      w("")(pw)
    }

    //================================================================
    //GUAVA
    //================================================================
    {
      val methodsReturningBools = infos.filter(i => i.returnType == "Boolean" || i.returnType == "bool")
      val infosWithComparableReturnTypes = infos.filter(i => i.comparable)

      val sourceFile = processingEnv.getFiler().createSourceFile(fqClassNameGuava, e);
      implicit val strWtr = new PrintWriter(sourceFile.openWriter());
      w(s"""               |package ${pakage};
                           |
                           |import java.io.Serializable;
    		  			   |import java.util.Set;
    		  			   |
    		  			   |import javax.annotation.Generated;
    		  			   |import javax.annotation.concurrent.Immutable;
    		  			   |import javax.annotation.concurrent.ThreadSafe;
    		  			   |
    		  			   |import com.google.common.base.Equivalence;
    		  			   |import com.google.common.base.Function;
    		  			   |import com.google.common.base.Objects;
    		  			   |import com.google.common.base.Predicate;
    		  			   |import com.google.common.collect.ComparisonChain;
    		  			   |import com.google.common.collect.Ordering;
                           |
                           |/**
                           | * GENERATED - GENERATED - GENERATED - GENERATED - GENERATED - GENERATED<br>
                           | */
                           |@Immutable
                           |@ThreadSafe
                           |@Generated( "BeanBuilder" )
                           |@SuppressWarnings("all")
                           |public final class ${classNameGuava}
                           |{
                           |
                           |  private ${classNameGuava}()
                           |  {
                           |  }
                           |
                           |
                           |  //=Functions============================================
    ${
        ws(infos)(i => s"""
                           |  /**
        				   |   * Generated!
                           |   */
                           |  public static final Function<$className, ${i.boxed}> TO_${i.nameUpper.toUpperCase} = new Function<$className, ${i.boxed}>() {
                           |     public ${i.boxed} apply( $className x ) {
                           |       if(x == null) {
                           |         return null;
                           |       }
                           |       return x.${i.prefix}${i.nameUpper}();
                           |     }
                           |  };""")
      }

    ${
        ws(infos)(i => s"""
                           |  /**
                           |   * Generated - Alias of TO_${i.nameUpper.toUpperCase} to help with readability!
                           |   */
                           |  public static final Function<$className, ${i.boxed}> BY_${i.nameUpper.toUpperCase} = TO_${i.nameUpper.toUpperCase};""")
      }
                           |
                           |  //=Equivalences============================================
    ${
        ws(infos)(i => s"""
                           |  /**
                           |   * Generated!
                           |   */
                           |  public static final Equivalence<$className> EQUALS_${i.nameUpper.toUpperCase} = Equivalence.equals().onResultOf(TO_${i.nameUpper.toUpperCase});
                           |
        			       |  /**
                           |   * Generated!
                           |   */
                           |   public static final Function<$className,  Equivalence.Wrapper<$className>> EQUALS_${i.nameUpper.toUpperCase}_WRAPPER = new Function<$className,  Equivalence.Wrapper<$className>>() {
                           |           public  Equivalence.Wrapper<$className> apply( $className x ) {
                           |               return EQUALS_${i.nameUpper.toUpperCase}.wrap(x);
                           |           }
                           |       };
                           |""")
      }
                           |
                           |   public static final Function<Equivalence.Wrapper<$className>, $className> EQUALS_UNWRAPPER = new Function<Equivalence.Wrapper<$className>, $className>() {
                           |           public  $className apply( Equivalence.Wrapper<$className> x ) {
                           |               return x.get();
                           |           }
                           |       };
                           |
                           |  //=Predicates============================================
    ${
        ws(infos)(i => s"""
                           |  /**
                           |   * Generated!
                           |   */
                           |  public static final Predicate<${e.getQualifiedName.toString}> by${i.nameUpper}( final ${i.returnType} ${i.nameMangled} ) {
                           |     return new Predicate<${e.getQualifiedName.toString}>() {
                           |       public boolean apply( ${e.getQualifiedName.toString} x ) {
                           |         if(x == null) {
                           |           return false;
                           |         }
                           |         return Objects.equal( x.${i.prefix}${i.nameUpper}(), ${i.nameMangled} );
                           |       }
                           |     };
                           |  }""")
      }
                           |
    ${
        ws(methodsReturningBools)(i => s"""
                           |  /**
                           |   * Generated!
                           |   */
                           |  public static final Predicate<${e.getQualifiedName.toString}> ${"IS_" + i.nameUpper.toUpperCase} = new Predicate<${e.getQualifiedName.toString}>() {
                           |     public boolean apply( ${e.getQualifiedName.toString} x ) {
                           |       if(x == null) {
                           |         return false;
                           |       }
                           |       final Boolean b = x.${i.prefix}${i.nameUpper}();
                           |       if(b == null) {
                           |         return false;
                           |       }
                           |       return b;
                           |     }
                           |  };""")
      }
                           |
                           |  //=Ordering============================================")
    ${
        ws(infosWithComparableReturnTypes)(i => s"""
                           |  /**
      |   * Generated!
                           |   */
        |  public static final Ordering<$className> ORDER_BY_${i.nameUpper.toUpperCase} = new Ordering<$className>() {
                           |     public int compare( $className left, $className right ) {
                           |       if(left == null && right != null) {
                           |         return 1;
                           |       }
                           |       if(left != null && right == null) {
                           |         return -1;
                           |       }
                           |       if(left == null && right == null) {
                           |         return 0;
                           |       }
                           |       return ComparisonChain.start()
                           |              .compare( left.${i.prefix}${i.nameUpper}(), right.${i.prefix}${i.nameUpper}(), Ordering.natural().nullsFirst() )
                           |              .result();
                           |     }
                           |  };""")
      }
                           |
                           |}
      """)
      strWtr.close()
    }

    //===================================================================================
    // Useful data constants
    //===================================================================================

    // create a set of method calls. ie     m.getTitle1(), m.getTitle2(),
    def listAllMethods(things: List[info], m: String) = {
      things.map(i => s"${m}.${i.prefix}${i.nameUpper}()").mkString(", ")
    }

    // create a set of method calls. ie     m.getTitle1(), m.getTitle2(),
    def setEverything(things: List[info], from: String, to: String) = {
      things.map(i => s"${from}.set${i.nameUpper}( ${to}.${i.nameMangled} );").mkString("\n")
    }

    //String x, Integer i, MySubBean b
    def listAllMethodParameters(things: List[info] = abstracts) = {
      things.map(i => i.param).mkString(", ")
    }

    //x, i, b
    def listAllUsageParameters(things: List[info] = abstracts) = {
      things.map(i => i.nameMangled).mkString(", ")
    }

    //x, i, b
    def listAllUsageParametersViaGetters(prefix: String, things: List[info] = abstracts) = {
      things.map(i => prefix + "." + i.prefix + i.nameUpper + "()").mkString(", ")
    }

    //this.x = (thatPrefix.)x;
    //this.i = (thatPrefix.)i;
    //this.b = (thatPrefix.)b;
    def thisEqualsThatBlock(things: List[info] = abstracts, thatPrefix: String = "") = {
      val prefix = if (thatPrefix == "") "" else thatPrefix + "."
      things.map(i => s" this.${i.nameMangled} = ${prefix}${i.nameMangled};").mkString("\n", "\n", "\n")
    }

    //Preconditions.checkNotNull(x, "class.x cannot be null");
    //Preconditions.checkNotNull(i, "class.i cannot be null");
    //Preconditions.checkNotNull(b, "class.b cannot be null");
    def precondBlock(things: List[info] = abstracts) = {
      things.map(i => i.precond(className)).mkString("\n", "\n", "\n")
    }

    //private (final) String x;
    //private (final) Integer i;
    //private (final) MySubBean b;
    def memberVars(things: List[info] = abstracts, isFinal: Boolean = false, visability: String = "private") = {
      val finalStr = if (isFinal) "final" else ""
      things.map(i => s"  $visability $finalStr ${i.returnType} ${i.nameMangled};").mkString("\n", "\n", "\n")
    }

    // .x( x )
    // .i( i )
    // .b( b )
    def builderLines(things: List[info] = abstracts) = {
      things.map(i => s"  |    .${i.name}( ${i.nameMangled} )   ").mkString("\n", "\n", "\n")
    }

    def postInit() = {
      s""" final Set<ConstraintViolation<${className}>> constraintViolations = BeanValidator.validator.validate( this );
             Preconditions.checkArgument(constraintViolations.isEmpty(), constraintViolations);"""
    }

    val lastGeneric = if (nonNullInfo.size == 0) "T" else s"T${nonNullInfo.size}"
    val generics = if (nonNullInfo.size == 0) "T" else (1 to nonNullInfo.size).map(n => "T" + n).mkString(", ")
    val generics2 = if (nonNullInfo.size == 0) "T" else (1 to nonNullInfo.size).map(n => "T").mkString(", ")

    {
      val sourceFile = processingEnv.getFiler().createSourceFile(fqClassName, e);
      implicit val strWtr = new PrintWriter(sourceFile.openWriter());

      //===================================================================================
      // The Bean class
      //===================================================================================

      w(s"""               |package ${pakage};
                           |
                           |import java.io.Serializable;
            		  			   |import java.util.Set;
            		  			   |
            		  			   |import javax.annotation.CheckReturnValue;
            		  			   |import javax.annotation.Generated;
            		  			   |import javax.annotation.Nonnull;
            		  			   |import javax.annotation.concurrent.Immutable;
            		  			   |import javax.annotation.concurrent.ThreadSafe;
            		  			   |import javax.validation.ConstraintViolation;
            		  			   |import javax.validation.Validation;
            		  			   |import javax.validation.Validator;
            		  			   |import javax.validation.ValidatorFactory;
            		  			   |
            		  			   |import org.apache.commons.lang3.builder.EqualsBuilder;
            		  			   |
            		  			   |import ca.pandp.builder.BeanValidator;
            		  			   |import ca.pandp.builder.Buildable;
            		  			   |import ca.pandp.builder.Doneable;
            		  			   |import ca.pandp.builder.Callback;
            		  			   |import com.google.common.base.Objects;
            		  			   |import com.google.common.base.Preconditions;
                           |
                           |/**
                           | * @since ${format.format(new Date())}
                           | * @see ${pakage}.${interface}
                           | */
                           |@Immutable
                           |@ThreadSafe
                           |@Generated( "BeanBuilder" )
                           |@SuppressWarnings("all")
                           |
                           |public final class ${className} ${if (!e.getKind().isInterface()) s" extends ${e.getQualifiedName.toString}" else ""} implements Serializable ${if (e.getKind().isInterface()) s", ${e.getQualifiedName.toString}" else ""}
                           |{
                           |  private static final long serialVersionUID = 1L;
                           |
                           |  ${memberVars(abstracts, true)}
                           |
                           |  private ${className}( ${listAllMethodParameters()} )
                           |  {
                           |      ${thisEqualsThatBlock()}
                           |      ${postInit()}
                           |  }
                           |
                           |  public static ${className} fromMutable( final ${classNameMutable} mutable )
                           |  {
                           |      return new ${className}( ${listAllMethods(abstracts, "mutable")});
                           |  }
                           |
                           |  public ${classNameMutable} toMutable()
                           |  {
                           |      return toMutable( this );
                           |  }
                           |
                           |  private static ${classNameMutable} toMutable( final ${className} immutable )
                           |  {
                           |      final ${classNameMutable} mutable = new ${classNameMutable}();
                           |
                           |      if( immutable != null )
                           |      {
                           |          ${setEverything(abstracts, "mutable", "immutable")}
                           |      }
                           |      return mutable;
                           |  }
                           |
                           |  //=Getters============================================
${
        ws(abstracts)(i => s"""
                           |  ${i.annotations}
      				       |  @CheckReturnValue
                           |  @Override
                           |  public ${i.returnType} ${i.prefix}${i.nameUpper}()
                           |  {
                           |       return ${i.nameMangled};
                           |  }""")
      }
                           |
                           |  //=Withs============================================
${
        ws(abstracts)(i => s"""
                           |  @CheckReturnValue
                           |  @Nonnull
       					   |  public $className with${i.nameUpper}(${i.param})
                           |  {
                           |       return new ${className}(${listAllUsageParameters()});
                           |  }""")
      }
                           |
                           |  @Override
                           |  public String toString()
                           |  {
                           |    return Objects
	                       |             .toStringHelper( this.getClass() )
${ws(abstracts)(i => s"""  |             .add( "${i.nameUpper}", ${i.nameMangled} )""")}
                           |             .omitNullValues()
	                       |             .toString();
                           |  }
                           |
                           |  //=hashcode============================================
  	                       |  @Override
  	                       |  public int hashCode()
  	                       |  {
  	                       |     return Objects.hashCode( ${abstracts.map(_.nameMangled).mkString(", ")} );
  	                       |  }
  	                       |
                           |  //=equals============================================
  	                       |  @Override
  	                       |  public boolean equals(Object o)
  	                       |  {
  	                       |     if (this == o)
  	                       |     {
  	                       |        return true;
  	                       |     }
  	                       |     if ( !( o instanceof $className ) )
  	                       |     {
  	                       |        return false;
  	                       |     }
  	                       |
  	                       |     final $className that = ($className) o;
  	                       |     return new EqualsBuilder()
  ${ws(abstracts)(i => s"  |         .append( ${i.nameMangled}, that.${i.nameMangled} )")}
                           |         .isEquals();
	                       |  }
	                       """)

      //new Builders - All parameters (if <= 3)
      if (infos.size <= 3) {
        w(s"""            |  @Nonnull
        							     |  @CheckReturnValue
                           |  public static $className new$className(${listAllMethodParameters()})
                           |  {
                           |    return new $className(${listAllUsageParameters()});
                           |  }""")
        w("")
      }

      w(s"""               |  @Nonnull
        				   |  @CheckReturnValue
                           |  public static BeanRequires0 build${className}()
                           |  {
                           |    return new BeanBuilder();
                           |  }
                           |
                           |  public BeanUpdateable update() {
                           |  	return new BeanUpdater(this);
                           |  }
                           |
                           |
                           |  /**
                           |   * @Deprecated Not a public API - Internal BBB use only
                           |   */
                           |  @Deprecated
                           |  public static class Internal {
            			   |     public static <P> SubBeanRequires0<P> newSubBeanBuilder(P parent, Callback<${className}> c) {
                           |       return new SubBeanBuilder(parent, c);
                           |     }
                           |     public static <P> SubBeanUpdatable<P> newSubBeanUpdater(P parent, Callback<${className}> c, ${className} x) {
                           |       return new SubBeanUpdater(parent, c, x);
                           |     }
                           |  }""")


      ws2(nonNullInfo.zipWithIndex) {
        case (None, None) =>
        s"""             | //There are no non-null fields in this bean, therefore the Requires interface == Buildable interface
                         | public interface BeanRequires0 extends BeanBuildable{}
                         | public interface SubBeanRequires0<P> extends SubBeanBuildable<P>{}"""
          
        case (Some((a, suffix)), Some((b, suffixB))) =>
          s"""             | //Middle non-null parameter... 
                           | public interface BeanRequires${suffix}
                           | {
                           |      BeanRequires${suffixB}  ${a.nameMangled}( ${a.param} );
                           |      ${if (a.isBB) s"${a.returnType}.SubBeanRequires0<BeanRequires${suffixB}> new${a.nameUpper}();" else ""}
                           | }
                           | public interface SubBeanRequires${suffix}<P>
                           | {
                           |      SubBeanRequires${suffixB}<P>  ${a.nameMangled}( ${a.param} );
                           |      ${if (a.isBB) s"${a.returnType}.SubBeanRequires0<SubBeanRequires${suffixB}<P>> new${a.nameUpper}();" else ""}
                           | }
                           """
        case (Some((a, suffix)), None) =>
          s"""             | //Last non-null parameter... 
                           | public interface BeanRequires${suffix}
                           | {
                           |   BeanBuildable  ${a.nameMangled}( ${a.param} );
                           |   ${if (a.isBB) s"${a.returnType}.SubBeanRequires0<BeanBuildable> new${a.nameUpper}();" else ""}
                           | }
                           | public interface SubBeanRequires${suffix}<P>
                           | {
                           |   SubBeanBuildable<P>  ${a.nameMangled}( ${a.param} );
                           |   ${if (a.isBB) s"${a.returnType}.SubBeanRequires0<SubBeanBuildable<P>> new${a.nameUpper}();" else ""}
                           | }
                           """
        case (None, Some(_)) => ???
      }

      w(s"""                   | private interface Nullable<T>
                               |  {""")
      wsOld(nullableInfo) { a =>
        s"""
                               |    T ${a.nameMangled}( ${a.param} );
                               |    ${if (a.isBB) s"${a.returnType}.SubBeanRequires0<T> new${a.nameUpper}();" else ""}"""
      }
      w(s"""                   |  }""")

      w(s"""                   | private interface NonNullable<T> {""")
      wsOld(subbeans) { i => s"""       ${i.returnType}.SubBeanUpdatable<T> get${i.nameUpper}();""" }
      wsOld(nonNullInfo) { a =>
        s"""                   |    T ${a.nameMangled}( ${a.param} );
                               |    ${if (a.isBB) s"${a.returnType}.SubBeanRequires0<T> new${a.nameUpper}();" else ""}"""
      }
      w(s"""                   | }""")

      w(s"""                   | public interface BeanBuildable extends Nullable<BeanBuildable>, Buildable<${className}> {}""")
      w(s"""                   | public interface SubBeanBuildable<P> extends Nullable<SubBeanBuildable<P>>, Doneable<P> {}""")
      w(s"""                   | public interface BeanUpdateable extends Nullable<BeanUpdateable>, NonNullable<BeanUpdateable>, Buildable<${className}> {}""")
      w(s"""                   | public interface SubBeanUpdatable<P> extends Nullable<SubBeanUpdatable<P>>, NonNullable<SubBeanUpdatable<P>>, Doneable<P> {}""")

      def writeSubBeansU() = {

        subbeans.map { i =>
          s"""
            						public ${i.returnType}.SubBeanUpdatable<${lastGeneric}> get${i.nameUpper}() {
                             			return ${i.returnType}.Internal.newSubBeanUpdater((${lastGeneric})this, newCallback${i.nameUpper}(m), m.get${i.nameUpper}());
                             		}
      					   """
        }.mkString("\n", "\n", "\n")
      }

      def writeNonSubBeans(currentUpdater: String) = {
        nullableInfo.map { i =>
          val bbb = if (i.isBB)
            s"""
                        public ${i.returnType}.SubBeanRequires0<${lastGeneric}> new${i.nameUpper}() {
                   			return ${i.returnType}.Internal.newSubBeanBuilder((${lastGeneric})this, newCallback${i.nameUpper}(m) );
                   		}
                         """
          else ""
          val ccc = s"""
                		public ${lastGeneric} ${i.nameMangled}(${i.param}) {
                		    ${i.precond(className)}
                   			m.set${i.nameUpper}(${i.nameMangled});
                   			return (${lastGeneric})this;
                   		}
                 """
          bbb + ccc
        }.mkString("\n", "\n", "\n")
      }

      w("//Abstract===========================")
      w(s"""
                           |  private abstract static class Abstract<P, ${generics}> {
                           |
                           |   		private final P parent;
                           |   		private final Callback<${className}> callback;
      					           |      private final ${classNameMutable} m;
                           |
                           |  		public Abstract(${className} x, P parent, Callback<${className}> callback) {
                           |			this.m = toMutable(x);
                           |            this.parent = parent;
                           |            this.callback = callback;
             		           |  		}
                           |
                           """)
      wsOld(nonNullInfo.zipWithIndex) {
        case (a, index) => s"""
                               |    public T${index + 1} ${a.nameMangled}(${a.returnType} ${a.nameMangled}) {
                               |    	Preconditions.checkNotNull(${a.nameMangled}, "${className}.${a.name} cannot be null");
                               |    	m.set${a.nameUpper}(${a.nameMangled});
                               |    	return (T${index + 1})this;
                               |    }"""
      }

      wsOld(nonNullInfo.zipWithIndex.filter(_._1.isBB)) {
        case (a, index) => s"""
                               |    public ${a.returnType}.SubBeanRequires0<T${index + 1}> new${a.nameUpper}() {
                   			   |        return ${a.returnType}.Internal.newSubBeanBuilder((T${index + 1})this, newCallback${a.nameUpper}(m) );
                   		       |    }"""
      }

      w(s"""
          			  	       |        ${writeNonSubBeans("")}
          			  	       |  	    ${writeSubBeansU()}
          			  	       |
                           |  		public ${className} build() {
                           |			return new ${className}(${listAllUsageParametersViaGetters("m")});
                           |  		}
                           |
                           |   		public final P done() {
                           |   			callback.update(build());
                           |   			return parent;
                           |   		}
                           """)
      wsOld(subbeans) { i =>
        s"""               |  private Callback<${i.returnTypeFQ}> newCallback${i.nameUpper}(final ${classNameMutable} m) {
                           |      return new Callback<${i.returnTypeFQ}>() {
                           |			public void update(${i.returnTypeFQ} val) {
                           |					m.set${i.nameUpper}(val);
                           |			}
                           |		 };
                           |  }
                           """
      }

      def inter(prefix: String, tl: String) = {
        nonNullInfo.zipWithIndex.drop(1).map {
          case (a, index) => s"${prefix}${index}${tl}, "
        }.mkString("")
      }

      def inter2(tl: String) = {
        nonNullInfo.size match {
          case 0 => tl
          case n => List.fill(n)(tl).mkString(",")
        }
      }

      w(s"                 |}")
      w(s"""               |
                           |  private static class BeanBuilder
                           |    extends Abstract<BeanBuildable, ${inter("BeanRequires", "")} BeanBuildable>
                           |    implements BeanBuildable, ${inter("BeanRequires", "")} BeanRequires0 
                           |    {
                           |   		public BeanBuilder()
                           |      {
                           |        super( null, null, null );
                           |      }
                           |    }
                           |
                           |  private static class SubBeanBuilder<P>
                           |    extends Abstract<P, ${inter("SubBeanRequires", "<P>")} SubBeanBuildable<P>>
                           |    implements SubBeanRequires0<P>, ${inter("SubBeanRequires", "<P>")} SubBeanBuildable<P> {
                           |   		public SubBeanBuilder(P parent, Callback<${className}> callback)
                           |     {
                           |   			super(null, parent, callback);
             		           |  		}
                           | }
                           |
                           |  private static class BeanUpdater
                           |    extends Abstract<BeanUpdateable, ${inter2("BeanUpdateable")}>
                           |    implements BeanUpdateable {
                           |  		public BeanUpdater(${className} x)
                           |      {
                           |			  super(x, null, null );
             		           |  		}
                           | }
                           |
                           |  private static class SubBeanUpdater<P>
                           |    extends Abstract<P, ${inter2("SubBeanUpdatable<P>")}>
                           |    implements SubBeanUpdatable<P> {
                           |   		public SubBeanUpdater(P parent, Callback<${className}> callback, ${className} x)
                           |     {
                           |   			super(x, parent, callback);
                           |   		}
                           | }
                           """)

      //Close class
      w("")
      w(s"                 |}")

      strWtr.close()
    }

    {
      val sourceFile = processingEnv.getFiler().createSourceFile(fqClassNameMutable, e);
      implicit val strWtr = new PrintWriter(sourceFile.openWriter());

      //Package
      w(s"                 |package ${pakage};")
      w(s"""              |import java.io.Serializable;
                           |
                           |import javax.annotation.CheckReturnValue;
                           |import javax.annotation.Generated;
    		  			           |import javax.annotation.Nonnull;
    		  			           |
                           |import com.google.common.base.Function;
    		  			           |import com.google.common.base.Preconditions;
                           |""")
      w("")

      //Open class
      w(s"""              |
                           |/**
                           | * GENERATED - GENERATED - GENERATED - GENERATED - GENERATED - GENERATED<br>
                           | */
                           |@Generated( "BeanBuilder" )
                           |@SuppressWarnings("all")
                           |${classAnnotation}  """)
      w(s"""              |public final class ${classNameMutable} implements Serializable
                           |{
                           |
                           | ${memberVars()}
                           |
                           | public ${classNameMutable}() {}
                           |
                           """)
      //Getters
      w("                    |  //=Getters============================================")
      wsOld(abstracts)(i => s"""|
                             |  @CheckReturnValue
      						 |  ${i.annotations}
                             |  public ${i.returnType} ${i.prefix}${i.nameUpper}()
                             |  {
                             |       return ${i.nameMangled};
                             |  }""")
      w("")
      //Setters
      w("                    |  //=Setters============================================")
      wsOld(abstracts)(i => s"""|
                             |  public void set${i.nameUpper}(${i.param})
                             |  {
                             |       ${i.precond(classNameMutable)}
                             |       this.${i.nameMangled} = ${i.nameMangled};
                             |  }""")
      w(s"""                |}""")
      strWtr.close()
    }
  }

}

