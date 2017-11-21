package com.codesorcerer.generators.def.spells;

import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.codesorcerer.generators.def.BeanDefInfo;
import com.codesorcerer.generators.def.BeanDefInfo.BeanDefFieldInfo;
import com.codesorcerer.generators.def.BeanDefInputBuilder;
import com.codesorcerer.targets.BBBTypescript;
import com.codesorcerer.targets.TypescriptMapping;
import com.codesorcerer.typescript.TSUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class TypescriptSpell extends AbstractSpell<BBBTypescript, BeanDefInfo, List<TypescriptSpell.Out>> {

    public static class Out {
        BeanDefInfo ic;
        String ts;
  //      Set<TypeMirror> mappings;
    }

    @Override
    public int getRunOrder() {
        return 1000;
    }


    @Override
    public void processingOver(Collection<Result> results) throws Exception {
    }


    @Override
    public void modify(Result<AbstractSpell<BBBTypescript, BeanDefInfo, List<Out>>, BeanDefInfo, List<Out>> result, Collection<Result> results) throws Exception {

    }

    @Override
    public void write(Result<AbstractSpell<BBBTypescript, BeanDefInfo, List<Out>>, BeanDefInfo, List<Out>> result) throws Exception {
        BeanDefInfo ic = result.input;
        File dir = TSUtils.getDirToWriteInto(ic.pkg);
        for (Out o : result.output) {
//            System.out.println("res.out " + result.output + " " + dir.getAbsolutePath());
            FileUtils.write(new File(dir, ic.immutableClassName + ".ts"), o.ts, Charset.defaultCharset());
        }
    }


    @Override
    public void build(Result<AbstractSpell<BBBTypescript, BeanDefInfo, List<Out>>, BeanDefInfo, List<Out>> result) throws Exception {

        Set<TypeMirror> referenced = Sets.newHashSet();

        BeanDefInfo ic = result.input;
        Set<TypescriptMapping> mappings = TSUtils.getAllMappings(ic.typeElement);

        StringBuilder sb = new StringBuilder();
        sb.append("import {Mappable, deserialize} from '@c1/leanusecase-client';\n");
        sb.append("*IMPORTS*");

        AnnotationMirror bbbTypescriptAnn = getAnnotationMirror(ic.typeElement, BBBTypescript.class);
        Map<? extends ExecutableElement, ? extends AnnotationValue> vals = bbbTypescriptAnn.getElementValues();
        Boolean interfaceOnlyVal = vals.entrySet()
                .stream()
                .filter(e -> e.getKey().getSimpleName().toString().equals("interfaceOnly"))
                .map(e -> (Boolean)e.getValue().getValue())
                .findFirst()
                .orElse(Boolean.FALSE);

        if(!interfaceOnlyVal) {
            buildBuilder(ic, sb, mappings);
            buildClass(ic, sb, mappings);
        }

        buildInterface(ic, sb, mappings, interfaceOnlyVal);

        //Add references to super class/intefaces
        if (ic.superClass != null) {
            addReference(ic.superClass.typeElement, ic.typeElement.asType(), referenced);
        }
        if (!ic.superInterfaces.isEmpty()) {
            for (BeanDefInfo x : ic.superInterfaces) {
                addReference(x.typeElement, ic.typeElement.asType(), referenced);
            }
        }


        //Register
        ic.beanDefFieldInfos
                .stream()
                .filter(x -> !x.returnType.equals(ic.immutableClassName))  //Dont import youself!
                .forEach(i -> addReferences(i.getter, result.te.asType(), referenced));
        String imports = TSUtils.convertToImportStatements(ic.pkg, referenced, mappings, processingEnvironment);
        String x = sb.toString().replace("*IMPORTS*", imports);

        Out out = new Out();
        out.ts = x;
        out.ic = ic;
        //out.mappings = referenced;

        //TODO:
        //referenced.clear();

        //Collector.COLLECTOR.putAll("mappings", TSUtils.getAllMappings(ic.typeElement));
//        Collector.COLLECTOR.put("packages", ic.pkg);

        result.output = ImmutableList.of(out);
    }

    private void buildInterface(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, Boolean interfaceOnlyVal)
    {

        if (interfaceOnlyVal) {
            sb.append("export interface " + ic.typeImmutable.simpleName() + " ");
        }
        else {
            sb.append("export interface " + ic.typeDef.simpleName() + " ");
        }

        if (!ic.superInterfaces.isEmpty()) {
            String intrfaces = ic.superInterfaces
                    .stream()
                    .map(b -> b.typeDef.simpleName())
                    .collect(Collectors.joining(", "));

            sb.append(" extends " + intrfaces);
        }

        sb.append(" {\n");

        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            sb.append(i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + ";\n");
        }

        sb.append("}");
    }

    private void buildClass(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {

        sb.append("export class " + ic.immutableClassName + "  implements " + ic.immutableClassName + "Def");


        sb.append(" {\n");
        buildFields2(ic, sb, mappings);
        buildStaticStarter(ic, sb, mappings);
        buildPrivateConstructor2(ic, sb, mappings);
        buildPrivateConstructorJson(ic, sb, mappings);
        buildWith(ic, sb, mappings);
        sb.append("}");
    }


    private void buildBuilder(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        sb.append("export class " + ic.immutableClassName + "Builder ");
        buildImplements(ic, sb);
        sb.append(" {\n");

        buildFields(ic, sb, mappings);
        buildPrivateConstructor(sb);
        buildSetters(ic, sb, mappings);
        buildBuild(ic, sb);

        sb.append("}");

        buildRequiresInterfaces(ic, sb, mappings);
        buildNullableInterface(ic, sb, mappings);
    }

    private void buildImplements(BeanDefInfo ic, StringBuilder sb) {
        sb.append("implements ");
        for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
            sb.append(ic.immutableClassName + "Requires" + i.nameUpper + ", ");
        }
        sb.append(ic.immutableClassName + "Nullable");
    }

    private void buildBuild(BeanDefInfo ic, StringBuilder sb) {
        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> "this._" + i.nameMangled)
                .collect(Collectors.joining(", "));

        sb.append("build() : " + ic.immutableClassName + " {\n");
        sb.append(" return new " + ic.immutableClassName + "(" + allParams + ");\n");
        sb.append("}");
        sb.append("\n");
    }

//    private void buildGetters(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
//        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
//            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
//            sb.append("public get " + i.nameMangled + "() : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + " { return this." + i.nameMangled + "; }\n");
//        }
//    }


    private void buildSetters(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        //NonNull stuff
        if (ic.nonNullBeanDefFieldInfos.size() > 0) {
            for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size() - 1; x++) {
                BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
                BeanDefFieldInfo ii = ic.nonNullBeanDefFieldInfos.get(x + 1);
                String returnType = ic.immutableClassName + "Requires" + ii.nameUpper;
                setter(sb, i, returnType, mappings);
                sb.append("\n");
            }

            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
            String returnType = ic.immutableClassName + "Nullable";
            setter(sb, i, returnType, mappings);
            sb.append("\n");
        }

        //Nullable
        for (int x = 0; x < ic.nullableBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nullableBeanDefFieldInfos.get(x);
            String returnType = ic.immutableClassName + "Nullable";
            setter(sb, i, returnType, mappings);
            sb.append("\n");
        }
    }

    private void buildWith(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {

        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);

            //compute the list of parameters, with only one not having 'this.' before
            String allParams = ic.beanDefFieldInfos.stream()
                    .map(p -> {
                        if (p.name.equals(i.name)) {
                            return p.nameMangled;
                        }
                        return "this." + p.nameMangled;
                    })
                    .collect(Collectors.joining(", "));

            String returnType = ic.immutableClassName + "Nullable";
            sb.append("public with" + i.nameUpper + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + ") : " + ic.immutableClassName + " {\n");
            sb.append("  return new " + ic.immutableClassName + "(" + allParams + ");\n");
            sb.append("}\n");
            sb.append("\n");
        }
    }

    private void setter(StringBuilder sb, BeanDefFieldInfo i, String returnType, Set<TypescriptMapping> mappings) {
        sb.append("public " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + ") : " + returnType + " {\n");
        sb.append("  this._" + i.nameMangled + " = " + i.nameMangled + ";\n");
        sb.append("  return this;\n");
        sb.append("}\n");
    }

    private void buildStaticStarter(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        String retType = "";
        if (!ic.nonNullBeanDefFieldInfos.isEmpty()) {
            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(0);
            retType = ic.immutableClassName + "Requires" + i.nameUpper;
        } else {
            retType = ic.immutableClassName + "Nullable";
        }
        sb.append("static build" + ic.immutableClassName + "() : " + retType + " {\n");
        sb.append("  return new " + ic.immutableClassName + "Builder();\n");
        sb.append("}\n");

        if (ic.beanDefFieldInfos.size() <= 3) {
            String allParams1 = ic.beanDefFieldInfos.stream()
                    .map(i -> i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment))
                    .collect(Collectors.joining(", "));

            String allParams2 = ic.beanDefFieldInfos.stream()
                    .map(i -> i.nameMangled)
                    .collect(Collectors.joining(", "));

            sb.append("static new" + ic.immutableClassName + "(" + allParams1 + ") : " + ic.immutableClassName + " {\n");
            sb.append("  return new " + ic.immutableClassName + "(" + allParams2 + ");\n");
            sb.append("}\n");
        }

        sb.append("\n");
    }

    private void buildNullableInterface(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        sb.append("export interface " + ic.immutableClassName + "Nullable {\n");
        for (int x = 0; x < ic.nullableBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nullableBeanDefFieldInfos.get(x);
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + ") : " + ic.immutableClassName + "Nullable;\n");
        }
        sb.append("  build() : " + ic.immutableClassName + ";\n");

        sb.append("}\n");
        sb.append("\n");
    }

    private void buildRequiresInterfaces(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        if (ic.nonNullBeanDefFieldInfos.size() > 0) {
            for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size() - 1; x++) {
                BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
                BeanDefFieldInfo ii = ic.nonNullBeanDefFieldInfos.get(x + 1);
                sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
                sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + ") : " + ic.immutableClassName + "Requires" + ii.nameUpper + ";\n");
                sb.append("}\n");
            }
            sb.append("\n");

            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
            sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + ") : " + ic.immutableClassName + "Nullable;\n");
            sb.append("}\n");
        }
    }

    private void buildPrivateConstructor(StringBuilder sb) {
        sb.append("public constructor() {}\n");
        sb.append("\n");
    }

    private void buildPrivateConstructor2(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> i.nameMangled + "? : " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment))
                .collect(Collectors.joining(", "));


        sb.append("public constructor( " + allParams + ") {\n");
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  this." + i.nameMangled + " = " + i.nameMangled + ";\n");
        }
        sb.append("}");
        sb.append("\n");
        sb.append("\n");
    }

    private void buildPrivateConstructorJson(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> {
                    String typ = TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment);
                    return "deserialize(obj." + i.nameMangled + ")";
                })
                .collect(Collectors.joining(", "));

        sb.append("@Mappable('" + ic.pkg + "." + ic.immutableClassName + "')\n");
        sb.append("public static fromJson( obj:any ) : " + ic.immutableClassName + " {\n");
        sb.append("  return new " + ic.immutableClassName + "(" + allParams + ");\n");
        sb.append("}\n");
        sb.append("\n");
        sb.append("\n");
    }


    private void buildFields(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  _" + i.nameMangled + ": " + TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment) + ";\n");
        }
        sb.append("\n");
    }

    private void buildFields2(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            String typ = TSUtils.convertToTypescriptType(i.returnTypeMirror, mappings, processingEnvironment);

            sb.append("  public readonly " + i.nameMangled + ": " + typ + ";\n");
        }
        sb.append("\n");

        sb.append("private readonly clazz : string = '" + ic.pkg + "." + ic.immutableClassName + "';\n");
    }


    private void addReferences(ExecutableElement e, TypeMirror enclosing, Set<TypeMirror> referenced) {
        referenced.addAll(TSUtils.getReferences(e, enclosing, typeUtils));
    }

    private void addReference(TypeElement e, TypeMirror enclosing, Set<TypeMirror> referenced) {
        referenced.addAll(TSUtils.getReference(e, enclosing, typeUtils));
    }

}
