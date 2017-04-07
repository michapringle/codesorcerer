package com.beautifulbeanbuilder;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.beautifulbeanbuilder.Helper.compiles;

@RunWith(JUnit4.class)
public class JacksonSuccessTest {

    @Test
    public void testGenCode() throws Exception {
        compiles("",
                "          @BBBJson                                                 ",
                "          public interface BeanDef {                                                  ",
                "            int getThing();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "          class Usage {                                                  ",
                "             void test() {                                                  ",
                "               Bean x = Bean.buildBean()                                                  ",
                "                  .thing(1)                                                  ",
                "                  .build();                                                  ",
                "                                                                                            ",
                "               BeanJackson.Serializer s;                                                                             ",
                "               BeanJackson.Deserializer d;                                                                             ",
                "                                                                                            ",
                "             }                                                                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "                                                                               "
        );
    }


}

