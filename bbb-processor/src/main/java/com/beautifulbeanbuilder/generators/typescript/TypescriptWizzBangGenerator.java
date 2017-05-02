package com.beautifulbeanbuilder.generators.typescript;

import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.beautifulbeanbuilder.processor.info.InfoRestController;
import org.apache.commons.io.FileUtils;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class TypescriptWizzBangGenerator extends AbstractGenerator<RestController, InfoRestController, String> {

    @Override
    public void processingOver(Collection<String> objects) {
    }

    @Override
    public void write(InfoRestController ic, String objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        File dir = FileUtils.getTempDirectory();
        FileUtils.write(new File(dir, ic.typeElement.getSimpleName() + ".ts"), objectToWrite, Charset.defaultCharset());
    }

    @Override
    public String build(InfoRestController ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap) throws IOException {
        String serviceName = ic.typeElement.getSimpleName().toString();


        final StringBuilder sb = new StringBuilder();

        sb.append("import {Injectable} from '@angular/core';\n");
        sb.append("import {Http} from '@angular/http';\n");
        sb.append("import {StompClient} from './stomp.client';\n");

        sb.append("@Injectable()\n");
        sb.append("export class " + serviceName + "Service {\n");

        sb.append("     constructor( private stompClient: StompClient, private http: Http) {\n");
        sb.append("          stompClient.connect();\n");
        sb.append("     }\n");

        sb.append("\n");

        for (ExecutableElement e : ic.getAllMethodsStomp()) {
            SubscribeMapping rm = e.getAnnotation(SubscribeMapping.class);
            sb.append("     readonly " + e.getSimpleName() + " = this.stompClient.topic( '" + rm.value()[0] + "' );\n");
        }


        for (ExecutableElement e : ic.getAllMethodsRest()) {
            sb.append("\n");

            RequestMapping rm = e.getAnnotation(RequestMapping.class);

            boolean post = Arrays.stream(rm.method()).anyMatch(x -> x == RequestMethod.POST);
            if (post) {
                VariableElement requestBodyParameter = e.getParameters().stream().filter(p -> p.getAnnotation(RequestBody.class) != null).findFirst().get();
                String body = TypescriptGenerator.convertTypes(requestBodyParameter.asType());
                sb.append("     " + e.getSimpleName() + "( body : " + body + ") {\n");
                sb.append("         this.http.post( '" + rm.value()[0] + "', body ).subscribe();\n");
                sb.append("     }\n");
            }

            boolean get = Arrays.stream(rm.method()).anyMatch(x -> x == RequestMethod.GET);
            if (get) {
                sb.append("     " + e.getSimpleName() + "() {\n");
                sb.append("         this.http.get( '" + rm.value()[0] + "').subscribe();\n");
                sb.append("     }\n");
            }

        }
        sb.append(" }\n");
        sb.append(" ;\n");

        System.out.println(sb);
        return sb.toString();
    }

}
