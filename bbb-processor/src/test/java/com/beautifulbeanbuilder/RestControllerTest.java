package com.beautifulbeanbuilder;


import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

@RunWith(JUnit4.class)
public class RestControllerTest {

    @Test
    public void simple() throws Exception {

        JavaFileObject o = JavaFileObjects
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
                        "@RestController                                                                                                          ",
                        "public class AccountsRestController {                                                     ",
                        "                                                                                   ",
                        "            @RequestMapping(value = \"/api/accounts/\", method = POST)                                                     ",
                        "            public Single<Boolean> addAccount(@RequestBody String a) {                                                     ",
                        "                return Single.just(Boolean.TRUE);                                                     ",
                        "            }                                                                                                          ",
                        "                                                                                                          ",
                        "            @SubscribeMapping(\"/queue/accounts\")                                                                                                          ",
                        "            public Observable<List<String>> accounts() {                                                     ",
                        "                return Observable.just(new ArrayList<String>());                                                     ",
                        "            }                                                                                                          ",
                        "                                                                                                          ",
                        "}                                                                                                                                                               ",
                        "                                                                                                                                       "
                );

        Helper.hasNoCompileErrors(o);
    }

}

