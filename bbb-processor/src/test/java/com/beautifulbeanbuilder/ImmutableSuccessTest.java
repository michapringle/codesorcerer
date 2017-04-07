package com.beautifulbeanbuilder;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.beautifulbeanbuilder.Helper.compiles;

@RunWith(JUnit4.class)
public class ImmutableSuccessTest {

    @Test
    public void listOfSubBeans() throws Exception {
        compiles("",
                "          @BBBImmutable                                                 ",
                "          public interface BeanDef {                                                  ",
                "            List<? extends SubDef> getSubs();                                                  ",
                "            Map<String, ? extends SubDef> getSubs2();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "           @BBBImmutable                                                 ",
                "           interface SubDef {                                                  ",
                "             String getThing1();                                                  ",
                "           }                                                                          ",
                "                                                                                    ",
                "           class Usage {                                                  ",
                "             void test() {                                                  ",
                "               Bean x = Bean.buildBean()                                                  ",
                "               .subs(new ArrayList<Sub>())                                                  ",
                "               .subs2(new HashMap<String, Sub>())                                                  ",
                "               .build();                                                  ",
                "                                                                                       ",
                "               List<Sub> subs = x.getSubs();                                                  ",
                "             }                                                                                                  ",
                "           }                                                                          ",
                "                                                                                  "
        );
    }

    @Test
    public void subBean() throws Exception {
        compiles("",
                "          @com.beautifulbeanbuilder.BeautifulBean                                                 ",
                "          public interface BeanDef {                                                  ",
                "            SubDef getSub();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "           @com.beautifulbeanbuilder.BeautifulBean                                                 ",
                "           interface SubDef {                                                  ",
                "             String getThing1();                                                  ",
                "           }                                                                          ",
                "                                                                                    ",
                "           class Usage {                                                  ",
                "             void test() {                                                  ",
                "               Bean x = Bean.buildBean()                                                  ",
                "               .newSub()                                                  ",
                "                 .thing1(\"hi\")                                                  ",
                "               .done()                                                  ",
                "               .build();                                                  ",
                "                                                                                       ",
                "               Sub sub = x.getSub();                                                  ",
                "             }                                                                                                  ",
                "           }                                                                          ",
                "          "
        );
    }

    @Test
    public void memberFieldsNonNull() throws Exception {
        compiles("",
                "                                                            ",
                "          @BBBImmutable                                                 ",
                "          public interface BeanDef {                                                  ",
                "             @Nonnull String getThing1();                                                  ",
                "             @Nonnull Boolean getThing2();                                                  ",
                "             Long getThing8();                                          ",
                "          }                                                                          ",
                "                                                                                    ",
                "          class Usage {                                                                                                                 ",
                "             void test() {                                                                                                                 ",
                "                Bean x = Bean.buildBean()                                                                                     ",
                "                .thing1(\"x\")                                                                                                                        ",
                "                .thing2(true)                                                                                                                        ",
                "                .build();                                                                                                                        ",
                "             }                                                                                                                        ",
                "          }                                                                                                                        "

        );
    }

    @Test
    public void nonNullLast() throws Exception {
        compiles("",
                "                                                            ",
                "          @BBBImmutable                                                 ",
                "          public interface BeanDef {                                                  ",
                "             public static final String CONST = \"abc\";                                                  ",
                "             int getAuthReq();                                                  ",
                "             String getMyStr();                                                  ",
                "             byte[] getStuff();                                                  ",
                "             @Nonnull Integer getMyInt();                                        ",
                "          }                                                                          "
        );
    }

}

