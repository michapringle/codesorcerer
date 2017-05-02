package com.beautifulbeanbuilder;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.beautifulbeanbuilder.Helper.compiles;

@RunWith(JUnit4.class)
public class TypescriptTest {

    @Test
    public void simple() throws Exception {
        compiles("",
                "          @BBBTypescript                                                 ",
                "          @BBBJson                                                        ",
                "          @BBBImmutable                                                        ",
                "          public interface BeanDef {                                                  ",
                "            String getName();                                                  ",
                "            int getAge();                                                  ",
                "            @Nonnull String getLastName();                                                  ",
                "            @Nonnull String getSin();                                                  ",
                "          }                                                                          ",
                "                                                                                  "
        );
    }

}

