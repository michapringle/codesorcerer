package com.codesorcerer;


import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.codesorcerer.Helper.compiles;

@RunWith(JUnit4.class)
public class SubclassDispatchTest {

    @Test
    public void subclassSimple() throws Exception {
        compiles("",
                "                                                    ",
                "          @com.codesorcerer.targets.SubclassDispatch                                                  ",
                "          public interface Parent {                                                  ",
                "            public class ParentSub1 implements Parent {                                                  ",
                "            }                                                             ",
                "            public class ParentSub3 implements Parent {                                                  ",
                "            }                                                             ",
                "            public class ParentSub2 implements Parent {                                                  ",
                "            }                                                             ",
                "          }                                                                          ",
                "                                                                                    ",
                "           class Usage {                                                  ",
                "             void test() {                                                  ",
                "               ParentDispatch.createFunction(s1 -> 1, s2 -> 2, s3 -> 3).apply(new Parent.ParentSub1());                                                  ",
                "             }                                                                                                  ",
                "           }                                                                          ",
                "          "
        );
    }


    @Test
    public void subclassOnlyOneLevel() throws Exception {
        compiles("",
                "                                                    ",
                "          @com.codesorcerer.targets.SubclassDispatch                                                  ",
                "          public interface Parent {                                                  ",
                "          }                                                                          ",
                "                                                                                    ",
                "          public class ParentSub2 implements Parent {                                                  ",
                "          }                                                             ",
                "                                                                                    ",
                "          @com.codesorcerer.targets.SubclassDispatch                                                  ",
                "          public abstract class ParentSub1 implements Parent {                                                  ",
                "          }                                                             ",
                "          public class ParentSub1A extends ParentSub1 {                                                  ",
                "          }                                                             ",
                "          public class ParentSub1B extends ParentSub1 {                                                  ",
                "          }                                                             ",
                "                                                                                    ",
                "           class Usage {                                                  ",
                "             void test() {                                                  ",
                "               ParentDispatch.createFunction(s1 -> 1, s2 -> 2).apply(new ParentSub2());                                                  ",
                "             }                                                                                                  ",
                "           }                                                                          ",
                "          "
        );
    }

}

