package com.codesorcerer;


import com.google.testing.compile.JavaFileObjects;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.codesorcerer.Helper.compiles;
import static com.codesorcerer.Helper.hasCompileError;

@Ignore
@RunWith(JUnit4.class)
public class GeneralFailTest {


    @Test
    public void notEndingWithDef() throws Exception {
        JavaFileObject source = JavaFileObjects
                .forSourceLines("test.Bob",
                        "                                                                        ",
                        "package test;                                                  ",
                        "                                                                                    ",
                        "import com.codesorcerer.BBBImmutable;                                                    ",
                        "@BBBImmutable                                                  ",
                        "public interface Bob {                                                  ",
                        "}                                                                  ");

        hasCompileError(source, "Must end with Def");
    }


    @Test
    public void nothingToGenerate() throws Exception {
        compiles(
                "                                                                        ",
                "@com.codesorcerer.BeautifulBean                                                    ",
                "public interface Bob3Def {                                                  ",
                "}                                                                  ");
    }


    @Test
    public void notRelevantAnnotatoin() throws Exception {
        compiles(
                "                                                                        ",
                "@Deprecated                                                    ",
                "public class Bob2 {                                                  ",
                "}                                                                  ");
    }
}

