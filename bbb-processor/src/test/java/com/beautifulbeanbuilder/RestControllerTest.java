package com.beautifulbeanbuilder;


import com.google.common.collect.Ordering;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.annotation.Nonnull;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class RestControllerTest {

    @Test
    public void simple() throws Exception {

        JavaFileObject o1 = JavaFileObjects
                .forSourceLines("test.Test", "",
                        "package test;                                                                                                  ",
                        "                                                                                                   ",
                        "import io.reactivex.Observable;                                                                                                          ",
                        "import io.reactivex.Single;                                                                                                          ",
                        "import org.springframework.messaging.simp.annotation.SubscribeMapping;                                                     ",
                        "import org.springframework.web.bind.annotation.RequestBody;                                                     ",
                        "import org.springframework.web.bind.annotation.RequestMapping;                                                     ",
                        "import org.springframework.web.bind.annotation.RestController;                                                     ",
                        "import " + BBBMutable.class.getName() + ";                                                  ",
                        "import " + BBBJson.class.getName() + ";                                                  ",
                        "import " + BBBGuava.class.getName() + ";                                                  ",
                        "import " + BBBImmutable.class.getName() + ";                                                  ",
                        "import " + BBBTypescript.class.getName() + ";                                                  ",
                        "import " + Ordering.class.getName() + ";                                                  ",
                        "import " + Nonnull.class.getName() + ";                                                  ",
                        "import " + List.class.getName() + ";                                                  ",
                        "import " + Map.class.getName() + ";                                                  ",
                        "import " + ArrayList.class.getName() + ";                                                  ",
                        "import " + HashMap.class.getName() + ";                                                  ",
                        "                                                                                                                                                               ",
                        "import java.util.List;                                                                                                          ",
                        "import java.util.ArrayList;                                                                                                          ",
                        "                                                                                                          ",
                        "import static org.springframework.web.bind.annotation.RequestMethod.POST;                                                     ",
                        "                                                                                                                                                    ",
                        "                                                                                                                                                    ",
                        "public class Test {                                                     ",
                        "                                                                                                                                                    ",
                        "   @BBBTypescript                                                 ",
                        "   @BBBJson                                                        ",
                        "   @BBBImmutable                                                        ",
                        "   public interface AccountDef {                                                  ",
                        "      String getName();                                                  ",
                        "      int getAmount();                                                  ",
                        "   }                                                                          ",
                        "}                                                                                                                                       "
                );
        JavaFileObject o2 = JavaFileObjects
                .forSourceLines("test.Test2", "",
                        "package test;                                                                                                  ",
                        "                                                                                                   ",
                        "import io.reactivex.Observable;                                                                                                          ",
                        "import io.reactivex.Single;                                                                                                          ",
                        "import org.springframework.messaging.simp.annotation.SubscribeMapping;                                                     ",
                        "import org.springframework.web.bind.annotation.RequestBody;                                                     ",
                        "import org.springframework.web.bind.annotation.RequestMapping;                                                     ",
                        "import org.springframework.web.bind.annotation.RestController;                                                     ",
                        "import " + BBBMutable.class.getName() + ";                                                  ",
                        "import " + BBBJson.class.getName() + ";                                                  ",
                        "import " + BBBGuava.class.getName() + ";                                                  ",
                        "import " + BBBImmutable.class.getName() + ";                                                  ",
                        "import " + BBBTypescript.class.getName() + ";                                                  ",
                        "import " + Ordering.class.getName() + ";                                                  ",
                        "import " + Nonnull.class.getName() + ";                                                  ",
                        "import " + List.class.getName() + ";                                                  ",
                        "import " + Map.class.getName() + ";                                                  ",
                        "import " + ArrayList.class.getName() + ";                                                  ",
                        "import " + HashMap.class.getName() + ";                                                  ",
                        "                                                                                                                                                               ",
                        "import java.util.List;                                                                                                          ",
                        "import java.util.ArrayList;                                                                                                          ",
                        "                                                                                                          ",
                        "import static org.springframework.web.bind.annotation.RequestMethod.POST;                                                     ",
                        "                                                                                                                                                    ",
                        "                                                                                                                                                    ",
                        "public class Test2 {                                                     ",
                        "                                                                                                                                                    ",
                        "                                                                                   ",
                        "   @RestController                                                                                                          ",
                        "   public static class AccountsRestController {                                                     ",
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
                        "}                                                                                                                                       "
                );

        Helper.hasNoCompileErrors( o1, o2);
    }

}

