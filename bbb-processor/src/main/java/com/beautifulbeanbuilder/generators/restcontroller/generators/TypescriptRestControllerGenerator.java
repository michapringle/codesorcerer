package com.beautifulbeanbuilder.generators.restcontroller.generators;

import com.beautifulbeanbuilder.TypescriptController;
import com.beautifulbeanbuilder.TypescriptMapping;
import com.beautifulbeanbuilder.generators.beandef.generators.TypescriptGenerator;
import com.beautifulbeanbuilder.generators.restcontroller.RestControllerInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.beautifulbeanbuilder.typescript.TSUtils;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class TypescriptRestControllerGenerator extends AbstractGenerator<TypescriptController, RestControllerInfo, String> {

    @Override
    public void processingOver(Collection<String> objects) {
    }

    @Override
    public void write(RestControllerInfo ic, String objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        FileUtils.write(new File(TypescriptGenerator.DIR, ic.getCurrentTypePackage() + "." + ic.typeElement.getSimpleName() + ".ts"), objectToWrite, Charset.defaultCharset());
    }

    Set<TypeMirror> referenced = Sets.newHashSet();

    @Override
    public String build(RestControllerInfo ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnv) throws IOException {
        String serviceName = ic.typeElement.getSimpleName().toString();


        final StringBuilder sb = new StringBuilder();
        //  sb.append("namespace '" + ic.getCurrentTypePackage() +"' {");
        sb.append("import {Injectable} from '@angular/core';\n");
        sb.append("import {Http} from '@angular/http';\n");
        sb.append("import {StompClient} from './stomp.client';\n");


        //import {Account} from "test";
        sb.append("*IMPORTS*");

        sb.append("\n");
        sb.append("@Injectable()\n");
        sb.append("export class " + serviceName + "Service {\n");

        sb.append("     constructor( private stompClient: StompClient, private http: Http) {}\n");

        sb.append("\n");

        for (ExecutableElement e : ic.getAllMethodsStomp()) {
            addReferences(e);

            SubscribeMapping rm = e.getAnnotation(SubscribeMapping.class);
            sb.append("     public readonly " + e.getSimpleName() + " : " + TSUtils.convertTypes(e.getReturnType()) + " = this.stompClient.topic( '" + rm.value()[0] + "' );\n");
        }


        for (ExecutableElement e : ic.getAllMethodsRest()) {
            addReferences(e);

            sb.append("\n");

            RequestMapping rm = e.getAnnotation(RequestMapping.class);
            boolean post = Arrays.stream(rm.method()).anyMatch(x -> x == RequestMethod.POST);
            if (post) {
                List<? extends VariableElement> mmm = e.getParameters();
                VariableElement requestBodyParameter = e.getParameters().stream().filter(p -> p.getAnnotation(RequestBody.class) != null).findFirst().get();
                referenced.add(requestBodyParameter.asType());
                String body = TSUtils.convertTypes(requestBodyParameter.asType());
                sb.append("     public " + e.getSimpleName() + "( body : " + body + ") : " + TSUtils.convertTypes(e.getReturnType()) + " {\n");
                sb.append("         return this.http.post( '" + rm.value()[0] + "', body )\n");
                sb.append("                         .map( r => r.json());\n");
                sb.append("     }\n");
            }

            boolean get = Arrays.stream(rm.method()).anyMatch(x -> x == RequestMethod.GET);
            if (get) {
                sb.append("     public " + e.getSimpleName() + "() : " + TSUtils.convertTypes(e.getReturnType()) + " {\n");
                sb.append("         return this.http.get( '" + rm.value()[0] + "')\n");
                sb.append("                         .map( r => r.json());\n");
                sb.append("     }\n");
            }


        }
        sb.append(" }\n");
        sb.append("\n");
        //   sb.append("}");

//        TypescriptMappings tm = ic.typeElement.getAnnotation(TypescriptMappings.class);
        Set<TypescriptMapping> mappings = TSUtils.getAllMappings(ic.typeElement);
        String imports = TSUtils.convertToImportStatements(referenced, mappings, processingEnv);

        String x = sb.toString().replace("*IMPORTS*", imports);

        //   System.out.println(x);
        return x;
    }

    private void addReferences(ExecutableElement e) {
        referenced.add(e.getReturnType());
        RequestMapping rm = e.getAnnotation(RequestMapping.class);
        e.getParameters().stream().forEach(p -> referenced.add(p.asType()));
    }

}
