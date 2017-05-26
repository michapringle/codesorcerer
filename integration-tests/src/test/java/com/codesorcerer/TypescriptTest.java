package com.codesorcerer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.codesorcerer.Helper.compiles;

@RunWith(JUnit4.class)
public class TypescriptTest {

    @Test
    public void simple() throws Exception {
        compiles("",
                "          @BBBTypescript                                                 ",
                "          @BasicTypescriptMapping                                                       ",
                "          @BBBJson                                                        ",
                "          @BBBImmutable                                                        ",
                "          public interface BeanSimple1Def {                                                  ",
                "            String getName();                                                  ",
                "            int getAge();                                                  ",
                "          }                                                                          ",
                "                                                                                  "
        );
    }

    @Test
    public void collections() throws Exception {
        compiles("",
                "          @BBBTypescript                                                 ",
                "          @BasicTypescriptMapping                                                       ",
                "          @BBBJson                                                        ",
                "          @BBBImmutable                                                        ",
                "          public interface BeanCollDef {                                                  ",
                "            List<String> getNames();                                                  ",
                "            Set<Integer> getAges();                                                  ",
                "            Map<String, Integer> getNameToAge();                                                  ",
                "          }                                                                          ",
                "                                                                                  "
        );
    }

    @Test
    public void nonnull() throws Exception {
        compiles("",
                "          @BBBTypescript                                                 ",
                "          @BasicTypescriptMapping                                                       ",
                "          @BBBJson                                                        ",
                "          @BBBImmutable                                                        ",
                "          public interface BeanNonDef {                                                  ",
                "            @Nonnull String getLastName();                                                  ",
                "            @Nonnull String getSin();                                                  ",
                "          }                                                                          ",
                "                                                                                  "
        );
    }

    @Test
    public void bothNullNonNull() throws Exception {
        compiles("",
                "          @BBBTypescript                                                 ",
                "          @BasicTypescriptMapping                                                       ",
                "          @BBBJson                                                        ",
                "          @BBBImmutable                                                        ",
                "          public interface BeanBothDef {                                                  ",
                "            String getName();                                                  ",
                "            @Nonnull String getLastName();                                                  ",
                "            int getAge();                                                  ",
                "            @Nonnull String getSin();                                                  ",
                "          }                                                                          ",
                "                                                                                  "
        );
    }


    @Test
    public void subbean() throws Exception {
        compiles("",
                "          @BBBTypescript                                                 ",
                "          @BasicTypescriptMapping                                                       ",
                "          @BBBJson                                                        ",
                "          @BBBImmutable                                                        ",
                "          public interface BeanParentDef {                                                  ",
                "            BeanChildDef getChild();                                                  ",
                "          }                                                                          ",
                "                                                                                  ",
                "          @BBBTypescript                                                 ",
                "          @BasicTypescriptMapping                                                       ",
                "          @BBBJson                                                        ",
                "          @BBBImmutable                                                        ",
                "          public interface BeanChildDef {                                                  ",
                "            String getName();                                                  ",
                "          }                                                                          ",
                "                                                                                  "
        );
    }

}

