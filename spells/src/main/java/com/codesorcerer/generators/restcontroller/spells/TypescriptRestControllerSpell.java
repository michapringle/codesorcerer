package com.codesorcerer.generators.restcontroller.spells;

import com.codesorcerer.Collector;
import com.codesorcerer.targets.TypescriptController;
import com.codesorcerer.targets.TypescriptMapping;
import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.codesorcerer.generators.restcontroller.RestControllerInfo;
import com.codesorcerer.typescript.TSUtils;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.Sets;
import com.sun.tools.javac.code.Type;
import org.apache.commons.io.FileUtils;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TypescriptRestControllerSpell extends AbstractSpell<TypescriptController, RestControllerInfo, String> {


    @Override
    public int getRunOrder() {
        return 2000;
    }


    @Override
    public void processingOver(Collection<Result> results) throws Exception {
    }

    @Override
    public void modify(Result<AbstractSpell<TypescriptController, RestControllerInfo, String>, RestControllerInfo, String> result, Collection<Result> results) throws Exception {

    }

    @Override
    public void write(Result<AbstractSpell<TypescriptController, RestControllerInfo, String>, RestControllerInfo, String> result) throws Exception {
        RestControllerInfo ic = result.input;
        File dir = TSUtils.getDirToWriteInto(ic.getCurrentTypePackage());
        FileUtils.write(new File(dir, ic.typeElement.getSimpleName() + "Service.ts"), result.output, Charset.defaultCharset());
    }

    private void addReferences(ExecutableElement e, TypeMirror enclosing, Set<TypeMirror> referenced) {
        referenced.addAll(TSUtils.getReferences(e, enclosing, typeUtils));
    }

    @Override
    public void build(Result<AbstractSpell<TypescriptController, RestControllerInfo, String>, RestControllerInfo, String> result) throws Exception {
        RestControllerInfo ic = result.input;

        Set<TypeMirror> referenced = Sets.newHashSet();



        Set<TypescriptMapping> mappings = TSUtils.getAllMappings(ic.typeElement);
        String serviceName = ic.typeElement.getSimpleName().toString();

        final StringBuilder sb = new StringBuilder();
        sb.append("import {Injectable} from 'injection-js';\n");
        sb.append("import * as qwest from 'qwest';\n");
        sb.append("import {StompClient} from '@c1/stomp-client';\n");
        sb.append("import {Subject} from 'rxjs';\n");
        sb.append("import {plainToClass, serialize} from 'class-transformer';\n");


        //import {Account} from "test";
        sb.append("*IMPORTS*");

        sb.append("\n");
        sb.append("@Injectable()\n");
        sb.append("export class " + serviceName + "Service {\n");

        sb.append("//-----------------Constructor\n");
        sb.append("     constructor( private stompClient: StompClient ) {}\n");
        sb.append("\n");

        sb.append("//-----------------Stomp Methods\n");
        for (ExecutableElement e : ic.getAllMethodsStomp()) {
            addReferences(e, ic.typeElement.asType(), referenced);

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
                    .map(p -> {
                        String tsType = TSUtils.convertToTypescriptType(p.asType(), mappings, processingEnvironment);
                        DestinationVariable dv = p.getAnnotation(DestinationVariable.class);
                        return dv.value() + " : " + tsType;
                    })
                    .collect(Collectors.joining(","));

            sb.append("\n");
            String fullReturnType = TSUtils.convertToTypescriptType(e.getReturnType(), mappings, processingEnvironment);
            String innerReturnType = getInnerType(e.getReturnType(), mappings);

//            if(innerReturnType.startsWith("Array<")) {
//
//            }

            sb.append("public " + e.getSimpleName() + "(" + tsParameterList + "): " + fullReturnType + " {\n");
            sb.append("    return this.leanusecaseClient.topic('" + topic + "');\n");
            sb.append("}\n");
        }
        sb.append("\n");


        sb.append("//-----------------Rest Methods\n");
        for (ExecutableElement e : ic.getAllMethodsRest()) {
            addReferences(e, ic.typeElement.asType(), referenced);

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

            String body = TSUtils.convertToTypescriptType(requestBodyParameter.asType(), mappings, processingEnvironment);
            String url = rm.value()[0];

            String fullReturnType = TSUtils.convertToTypescriptType(e.getReturnType(), mappings, processingEnvironment);
            //String innerReturnType = getInnerType(e.getReturnType(), mappings);

            sb.append("public " + e.getSimpleName() + "( body : " + body + ") : " + fullReturnType + " {\n");
            sb.append("   return this.leanusecaseClient.post(body, '" + url + "');\n");
            sb.append("}\n");
        }

        sb.append("}\n");


        //for(TypeMirror tm : referenced) {
            //Collector.COLLECTOR.putAll("mappings", TSUtils.getAllMappings(MoreTypes.asTypeElement(tm)));
        //}
        //Collector.COLLECTOR.put("packages", ic.getCurrentTypePackage());

        //System.out.printf("Referenced: " + referenced);
        String imports = TSUtils.convertToImportStatements(ic.getCurrentTypePackage(), referenced, mappings, processingEnvironment);
        //System.out.printf("Imports: " + imports);
        String x = sb.toString().replace("*IMPORTS*", imports);
        result.output = x;
    }

    private String getInnerType(TypeMirror e, Set<TypescriptMapping> mappings) {
        if (e instanceof Type.ClassType) {
            Type.ClassType ct = (Type.ClassType) e;
            String name = ct.asElement().toString();
            if(name.equals(io.reactivex.Observable.class.getName()) ) {
                return getInnerType(ct.getTypeArguments().get(0), mappings);
            }
            if(name.equals(io.reactivex.Single.class.getName()) ) {
                return getInnerType(ct.getTypeArguments().get(0), mappings);
            }
            if(name.equals(List.class.getName()) ) {
                return getInnerType(ct.getTypeArguments().get(0), mappings);
            }
        }
        if (e instanceof Type.ArrayType) {
            Type.ArrayType ct = (Type.ArrayType) e;
            return getInnerType(ct.elemtype, mappings);
        }

        return TSUtils.convertToTypescriptType(e, mappings, processingEnvironment);
    }

    private String unboxOnce(TypeMirror e, Set<TypescriptMapping> mappings) {
        TypeMirror inner = e;
        if (e instanceof Type.ClassType) {
            Type.ClassType ct = (Type.ClassType) e;
            String name = ct.asElement().toString();

            if(name.equals(io.reactivex.Observable.class.getName()) ) {
                inner = ct.getTypeArguments().get(0);
            }
            if(name.equals(io.reactivex.Single.class.getName()) ) {
                inner = ct.getTypeArguments().get(0);
            }
            if(name.equals(List.class.getName()) ) {
                inner = ct.getTypeArguments().get(0);
            }
        }
        return TSUtils.convertToTypescriptType(inner, mappings, processingEnvironment);
    }



}
