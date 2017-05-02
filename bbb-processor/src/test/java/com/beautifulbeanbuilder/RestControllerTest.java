package com.beautifulbeanbuilder;


import com.beautifulbeanbuilder.generators.beandef.BeanDefProcessor;
import com.beautifulbeanbuilder.generators.restcontroller.RestControllerProcessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nonnull;
import javax.tools.JavaFileObject;
import java.util.*;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

@RunWith(JUnit4.class)
public class RestControllerTest {

    @Test
    public void simple() throws Exception {

        JavaFileObject o1 = JavaFileObjects
                .forSourceLines("test.AccountDef", "",
                        "package test;                                                                                                  ",
                        "                                                                                                   ",
                        "import " + BBBJson.class.getName() + ";                                                  ",
                        "import " + BBBImmutable.class.getName() + ";                                                  ",
                        "import " + BBBTypescript.class.getName() + ";                                                  ",
                        "                                                                                                                                                    ",
                        "   @BBBTypescript                                                 ",
                        "   @BBBJson                                                        ",
                        "   @BBBImmutable                                                        ",
                        "   public interface AccountDef {                                                  ",
                        "      String getName();                                                  ",
                        "      int getAmount();                                                  ",
                        "}                                                                                                                                       "
                );

        JavaFileObject o2 = JavaFileObjects
                .forSourceLines("test.AccountsRestController", "",
                        "package test;                                                                                                  ",
                        "                                                                                                   ",
                        "import io.reactivex.Observable;                                                                                                          ",
                        "import io.reactivex.Single;                                                                                                          ",
                        "import org.springframework.messaging.simp.annotation.SubscribeMapping;                                                     ",
                        "import org.springframework.web.bind.annotation.RequestBody;                                                     ",
                        "import org.springframework.web.bind.annotation.RequestMapping;                                                     ",
                        "import org.springframework.web.bind.annotation.RestController;                                                     ",
                        "                                                                                                                                                               ",
                        "import java.util.List;                                                                                                          ",
                        "import java.util.ArrayList;                                                                                                          ",
                        "                                                                                                          ",
                        "import static org.springframework.web.bind.annotation.RequestMethod.POST;                                                     ",
                        "                                                                                                                                                    ",
                        "                                                                                                                                                    ",
                        "   @RestController                                                                                                          ",
                        "   public class AccountsRestController {                                                     ",
                        "                                                                                   ",
                        "            @RequestMapping(value = \"/api/accounts/\", method = POST)                                                     ",
                        "            public Single<Boolean> addAccount(@RequestBody Account a) {                                                     ",
                        "                return Single.just(Boolean.TRUE);                                                     ",
                        "            }                                                                                                          ",
                        "                                                                                                          ",
                        "            @SubscribeMapping(\"/queue/accounts\")                                                                                                          ",
                        "            public Observable<List<Account>> accounts() {                                                     ",
                        "                return Observable.just(new ArrayList<Account>());                                                     ",
                        "            }                                                                                                          ",
                        "   }                                                                                                                                                               "
                );

        Helper.hasNoCompileErrors( o1, o2);
    }

}

