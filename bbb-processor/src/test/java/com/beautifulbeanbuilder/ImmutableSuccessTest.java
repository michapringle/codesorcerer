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
                "          public interface BeanListOfSubDef {                                                  ",
                "            @Nonnull List<? extends SubBeanListOfSubDef> getSubs();                                                  ",
                "            List<? extends SubBeanListOfSubDef> getSubs1();                                                  ",
                "            Map<String, ? extends SubBeanListOfSubDef> getSubs2();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "           @BBBImmutable                                                 ",
                "           interface SubBeanListOfSubDef {                                                  ",
                "             String getThing1();                                                  ",
                "           }                                                             ",
                "                                                                                    ",
                "           class Usage {                                                  ",
                "             void test() {                                                  ",
                "               BeanListOfSub x = BeanListOfSub.buildBeanListOfSub()                                                  ",
                "               .subs(new ArrayList<SubBeanListOfSub>())                                                  ",
                "               .subs2(new HashMap<String, SubBeanListOfSub>())                                                  ",
                "               .build();                                                  ",
                "                                                                                       ",
                "               List<SubBeanListOfSub> subs = x.getSubs();                                                  ",
                "             }                                                                                                  ",
                "           }                                                                          ",
                "                                                                                  "
        );
    }

    @Test
    public void subBean() throws Exception {
        compiles("",
                "          @com.beautifulbeanbuilder.BeautifulBean                                                 ",
                "          public interface Bean8Def {                                                  ",
                "            Sub8Def getSub();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "           @com.beautifulbeanbuilder.BeautifulBean                                                 ",
                "           interface Sub8Def {                                                  ",
                "             String getThing1();                                                  ",
                "           }                                                                          ",
                "                                                                                    ",
                "           class Usage {                                                  ",
                "             void test() {                                                  ",
                "               Bean8 x = Bean8.buildBean8()                                                  ",
                "               .newSub()                                                  ",
                "                 .thing1(\"hi\")                                                  ",
                "               .done()                                                  ",
                "               .build();                                                  ",
                "                                                                                       ",
                "               Sub8 sub = x.getSub();                                                  ",
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
                "          public interface BeanMemberNonNullDef {                                                  ",
                "             @Nonnull String getThing1();                                                  ",
                "             @Nonnull Boolean getThing2();                                                  ",
                "             Long getThing8();                                          ",
                "          }                                                                          ",
                "                                                                                    ",
                "          class Usage {                                                                                                                 ",
                "             void test() {                                                                                                                 ",
                "                BeanMemberNonNull x = BeanMemberNonNull.buildBeanMemberNonNull()                                                                                     ",
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
                "          public interface BeanNullLastDef {                                                  ",
                "             public static final String CONST = \"abc\";                                                  ",
                "             int getAuthReq();                                                  ",
                "             String getMyStr();                                                  ",
                "             byte[] getStuff();                                                  ",
                "             @Nonnull Integer getMyInt();                                        ",
                "          }                                                                          "
        );
    }


}

