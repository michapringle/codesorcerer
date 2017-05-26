package com.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.template.BeanVal;
import com.test.template.Complex;
import com.test.template.Complex2;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.test.template.BeanVal.buildBeanVal;

public class TestJson {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonSerialization() throws IOException {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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

}
