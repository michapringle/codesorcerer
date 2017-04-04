package com.dp;


import com.beautifulbeanbuilder.processor.BBBProcessor;
import com.beautifulbeanbuilder.BeautifulBean;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nonnull;
import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

@RunWith(JUnit4.class)
public class BBBProcessorTest {

    @Test
    public void ListOfBeans() throws Exception {
        List<String> start = Arrays.asList(
                "package test;",
                "",
                "import com.beautifulbeanbuilder.BeautifulBean;",
                "import javax.annotation.Nonnull;",
                "import java.util.List;",
                "",
                "@BeautifulBean",
                "public interface HelloWorldDef {",
                "  List<? extends SubDef> getSubs();",
                "",
                "}",
                "",
                " @BeautifulBean",
                " interface SubDef {",
                "   String getThing1();",
                " }",
                ""
        );

        hasNoCompileErrors(JavaFileObjects.forSourceLines("test.HelloWorldDef", start));
    }

    @Test
    public void subbean() throws Exception {
        List<String> start = Arrays.asList(
                "package test;",
                "",
                "import com.beautifulbeanbuilder.BeautifulBean;",
                "import javax.annotation.Nonnull;",
                "",
                "@BeautifulBean",
                "public interface HelloWorldDef {",
                "  SubDef getSub();",
                "",
                "}",
                "",
                " @BeautifulBean",
                " interface SubDef {",
                "   String getThing1();",
                " }",
                ""
        );

        hasNoCompileErrors(JavaFileObjects.forSourceLines("test.HelloWorldDef", start));
    }

    @Test
    public void memberFieldsNonNull() throws Exception {
        JavaFileObject source = forStandardDef(
                "",
                "@Nonnull String getThing1();",
                "@Nonnull Boolean getThing2();",
                "Long getThing8();"
        );
        hasNoCompileErrors(source);
    }

    @Test
    public void nonNullLast() throws Exception {
        JavaFileObject source = forStandardDef(
                "",
                "public static final String CONST = \"abc\";",
                "int getAuthReq();",
                "String getMyStr();",
                "byte[] getStuff();",
                "@Nonnull Integer getMyInt();"
        );
        hasNoCompileErrors(source);
    }

    @Test
    public void comparable() throws Exception {
        JavaFileObject source = forStandardDef(
                "",
                " @BeautifulBean",
                " interface CompDef {",
                "Long getThing1();",
                "long getThing2();",
                " }",
                "",
                "Ordering x = CompGuava.ORDER_BY_THING1;",
                "Ordering y = CompGuava.ORDER_BY_THING2;"
        );
        hasNoCompileErrors(source);
    }


    @Test
    public void memberFields() throws Exception {
        JavaFileObject source = forStandardDef(
                "",
                "String getThing1();",
                "Boolean getThing2();",
                "Long getThing8();"
        );
        hasNoCompileErrors(source);
    }


    @Test
    public void basic() throws Exception {
        JavaFileObject source = forStandardDef();
        hasNoCompileErrors(source);
    }


    @Test
    public void notEndingWithDef() throws Exception {
        JavaFileObject source = JavaFileObjects
                .forSourceLines("test.HelloWorldDef__",
                        "",
                        "package test;",
                        "",
                        "import com.beautifulbeanbuilder.BeautifulBean;",
                        "",
                        "@BeautifulBean",
                        "public interface HelloWorldDef__ {",
                        "}");

        hasCompileError(source, "Must end with Def");
    }


//    @Test
//    public void notAbstractClass() throws Exception {
//        JavaFileObject source = JavaFileObjects
//                .forSourceLines("test.HelloWorldDef",
//                        "",
//                        "package test;",
//                        "",
//                        "import com.beautifulbeanbuilder.BeautifulBean;",
//                        "",
//                        "@BeautifulBean",
//                        "public class HelloWorldDef {",
//                        "}");
//
//        hasCompileError(source, "abstract class");
//    }

    public static JavaFileObject forStandardDef(String... lines) {

        List<String> start = Arrays.asList(
                "package test;",
                "",
                "import " + BeautifulBean.class.getName() + ";",
                "import " + Ordering.class.getName() + ";",
                "import " + Nonnull.class.getName() + ";",
                "",
                "@BeautifulBean",
                "public interface HelloWorldDef {"
        );

        List<String> end = Arrays.asList(
                "}"
        );

        Iterable allLines = Iterables.concat(start, Arrays.asList(lines), end);

        return JavaFileObjects
                .forSourceLines("test.HelloWorldDef", allLines);

    }


    private void hasCompileError(JavaFileObject source, String msg) {
        assertAbout(javaSource()).that(source)
                .processedWith(new BBBProcessor())
                .failsToCompile()
                .withErrorContaining(msg);
    }


    private void hasNoCompileErrors(JavaFileObject source) {
        assertAbout(javaSource()).that(source)
                .processedWith(new BBBProcessor())
                .compilesWithoutError();
    }
}

