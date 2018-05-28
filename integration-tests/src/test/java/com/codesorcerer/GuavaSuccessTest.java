package com.codesorcerer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.codesorcerer.Helper.compiles;

@RunWith(JUnit4.class)
public class GuavaSuccessTest {


    @Test
    public void comparable() throws Exception {
        compiles("",
                "                                                            ",
                "           @BBBGuava                                                 ",
                "           interface CompDef {                                                  ",
                "              Long getThing1();                                                  ",
                "              long getThing2();                                                  ",
                "           }                                                  ",
                "                                                            ",
                "          class Usage {                                                                                                                 ",
                "             void test() {                                                                                                                 ",
                "               Ordering x = CompGuava.ORDER_BY_THING1;                                                  ",
                "               Ordering y = CompGuava.ORDER_BY_THING2;                                ",
                "             }                                                                                                                        ",
                "          }                                                                                                                        "
        );
    }


}

