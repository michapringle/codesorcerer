package com.beautifulbeanbuilder;


import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.beautifulbeanbuilder.Helper.hasCompileError;

@RunWith(JUnit4.class)
public class GeneralFailTest {


    @Test
    public void notEndingWithDef() throws Exception {
        JavaFileObject source = JavaFileObjects
                .forSourceLines("test.Bob",
                        "                                                                        ",
                        "package test;                                                  ",
                        "                                                                                    ",
                        "import com.beautifulbeanbuilder.BeautifulBean;                                                  ",
                        "                                                                                   ",
                        "@BeautifulBean                                                    ",
                        "public interface Bob {                                                  ",
                        "}                                                                  ");

        hasCompileError(source, "Must end with Def");
    }
}

