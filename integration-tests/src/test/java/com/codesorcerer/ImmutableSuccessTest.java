package com.codesorcerer;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.codesorcerer.Helper.compiles;

@RunWith(JUnit4.class)
public class ImmutableSuccessTest {


    @Test
    public void genericParent() throws Exception {
        compiles("",
                "          public interface Parent<X> {                                                  ",
                "            X getThing();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "           @com.codesorcerer.targets.BeautifulBean                                                 ",
                "           interface RealDef extends Parent<String> {                                                  ",
                "           }                                                                          ",
                "                                                                                    ",
                "           class Usage {                                                  ",
                "             void test() {                                                  ",
                "               Real x = Real.buildReal()                                                  ",
                "                 .thing(\"hi\")                                                  ",
                "                 .build();                                                  ",
                "                                                                                       ",
                "               String s = x.getThing();                                                  ",
                "             }                                                                                                  ",
                "           }                                                                          ",
                "          "
        );
    }

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
                "          @com.codesorcerer.targets.BeautifulBean                                                 ",
                "          public interface Bean8Def {                                                  ",
                "            Sub8Def getSub();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "           @com.codesorcerer.targets.BeautifulBean                                                 ",
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
                "             @Nonnull int getThing3();                                                  ",
                "             @Nonnull Boolean getThing2();                                                  ",
                "             Long getThing8();                                          ",
                "          }                                                                          ",
                "                                                                                    ",
                "          class Usage {                                                                                                                 ",
                "             void test() {                                                                                                                 ",
                "                BeanMemberNonNull x = BeanMemberNonNull.buildBeanMemberNonNull()                                                                                     ",
                "                .thing1(\"x\")                                                                                                                        ",
                "                .thing3(3)                                                                                                                        ",
                "                .thing2(true)                                                                                                                        ",
                "                .thing8(6L)                                                                                                                        ",
                "                .build();                                                                                                                        ",
                "             }                                                                                                                        ",
                "          }                                                                                                                        "

        );
    }

    @Test
    public void memberFieldsNonNullWithParentNullable() throws Exception {
        compiles("",
                "                                                            ",
                "          public interface Test {                                                  ",
                "             String getThing1();                                                  ",
                "             @Nonnull boolean getThing2();                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "          @BBBImmutable                                                 ",
                "          public interface BeanMemberNonNullDef extends Test {                                                  ",
                "             @Nonnull String getThing1();                                                  ",
                "             @Nonnull int getThing3();                                                  ",
                "             Long getThing8();                                          ",
                "          }                                                                          ",
                "                                                                                    ",
                "          class Usage {                                                                                                                 ",
                "             void test() {                                                                                                                 ",
                "                BeanMemberNonNull x = BeanMemberNonNull.buildBeanMemberNonNull()                                                                                     ",
                "                .thing1(\"x\")                                                                                                                        ",
                "                .thing2(true)                                                                                                                        ",
                "                .thing3(3)                                                                                                                        ",
                "                .thing8(8L)                                                                                                                        ",
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

