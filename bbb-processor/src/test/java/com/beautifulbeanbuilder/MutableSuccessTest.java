package com.beautifulbeanbuilder;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.beautifulbeanbuilder.Helper.compiles;

@RunWith(JUnit4.class)
public class MutableSuccessTest {

    @Test
    public void toFromMutable() throws Exception {
        compiles("",
                "          @BBBMutable                                                 ",
                "          public interface BeanDef {                                                                                                        ",
                "            String getThing();                                                                                                     ",
                "          }                                                                                                                             ",
                "                                                                                                                                           ",
                "          class Usage {                                                                                                                 ",
                "             void test() {                                                                                                                 ",
                "                BeanMutable x = new BeanMutable();                                                                                     ",
                "                x.setThing(\"x\");                                                                                                                        ",
                "                BeanMutable y = x.toImmutable().toMutable();                                                                                     ",
                "             }                                                                                                                        ",
                "          }                                                                                                                        ",
                "                                                                                                                   "
        );
    }


}

