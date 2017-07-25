package com.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.test.bean.sub.ChildA;
import com.test.bean.sub.ParentXDef;
import com.test.takeshi.IncreasedAuthChallengeQuestion;
import com.test.takeshi.IncreasedAuthData;
import com.test.template.Complex;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestJson {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonSerialization() throws IOException {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        {
            final Complex b = Complex.buildComplex()
                    .title1("totle")
                    .title2("title2")
                    .title3("tit3")
                    .newSimple1()
                    .name("sim1")
                    .done()
                    .newSimple2()
                    .name("sim2")
                    .done()
                    .build();


            final String serialized = mapper.writeValueAsString(b);
            System.out.println(serialized);
            final Complex b2 = mapper.readValue(serialized, Complex.class);
            Assert.assertTrue(b.equals(b2));
        }

        {
            IncreasedAuthChallengeQuestion iacq = IncreasedAuthChallengeQuestion.buildIncreasedAuthChallengeQuestion()
                    .actualAnswer("")
                    .build();

            IncreasedAuthData x = IncreasedAuthData.buildIncreasedAuthData()
                    .challengeQuestions(ImmutableList.of(iacq))
                    .build();

            final String serialized2 = mapper.writeValueAsString(x);
            System.out.println(serialized2);
            Assert.assertFalse(serialized2.contains("List"));
            final IncreasedAuthData x2 = mapper.readValue(serialized2, IncreasedAuthData.class);
        }

        {
            ParentXDef p = ChildA.buildChildA()
                    .thing("thing")
                    .thingA("thingA")
                    .build();

            final String serialized2 = mapper.writeValueAsString(p);
            System.out.println(serialized2);
            final ParentXDef p2 = mapper.readValue(serialized2, ParentXDef.class);


        }


    }

}
