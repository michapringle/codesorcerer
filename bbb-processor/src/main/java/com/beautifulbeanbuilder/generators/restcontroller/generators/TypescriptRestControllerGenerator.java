package com.beautifulbeanbuilder.generators.restcontroller.generators;

import com.beautifulbeanbuilder.generators.beandef.generators.TypescriptGenerator;
import com.beautifulbeanbuilder.generators.restcontroller.RestControllerInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.google.common.collect.Sets;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacFiler;
import org.apache.commons.io.FileUtils;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class TypescriptRestControllerGenerator extends AbstractGenerator<RestController, RestControllerInfo, String> {

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

        sb.append("     constructor( private stompClient: StompClient, private http: Http) {\n");
        sb.append("          stompClient.connect();\n");
        sb.append("     }\n");

        sb.append("\n");

        for (ExecutableElement e : ic.getAllMethodsStomp()) {
            SubscribeMapping rm = e.getAnnotation(SubscribeMapping.class);
            sb.append("     public readonly " + e.getSimpleName() + " : " + TypescriptGenerator.convertTypes(e.getReturnType()) + " = this.stompClient.topic( '" + rm.value()[0] + "' );\n");
        }


        for (ExecutableElement e : ic.getAllMethodsRest()) {
            sb.append("\n");

            RequestMapping rm = e.getAnnotation(RequestMapping.class);

            boolean post = Arrays.stream(rm.method()).anyMatch(x -> x == RequestMethod.POST);
            if (post) {
                List<? extends VariableElement> mmm = e.getParameters();
                VariableElement requestBodyParameter = e.getParameters().stream().filter(p -> p.getAnnotation(RequestBody.class) != null).findFirst().get();
                referenced.add(requestBodyParameter.asType());
                String body = TypescriptGenerator.convertTypes(requestBodyParameter.asType());
                sb.append("     public " + e.getSimpleName() + "( body : " + body + ") : " + TypescriptGenerator.convertTypes(e.getReturnType()) + " {\n");
                sb.append("         return this.http.post( '" + rm.value()[0] + "', body )\n");
                sb.append("                         .map( r => r.json());\n");
                sb.append("     }\n");
            }

            boolean get = Arrays.stream(rm.method()).anyMatch(x -> x == RequestMethod.GET);
            if (get) {
                sb.append("     public " + e.getSimpleName() + "() : " + TypescriptGenerator.convertTypes(e.getReturnType()) + " {\n");
                sb.append("         return this.http.get( '" + rm.value()[0] + "')\n");
                sb.append("                         .map( r => r.json());\n");
                sb.append("     }\n");
            }

            referenced.add(e.getReturnType());

        }
        sb.append(" }\n");
        sb.append("\n");
        //   sb.append("}");

        String imports = generateImportStatement(processingEnv);

        String x = sb.toString().replace("*IMPORTS*", imports);

     //   System.out.println(x);
        return x;
    }

    private String generateImportStatement(ProcessingEnvironment processingEnv) {
        return referenced.stream()
                    .map(tm -> {

                        if (tm.toString().contains("io.reactivex.Observable")) {
                            return "import {Observable} from 'rxjs';\n";
                        } else if (tm.toString().contains("io.reactivex.Single")) {
                            return "import {Observable} from 'rxjs';\n";
                        } else if (tm.toString().contains("java.util.List")) {
                            return "";
                        } else {
                            String name = "";
                            String simpleName = tm.toString();

                            if (tm instanceof ErrorType) {
                                simpleName = tm.toString();

                                JavacFiler f = (JavacFiler) processingEnv.getFiler();
                                name = f.getGeneratedSourceNames().stream()
                                        .filter(n -> n.endsWith("." + tm.toString()))
                                        .findFirst()
                                        .get();

                            }
                            else if (tm instanceof Type.ClassType) {
                                Type.ClassType ct = (Type.ClassType) tm;
                                name = ct.asElement().getQualifiedName().toString();
                                simpleName  = ct.asElement().getSimpleName().toString();
                            }
                            else {
                                throw new RuntimeException("Unknown type " + tm.getClass());
                            }
                            return "import {" + simpleName + "} from './" + name + "';\n";
                        }
                    })
                    .collect(Collectors.joining());
    }

}
