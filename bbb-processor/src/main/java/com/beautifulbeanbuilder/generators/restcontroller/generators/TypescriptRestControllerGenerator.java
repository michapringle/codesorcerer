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
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
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
import java.util.stream.Collectors;

public class TypescriptRestControllerGenerator extends AbstractGenerator<TypescriptController, RestControllerInfo, String> {

    @Override
    public void processingOver(Collection<String> objects, ProcessingEnvironment processingEnv) {

    }

    @Override
    public void write(RestControllerInfo ic, String objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        FileUtils.write(new File(TypescriptGenerator.DIR, ic.getCurrentTypePackage() + "." + ic.typeElement.getSimpleName() + ".ts"), objectToWrite, Charset.defaultCharset());
    }

    @Override
    public String build(RestControllerInfo ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnv) throws IOException {
        Set<TypescriptMapping> mappings = TSUtils.getAllMappings(ic.typeElement);
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

        sb.append("//-----------------Constructor\n");
        sb.append("     constructor( private stompClient: StompClient, private http: Http) {}\n");
        sb.append("\n");

        sb.append("//-----------------Stomp Methods\n");
        for (ExecutableElement e : ic.getAllMethodsStomp()) {
            addReferences(e);

            SubscribeMapping rm = e.getAnnotation(SubscribeMapping.class);

            //Topic
            String topic = rm.value()[0];
            for (VariableElement v : e.getParameters()) {
                DestinationVariable dv = v.getAnnotation(DestinationVariable.class);
                topic = topic.replace("{" + dv.value() + "}", "' + " + dv.value() + " + '");
            }

            //Parameters
            String tsParameterList = e.getParameters()
                    .stream()
                    .map(v -> {
                        DestinationVariable dv = v.getAnnotation(DestinationVariable.class);
                        String tsType = TSUtils.convertToTypescriptType(v.asType(), mappings, processingEnv);
                        return dv.value() + " : " + tsType;
                        //topic = topic.replace("{" + dv.value() + "}", "' + " + dv.value() + "'");
                    })
                    .collect(Collectors.joining(","));

            sb.append("\n");
            sb.append("public " + e.getSimpleName() + "(" + tsParameterList + "): " + TSUtils.convertToTypescriptType(e.getReturnType(), mappings, processingEnv) + " {\n");
            sb.append("    return this.stompClient.topic('" + topic + "')\n");
            sb.append("}\n");
        }
        sb.append("\n");


        sb.append("//-----------------Rest Methods\n");
        for (
                ExecutableElement e : ic.getAllMethodsRest())

        {
            addReferences(e);

            sb.append("\n");

            final RequestMapping rm = e.getAnnotation(RequestMapping.class);
            final boolean post = Arrays.stream(rm.method()).allMatch(x -> x == RequestMethod.POST);
            if (!post) {
                throw new RuntimeException("Can only handle posts");
            }

            final List<? extends VariableElement> parameters = e.getParameters();
            if (parameters.size() != 1) {
                throw new RuntimeException("Can only handle a single body parameter");
            }

            final VariableElement requestBodyParameter = e.getParameters().get(0);
            referenced.add(requestBodyParameter.asType());

            String body = TSUtils.convertToTypescriptType(requestBodyParameter.asType(), mappings, processingEnv);
            sb.append("     public " + e.getSimpleName() + "( body : " + body + ") : " + TSUtils.convertToTypescriptType(e.getReturnType(), mappings, processingEnv) + " {\n");
            sb.append("         return this.http.post( '" + rm.value()[0] + "', body )\n");
            sb.append("                         .map( r => r.json());\n");
            sb.append("     }\n");


        }
        sb.append(" }\n");
        sb.append("\n");

        String imports = TSUtils.convertToImportStatements(referenced, mappings, processingEnv);
        String x = sb.toString().replace("*IMPORTS*", imports);

        return x;
    }

    private Set<TypeMirror> referenced = Sets.newHashSet();

    private void addReferences(ExecutableElement e) {
        referenced.addAll(TSUtils.getReferences(e));
    }

}
