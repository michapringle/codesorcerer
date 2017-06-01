package com.codesorcerer;


import com.codesorcerer.processor.CodeSorcererProcessor;
import com.codesorcerer.targets.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.testing.compile.JavaFileObjects;

import javax.annotation.Nonnull;
import javax.tools.JavaFileObject;
import java.util.*;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class Helper {

    public static void compiles(String... lines) {
        hasNoCompileErrors(forStandardDef(lines));
    }

    public static JavaFileObject forStandardDef(String... lines) {

        long now = System.currentTimeMillis();
        List<String> start = Arrays.asList(
                "          package test;                                                  ",
                "                                                            ",
                "          import " + ObjectMapper.class.getName() + ";                                                  ",
                "          import " + BBBMutable.class.getName() + ";                                                  ",
                "          import " + BBBJson.class.getName() + ";                                                  ",
                "          import " + BBBGuava.class.getName() + ";                                                  ",
                "          import " + BBBImmutable.class.getName() + ";                                                  ",
                "          import " + BBBTypescript.class.getName() + ";                                                  ",
                "          import " + BasicTypescriptMapping.class.getName() + ";                                                  ",
                "          import " + Ordering.class.getName() + ";                                                  ",
                "          import " + Nonnull.class.getName() + ";                                                  ",
                "          import " + List.class.getName() + ";                                                  ",
                "          import " + Map.class.getName() + ";                                                  ",
                "          import " + Set.class.getName() + ";                                                  ",
                "          import " + ArrayList.class.getName() + ";                                                  ",
                "          import " + HashMap.class.getName() + ";                                                  ",
                "                                                            ",
                "          public class Container" + now + " {"
        );

        List<String> end = Arrays.asList("          }");

        Iterable allLines = Iterables.concat(start, Arrays.asList(lines), end);

        return JavaFileObjects
                .forSourceLines("test.Container"+now, allLines);

    }


    public static void hasCompileError(JavaFileObject source, String msg) {
        assertAbout(javaSource()).that(source)
                //.withCompilerOptions(ImmutableList.of("-XprintRounds"))
                .processedWith(new CodeSorcererProcessor())
                .failsToCompile()
                .withErrorContaining(msg);
    }


    public static void hasNoCompileErrors(JavaFileObject... source) {
        assertAbout(javaSources()).that(Arrays.asList(source))
                //.withCompilerOptions(ImmutableList.of("-XprintRounds", "-proc:only"))
                .processedWith(new CodeSorcererProcessor())
                .compilesWithoutError();
    }
}

