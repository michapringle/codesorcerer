package com.codesorcerer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.codesorcerer.Helper.compiles;

@RunWith(JUnit4.class)
public class JacksonSuccessTest {

    @Test
    public void testGenCode() throws Exception {
        compiles("",
                "          @BBBJson                                                 ",
                "          public interface BeanJack1Def {                                                  ",
                "            int getThing();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "          class Usage {                                                  ",
                "             void test() {                                                  ",
                "               BeanJack1 x = BeanJack1.buildBeanJack1()                                                  ",
                "                  .thing(1)                                                  ",
                "                  .build();                                                  ",
                "                                                                                            ",
                "               BeanJack1Jackson.Serializer s;                                                                             ",
                "               BeanJack1Jackson.Deserializer d;                                                                             ",
                "                                                                                            ",
                "             }                                                                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "                                                                               "
        );
    }


}

