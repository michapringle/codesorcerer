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
                "            BeanJack2Def getJack2();                                                  ",
                "          }                                                                          ",

                "          @BBBJson                                                 ",
                "          public interface BeanJack2Def {                                                  ",
                "            int getThing();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "          class Usage {                                                  ",
                "             void test() throws Exception {                                                  ",
                "               BeanJack1 x = BeanJack1.buildBeanJack1()                                                  ",
                "                  .thing(1)                                                  ",
                "                  .newJack2()                                                  ",
                "                    .thing(2)                                                  ",
                "                    .done()                                                  ",
                "                  .build();                                                  ",
                "                                                                                            ",
                "               BeanJack1Jackson.Serializer s;                                                                             ",
                "               BeanJack1Jackson.Deserializer d;                                                                             ",
                "                                                                                            ",
                "                                                                                            ",
                "               ObjectMapper mapper = new ObjectMapper();                                                                             ",
                "               String jsonInString = mapper.writeValueAsString(x);                                                                             ",
                "               System.out.println(jsonInString);                                                                             ",
                "               BeanJack1 xx = mapper.readValue(jsonInString, BeanJack1.class);                                                                             ",
                "               System.out.println(xx);                                                                             ",
                "                                                                                            ",
                "             }                                                                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "                                                                               "
        );
    }


}

