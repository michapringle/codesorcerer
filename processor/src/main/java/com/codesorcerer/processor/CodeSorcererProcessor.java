package com.codesorcerer.processor;

import com.codesorcerer.ConsoleColors;
import com.codesorcerer.LambdaExceptionUtils;
import com.codesorcerer.abstracts.AbstractInputBuilder;
import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;

import com.google.common.collect.*;
import com.google.common.reflect.ClassPath;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;


@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CodeSorcererProcessor extends javax.annotation.processing.AbstractProcessor {

  public Types typeUtils;
  public Elements elementUtils;
  public Filer filer;
  public Messager messager;

  private static boolean headerPrinted = false;
  private static boolean loaded = false;
  private static Multimap<String, AbstractSpell> allGeneratorsByAnnotationClass = HashMultimap
      .create(); //AnnotationClassName to processor
  private static Map<Class, AbstractInputBuilder> allInputBuilders = Maps
      .newHashMap(); //InputClass to inputBuilder
  private static Set<TypeElement> processedTypes = Sets.newHashSet();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return ImmutableSet.of("*");
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();
    this.elementUtils = processingEnv.getElementUtils();
    this.typeUtils = processingEnv.getTypeUtils();

    load();
  }

  private void load() {
    try {
      if (!loaded) {
        loaded = true;
        allGeneratorsByAnnotationClass = fillAllGenerators();
        allInputBuilders = fillAllInputs();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private void printHeaders() {
    try {
      if (!headerPrinted) {
        headerPrinted = true;
        printHeader();
        //printInfo();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void printInfo() {
    System.out.println("Inputs:");
    for (Map.Entry<Class, AbstractInputBuilder> b : allInputBuilders.entrySet()) {
      System.out.println(
          "  " + b.getValue().getClass().getSimpleName() + " builds " + b.getKey().getSimpleName());
    }
    System.out.println("Spells:");
    for (Map.Entry<String, AbstractSpell> b : allGeneratorsByAnnotationClass.entries()) {
      System.out.println(
          "  @" + StringUtils.substringAfterLast(b.getKey(), ".") + " casts " + b.getValue()
              .getClass().getSimpleName());
    }
  }

  private Map<Class, AbstractInputBuilder> fillAllInputs() throws Exception {
    File f = new File(System.getProperty("user.home") + "/.code-sorcerer-allInputs.txt");
    if (f.exists()) {

      final long msSinceEdited = System.currentTimeMillis() - f.lastModified();
      final boolean recentlyCreated = msSinceEdited < 30 * 1000;

      if (!recentlyCreated) {
        final Set<String> builders = searchForBuilders();
        writeCache(f, builders);
      } else {
      }
    } else {
      final Set<String> builders = searchForBuilders();
      writeCache(f, builders);
    }

    return readCacheClasses(f)
        .stream()
        .map(this::newInputBuilder)
        .collect(Collectors.toMap(c -> c.getInputClass(), Function.identity()));
  }



  private Multimap<String, AbstractSpell> fillAllGenerators() throws IOException {
    File f = new File(System.getProperty("user.home") + "/.code-sorcerer-allGens.txt");
    if (f.exists()) {

      final long msSinceEdited = System.currentTimeMillis() - f.lastModified();
      final boolean recentlyCreated = msSinceEdited < 60 * 1000;

      if (!recentlyCreated) {
        final Set<String> builders = searchForSpells();
        writeCache(f, builders);
      } else {
      }
    } else {
      final Set<String> builders = searchForSpells();
      writeCache(f, builders);
    }

    final HashMultimap<String, AbstractSpell> allGeneratorsByAnnotationClass = HashMultimap.create();
    readCacheClasses(f)
        .stream()
        .map(this::newGenerator)
        .forEach(g -> allGeneratorsByAnnotationClass.put(g.getAnnotationClass().getName(), g));

    return allGeneratorsByAnnotationClass;
  }

  /**
   * THIS IS SLOW
   */
  private Set<String> searchForBuilders() {
    try {
      return ClassPath.from(getClass().getClassLoader()).getAllClasses()
          .stream()
          .filter(c -> c.getSimpleName().endsWith("InputBuilder"))
          .map(this::safeLoad)
          .filter(AbstractInputBuilder.class::isAssignableFrom)
          .filter(c -> !Modifier.isAbstract(c.getModifiers()))
          .map(c -> c.getName())
          .collect(toSet());
    } catch (Exception e) {
      return Sets.newHashSet();
    }
  }

  private Set<String> searchForSpells() {
    try {
      return ClassPath.from(getClass().getClassLoader()).getAllClasses()
          .stream()
          .filter(c -> c.getSimpleName().endsWith("Spell"))
          .map(this::safeLoad)
          .filter(AbstractSpell.class::isAssignableFrom)
          .filter(c -> !Modifier.isAbstract(c.getModifiers()))
          .map(c -> c.getName())
          .collect(toSet());
    } catch (Exception e) {
      return Sets.newHashSet();
    }
  }

  private Set<Class> readCacheClasses(File f) {
    return readCache(f)
        .stream()
        .map(this::safeForName)
        .filter(c -> c != null)
        .collect(toSet());
  }

  private HashSet<String> readCache(File f) {
    try {
      FileUtils.touch(f);
      return SerializationUtils.deserialize(new FileInputStream(f));
    } catch (Exception e) {
      e.printStackTrace();
      f.delete();
      return Sets.newHashSet();
    }
  }

  private Set<String> writeCache(File f, Collection<String> cache) {
    try {
      final HashSet<String> prev = readCache(f);
      prev.addAll(cache);
      SerializationUtils.serialize(prev, new FileOutputStream(f));
      return prev;
    } catch (Exception e) {
      e.printStackTrace();
      return Sets.newHashSet();
    }
  }


  private Class<?> safeLoad(ClassPath.ClassInfo c) {
    try {
      return c.load();
    } catch (Throwable e) {
      return String.class;
    }
  }

  private Class<?> safeForName(String c) {
    try {
      return Class.forName(c);
    } catch (Throwable e) {
      return null;
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Set<TypeElement> types = elementsThatNeedProcessing(annotations, roundEnv);

    //Calculate the set of types that need processing
    final Set<TypeElement> notYetProcessed = Sets.newHashSet();
    for (TypeElement te : types) {
      if (processedTypes.add(te)) {
        notYetProcessed.add(te);
      }
    }

    //Run the generators for this type
    runFunction(notYetProcessed, (result) -> {
      printHeaders();
      System.out.println(
          ConsoleColors.CYAN + " Casting " + result.spell.getClass().getSimpleName() + " on "
              + ConsoleColors.CYAN_BOLD + result.te.getQualifiedName() + ConsoleColors.RESET);
    }, "Debug");

//        runFunction(notYetProcessed, (result) -> {
//            result.spell.build(result);
//            result.spell.modify(result, results.values());
//            result.spell.write(result);
//        }, "All");

    runFunction(notYetProcessed, (result) -> result.spell.build(result), "Build");
    runFunction(notYetProcessed, (result) -> result.spell.modify(result, results.values()),
        "Modify");
    runFunction(notYetProcessed, (result) -> result.spell.write(result), "Write");
    runFunction(notYetProcessed, (result) -> result.spell.processingOver(results.values()), "Over");

//
//        //Print Summary
//        if (roundEnv.processingOver()) {
//            Long outputCount = results.values()
//                    .stream()
//                    .filter(r -> r.output != null)
//                    .collect(Collectors.counting());
//
//            System.out.println("Conjured " + outputCount + " code files!");
//        }

    return false;

  }

  private final ConcurrentMap<Pair<TypeElement, AbstractSpell>, Result> results = Maps
      .newConcurrentMap();

  private void runFunction(Collection<TypeElement> types,
      LambdaExceptionUtils.Consumer_WithExceptions<Result> f, String phase) {

    Multimap<AbstractSpell, TypeElement> runOrder = TreeMultimap
        .create(Ordering.natural(), Ordering.arbitrary());

    for (TypeElement te : types) {
      for (String ann : AbstractSpell.getAllAnnotations(te)) {
        for (AbstractSpell g : allGeneratorsByAnnotationClass.get(ann)) {
          runOrder.put(g, te);
        }
      }
    }

    for (Map.Entry<AbstractSpell, TypeElement> e : runOrder.entries()) {
      try {
        Result result = getResult(e.getValue(), e.getKey());
        fillSpell(result.spell);
        f.accept(result);
      } catch (Exception ex) {
        messager.printMessage(Diagnostic.Kind.ERROR, trimToEmpty(ex.getMessage()), e.getValue());
        ex.printStackTrace();
      }

    }

  }

  private Result getResult(TypeElement te, AbstractSpell g) {
    return results.computeIfAbsent(Pair.of(te, g), p -> {
      AbstractInputBuilder inputBuilder = allInputBuilders.get(g.getInputClass());
      fillInputBuilder(inputBuilder);
      Object input = inputBuilder.buildInput(te);

      Result r = new Result();
      r.te = p.getLeft();
      r.spell = p.getRight();
      r.input = input;
      return r;
    });
  }

  private AbstractSpell newGenerator(Class c) {
    try {
      return (AbstractSpell) c.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("", e);
    }
  }

  private void fillSpell(AbstractSpell abstractSpell) {
    abstractSpell.elementUtils = elementUtils;
    abstractSpell.filer = filer;
    abstractSpell.messager = messager;
    abstractSpell.processingEnvironment = processingEnv;
    abstractSpell.typeUtils = typeUtils;
  }

  private AbstractInputBuilder newInputBuilder(Class c) {
    try {
      return (AbstractInputBuilder) c.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("", e);
    }
  }

  private void fillInputBuilder(AbstractInputBuilder abstractInputBuilder) {
    abstractInputBuilder.elementUtils = elementUtils;
    abstractInputBuilder.filer = filer;
    abstractInputBuilder.messager = messager;
    abstractInputBuilder.processingEnvironment = processingEnv;
    abstractInputBuilder.typeUtils = typeUtils;
  }


  private Set<TypeElement> elementsThatNeedProcessing(Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv) {
    final Set<TypeElement> elementThatNeedProcessing = Sets.newHashSet();

    annotations.forEach(a -> {
      Set e = roundEnv.getElementsAnnotatedWith(a);
      elementThatNeedProcessing.addAll(e);
    });

    final Set<TypeElement> interstingElementsThatNeedProcessing = ElementFilter
        .typesIn(elementThatNeedProcessing)
        .stream()
        .filter(te -> te.getKind() == ElementKind.CLASS || te.getKind() == ElementKind.INTERFACE
            || te.getKind() == ElementKind.PACKAGE || te.getKind() == ElementKind.ENUM)
        .collect(toSet());

    return interstingElementsThatNeedProcessing;
  }


  private void printHeader() {
    System.out.println(ConsoleColors.CYAN);
    System.out.println(
        ("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"));
    System.out.println(
        ("                   ,%%%,                                                               ")
            .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN));
    System.out.println(
        ("                 ,%%%` %==--                                                               ")
            .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN));
    System.out.println(("                ,%%`( '|        " + ConsoleColors.CYAN_BOLD
        + "     ,-.       .        ,-.                                ")
        .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN) + ConsoleColors.CYAN);
    System.out.println(("               ,%%@ /\\_/       " + ConsoleColors.CYAN_BOLD
        + "     /          |       (   `                              ")
        .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN) + ConsoleColors.CYAN);
    System.out.println(("     ,%.-\"\"\"-- % %%\"@@__    " + ConsoleColors.CYAN_BOLD
        + "        |    ,-. ,-| ,-.    `-.  ,-. ;-. ,-. ,-. ;-. ,-. ;-. ")
        .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN) + ConsoleColors.CYAN);
    System.out.println(("    %%/             |__`\\      " + ConsoleColors.CYAN_BOLD
        + "     \\    | | | | |-'   .   ) | | |   |   |-' |   |-' |     ")
        .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN) + ConsoleColors.CYAN);
    System.out.println(("   .%'\\     |   \\   /  //     " + ConsoleColors.CYAN_BOLD
        + "       `-' `-' `-' `-'    `-'  `-' '   `-' `-' '   `-' '     ")
        .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN) + ConsoleColors.CYAN);
    System.out.println(("   ,%' >   .'----\\ |  [/       " + ConsoleColors.CYAN_BOLD
        + "                                                        ")
        .replace("%", ConsoleColors.CYAN_BOLD + "%" + ConsoleColors.CYAN) + ConsoleColors.CYAN);
    System.out.println(
        ("      < <<`       ||                                                               "));
    System.out.println(
        ("       `\\\\\\       ||                                                               "));
    System.out.println(
        ("         )\\\\      )\\                                                               "));
    System.out.println(
        ("^^^^^^^^^\"\"\"^^^^^^\"\"^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"));
    System.out.print(ConsoleColors.RESET);
  }

}
