package ca.pandp.processor;

import java.io.PrintWriter
import java.util.Date
import javax.annotation.Nonnull
import javax.annotation.processing.{ AbstractProcessor, RoundEnvironment, SupportedAnnotationTypes, SupportedSourceVersion }
import javax.lang.model.SourceVersion
import javax.lang.model.`type`.{ DeclaredType, ExecutableType }
import javax.lang.model.element.{ AnnotationMirror, Element, Modifier, TypeElement }
import javax.lang.model.util.SimpleTypeVisitor6
import scala.collection.JavaConversions._
import javax.lang.model.`type`.TypeMirror
import javax.lang.model.`type`.TypeKind
import scala.collection.JavaConversions._
import collection.breakOut
import scala.collection.mutable.LinkedHashMap

object Context {

  var first = true
  def printHeader(e: TypeElement): Unit = {
    if (first) {
      println("****************************************************")
      println("* Bean Builder - Found the following bean Defs:    *")
      println("****************************************************")
      first = false
    }
    println("*      " + e.getQualifiedName)
  }

  def printFooter(): Unit = {
    println("******************************************************************************")
  }

  def packageName(e: Element) = {
    val name = e.getEnclosingElement.toString

    if (name.endsWith(".def")) {
      name.dropRight(4)
    } else {
      name
    }
  }

  def interfaceName(e: Element) = {
    e.getSimpleName.toString().reverse.takeWhile(_ != '.').reverse
  }

  /**
   * Determine all type elements for the classes and interfaces referenced
   * in the extends/implements clauses of the given type element.
   */
  def getSuperTypes2(myType: TypeElement): List[TypeElement] =
    {
      if (myType == null) {
        List()
      } 
      else {
        var superelems: List[TypeElement] = List()

        val stack: java.util.Deque[TypeElement] = new java.util.ArrayDeque[TypeElement]
        stack.push(myType)
        while (!stack.isEmpty) {
          val current: TypeElement = stack.pop
          val supertypecls: TypeMirror = current.getSuperclass
          if (supertypecls.getKind ne TypeKind.NONE) {
            val supercls: TypeElement = supertypecls.asInstanceOf[DeclaredType].asElement.asInstanceOf[TypeElement]
            if (!superelems.contains(supercls)) {
              stack.push(supercls)
              superelems = superelems :+ supercls
            }
          }

          for (supertypeitf <- current.getInterfaces) {
            val superitf: TypeElement = supertypeitf.asInstanceOf[DeclaredType].asElement.asInstanceOf[TypeElement]
            if (!superelems.contains(superitf)) {
              stack.push(superitf)
              superelems = superelems :+ superitf
            }
          }
        }
        val elms: List[TypeElement] = superelems.filter(_.toString != "java.lang.Object")
        elms
      }
    }

  case class GetterInfo(e:Element, topLevelDefinedInterface:TypeElement, nullable:Boolean)
  
  def allGetters(myType: TypeElement): List[Element] = {
    val superInterfaces = getSuperTypes2(myType) :+ myType
    
    val pairs = superInterfaces
    .flatMap(_.getEnclosedElements.toList)
    .map(e => (e.getSimpleName().toString(), e))
    
    val map = scala.collection.mutable.LinkedHashMap[String, Element]()

    //Add everything
    pairs.foreach(p => map += p)
    
    //Add nonnull things again... should always have @nonnull if any inherited method has it!
    pairs.filter{ e =>
      e._2 .getAnnotation(classOf[Nonnull]) != null}.foreach(p => map += p)
    
    map.values.toList
  }

}