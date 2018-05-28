package com.codesorcerer;


import com.codesorcerer.targets.*;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;
import java.math.BigDecimal;

@RunWith(JUnit4.class)
public class RestControllerTest {

    @Test
    public void simple2() throws Exception {

        JavaFileObject o1 = JavaFileObjects
                .forSourceLines("test.Test", "",
                        "package test;                                                                                                  ",
                        "                                                                                                   ",
                        "import " + BBBJson.class.getName() + ";                                                  ",
                        "import " + BigDecimal.class.getName() + ";                                                  ",
                        "import " + BBBImmutable.class.getName() + ";                                                  ",
                        "import " + BasicTypescriptMapping.class.getName() + ";                                                  ",
                        "import " + TypescriptController.class.getName() + ";                                                  ",
                        "import " + TypescriptMapping.class.getName() + ";                                                  ",
                        "import " + BBBTypescript.class.getName() + ";                                                  ",
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
                        "public class Test {                                                     ",

                        "                                                                                                                                                    ",
                        "   @TypescriptMapping(javaClass = Observable.class, typescriptClassName = \"Observable\", typescriptImportLocation = \"./rxjs\")                                                                                                                                                 ",
                        "   @TypescriptMapping(javaClass = Single.class, typescriptClassName = \"Single\", typescriptImportLocation = \"./rxjs\")                                                                                                                                                 ",
                        "   @BasicTypescriptMapping                                                 ",
                        "   public @interface C1Mappings {                                                  ",
                        "   }                                                                                                                                       ",
                        "                                                                                                                                                    ",
                        "   @TypescriptController                                                 ",
                        "   @C1Mappings                                                 ",
                        "   public @interface C1Controller {                                                  ",
                        "   }                                                                                                                                                                                ",
                        "                                                                                                                                                    ",
                        "   @C1Mappings                                                                                                                                                ",
                        "   @BBBTypescript                                 ",
                        "   @BBBJson                                                        ",
                        "   public @interface C1Bean {                                                  ",
                        "   }                                                                                                                                                                                ",
                        "                                                                                                                                                    ",
                        "   @C1Bean                                                                                                                                                ",
                        "   public interface AccountDef {                                                  ",
                        "      String getName();                                                  ",
                        "      int getAmount();                                                  ",
                        "      BigDecimal getBDec();                                                  ",
                        "   }                                                                                                                                       ",
                        "                                                                                                                                                    ",
                        "   @C1Controller                                                                                                                                                ",
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
                        "   }                                                                                                                                                               ",
                        "}                                                                                                                                                               "

                );
        Helper.hasNoCompileErrors(o1);
    }


    @Test
    public void simple() throws Exception {

        JavaFileObject o1 = JavaFileObjects
                .forSourceLines("testx.AccountDef", "",
                        "package testx;                                                                                                  ",
                        "                                                                                                   ",
                        "import " + BBBJson.class.getName() + ";                                                  ",
                        "import " + BigDecimal.class.getName() + ";                                                  ",
                        "import " + BBBImmutable.class.getName() + ";                                                  ",
                        "import " + BBBTypescript.class.getName() + ";                                                  ",
                        "import " + BasicTypescriptMapping.class.getName() + ";                                                  ",
                        "                                                                                                                                                    ",
                        "   @BBBTypescript                                                 ",
                        "   @BBBJson                                                        ",
                        "   @BBBImmutable                                                        ",
                        "   @BasicTypescriptMapping                                                       ",
                        "   public interface AccountDef {                                                  ",
                        "      String getName();                                                  ",
                        "      int getAmount();                                                  ",
                        "      Long getAmount2();                                                  ",
                        "      BigDecimal getBDec();                                                  ",
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
                        "import testx.Account;                                                                                                          ",
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

        Helper.hasNoCompileErrors(o1, o2);
        Helper.hasNoCompileErrors(o2, o1);
    }

}

