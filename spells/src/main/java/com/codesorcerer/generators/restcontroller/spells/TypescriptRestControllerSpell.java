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
        FileUtils.write(new File(dir, ic.typeElement.getSimpleName() + ".ts"), result.output, Charset.defaultCharset());
    }

    @Override
    public void build(Result<AbstractSpell<TypescriptController, RestControllerInfo, String>, RestControllerInfo, String> result) throws Exception {
        RestControllerInfo ic = result.input;

        Set<TypescriptMapping> mappings = TSUtils.getAllMappings(ic.typeElement);
        String serviceName = ic.typeElement.getSimpleName().toString();

        final StringBuilder sb = new StringBuilder();
        sb.append("import {Injectable} from 'injection-js';\n");
        sb.append("import * as qwest from 'qwest';\n");
        sb.append("import {StompClient} from '@c1/stomp-client';\n");
        sb.append("import {Observable, BehaviorSubject} from 'rxjs';\n");
        sb.append("import {plainToClass} from 'class-transformer';\n");


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
                        String tsType = TSUtils.convertToTypescriptType(v.asType(), mappings, processingEnvironment);
                        return dv.value() + " : " + tsType;
                    })
                    .collect(Collectors.joining(","));

            sb.append("\n");
            String fullReturnType = TSUtils.convertToTypescriptType(e.getReturnType(), mappings, processingEnvironment);
            String innerReturnType = getInnerType(e.getReturnType(), mappings);

            sb.append("public " + e.getSimpleName() + "(" + tsParameterList + "): " + fullReturnType + " {\n");
            sb.append("    return this.stompClient.topic('" + topic + "')\n");
            sb.append("       .map(x => plainToClass(" + innerReturnType + ", x));\n");
            sb.append("}\n");
        }
        sb.append("\n");


        sb.append("//-----------------Rest Methods\n");
        for (ExecutableElement e : ic.getAllMethodsRest()) {
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

            String body = TSUtils.convertToTypescriptType(requestBodyParameter.asType(), mappings, processingEnvironment);
            String url = rm.value()[0];

            String fullReturnType = TSUtils.convertToTypescriptType(e.getReturnType(), mappings, processingEnvironment);
            String innerReturnType = getInnerType(e.getReturnType(), mappings);

            sb.append("public " + e.getSimpleName() + "( body : " + body + ") : " + fullReturnType + " {\n");
            sb.append("   let o = new BehaviorSubject<" + innerReturnType + ">();\n");
            sb.append("   qwest.post( '" + url + "', body )\n");
            sb.append("       .then((xhr, response) => {\n");
            sb.append("            let x = plainToClass(" + innerReturnType + ", response);\n");
            sb.append("            o.next(x);\n");
            sb.append("        })\n");
            sb.append("       .catch((e, xhr, response)  => {\n");
            sb.append("            o.error(e);\n");
            sb.append("        });\n");
            sb.append("   return o.asObservable();\n");
            sb.append("}\n");
        }

        sb.append("}\n");

        String imports = TSUtils.convertToImportStatements(referenced, mappings, processingEnvironment);
        String x = sb.toString().replace("*IMPORTS*", imports);

        for(TypeMirror tm : referenced) {
            Collector.COLLECTOR.putAll("mappings", TSUtils.getAllMappings(MoreTypes.asTypeElement(tm)));
        }

        Collector.COLLECTOR.put("packages", ic.getCurrentTypePackage());

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


    private Set<TypeMirror> referenced = Sets.newHashSet();

    private void addReferences(ExecutableElement e) {
        referenced.addAll(TSUtils.getReferences(e));
    }

}
