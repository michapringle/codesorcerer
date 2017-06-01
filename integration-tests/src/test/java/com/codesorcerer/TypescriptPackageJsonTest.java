package com.codesorcerer;


import com.codesorcerer.targets.*;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;
import java.math.BigDecimal;

@RunWith(JUnit4.class)
public class TypescriptPackageJsonTest {

    @Test
    public void generateTestInCorrectPackage() throws Exception {

        JavaFileObject o1 = JavaFileObjects
                .forSourceLines("a.b.c.d.e.Test", "",
                        "package a.b.c.d.e;                                                                                                  ",
                        "                                                                                                   ",
                        "import " + BBBJson.class.getName() + ";                                                  ",
                        "import " + BBBImmutable.class.getName() + ";                                                  ",
                        "import " + BasicTypescriptMapping.class.getName() + ";                                                  ",
                        "import " + BBBTypescript.class.getName() + ";                                                  ",
                        "                                                                                                          ",
                        "public class Test {                                                     ",
                        "                                                                                                                                                    ",
                        "   @BasicTypescriptMapping                                 ",
                        "   @BBBTypescript                                 ",
                        "   @BBBJson                                                        ",
                        "   public interface AccountDef {                                                  ",
                        "      String getName();                                                  ",
                        "   }                                                                                                                                       ",
                        "}                                                                                                                                                               "

                );

        JavaFileObject o2 = JavaFileObjects
                .forSourceLines("a.b.Pkg", "",
                        "package a.b;                                                                                                  ",
                        "                                                                                                   ",
                        "import " + TypescriptRoot.class.getName() + ";                                                  ",
                        "                                                                                                          ",
                        "@TypescriptRoot                                 ",
                        "public class Pkg {                                                     ",
                        "}                                                                                                                                                               "

                );
        Helper.hasNoCompileErrors(o1, o2);
    }


}

