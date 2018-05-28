package com.codesorcerer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.codesorcerer.Helper.compiles;

@RunWith(JUnit4.class)
public class MutableSuccessTest {

    @Test
    public void toFromMutable() throws Exception {
        compiles("",
                "          @BBBMutable                                                 ",
                "          public interface BeanMut1Def {                                                                                                        ",
                "            String getThing();                                                                                                     ",
                "          }                                                                                                                             ",
                "                                                                                                                                           ",
                "          class Usage {                                                                                                                 ",
                "             void test() {                                                                                                                 ",
                "                BeanMut1Mutable x = new BeanMut1Mutable();                                                                                     ",
                "                x.setThing(\"x\");                                                                                                                        ",
                "                BeanMut1Mutable y = x.toImmutable().toMutable();                                                                                     ",
                "             }                                                                                                                        ",
                "          }                                                                                                                        ",
                "                                                                                                                   "
        );
    }


}

