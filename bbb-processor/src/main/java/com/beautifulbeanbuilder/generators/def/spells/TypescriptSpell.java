package com.beautifulbeanbuilder.generators.def.spells;

import com.beautifulbeanbuilder.BBBTypescript;
import com.beautifulbeanbuilder.Collector;
import com.beautifulbeanbuilder.typescript.PackageJson;
import com.beautifulbeanbuilder.TypescriptMapping;
import com.beautifulbeanbuilder.generators.def.BeanDefInfo;
import com.beautifulbeanbuilder.generators.def.BeanDefInfo.BeanDefFieldInfo;
import com.beautifulbeanbuilder.abstracts.AbstractSpell;
import com.beautifulbeanbuilder.processor.CodeSorcererProcessor;
import com.beautifulbeanbuilder.typescript.TSUtils;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class TypescriptSpell extends AbstractSpell<BBBTypescript, BeanDefInfo, TypescriptSpell.Out> {

    public static class Out {
        BeanDefInfo ic;
        String ts;
        Set<TypeMirror> mappings;
    }


    public static final File DIR = new File("typescript");

    @Override
    public int getRunOrder() {
        return 1000;
    }


    @Override
    public void processingOver(Collection<CodeSorcererProcessor.Result> results) throws Exception {
        //write package.json

        PackageJson packageJson = new PackageJson();
        packageJson.version = "0.0.0.0.0-SNAPSHOT";
        packageJson.version = "1.0.0";


        //Calc lowest common package name
        String pkg = null;
        final Set<String> packages = Collector.get("packages");
        for (String p : packages) {
            if (pkg == null) {
                pkg = p;
            } else {
                pkg = StringUtils.removeEnd(StringUtils.getCommonPrefix(pkg, p), ".");
            }
        }
        packageJson.name = pkg;


        //Add devDependencies
        final Set<TypescriptMapping> mappings = Collector.get("mappings");
        for (TypescriptMapping tm : mappings) {
            if (!tm.typescriptPackageName().isEmpty()) {
                packageJson.peerDependencies.put(tm.typescriptPackageName(), tm.typescriptPackageVersion());
            }
        }
        packageJson.peerDependencies.put("class-transformer", "^0.1.6");
        packageJson.peerDependencies.put("@c1/stomp-client", "^0.0.1");
        packageJson.peerDependencies.put("qwest", "^4.4.6");

        File dir = new File(DIR, pkg);
        FileUtils.forceMkdirParent(dir);

        FileUtils.write(new File(dir, "package.json"), packageJson.toJson(), Charset.defaultCharset());
    }



    @Override
    public void modify(CodeSorcererProcessor.Result<AbstractSpell<BBBTypescript, BeanDefInfo, Out>, BeanDefInfo, Out> result, Collection<CodeSorcererProcessor.Result> results) throws Exception {

    }

    @Override
    public void write(CodeSorcererProcessor.Result<AbstractSpell<BBBTypescript, BeanDefInfo, Out>, BeanDefInfo, Out> result) throws Exception {
        BeanDefInfo ic = result.input;

        File dir = new File(DIR, ic.pkg);
        FileUtils.forceMkdirParent(dir);

        FileUtils.write(new File(dir, ic.immutableClassName + ".ts"), result.output.ts, Charset.defaultCharset());
    }

    @Override
    public void build(CodeSorcererProcessor.Result<AbstractSpell<BBBTypescript, BeanDefInfo, Out>, BeanDefInfo, Out> result) throws Exception {

        BeanDefInfo ic = result.input;
        Set<TypescriptMapping> mappings = TSUtils.getAllMappings(ic.typeElement);

        StringBuilder sb = new StringBuilder();
        sb.append("import {Type, Expose} from 'class-transformer';\n");
        sb.append("*IMPORTS*");

        buildBuilder(ic, sb, mappings);
        buildClass(ic, sb, mappings);

        //Register
        ic.beanDefFieldInfos.forEach(i -> addReferences(i.getter));
        String imports = TSUtils.convertToImportStatements(referenced, mappings, processingEnvironment);
        String x = sb.toString().replace("*IMPORTS*", imports);

        Out out = new Out();
        out.ts = x;
        out.ic = ic;
        out.mappings = referenced;

        Collector.COLLECTOR.putAll("mappings", TSUtils.getAllMappings(ic.typeElement));
        Collector.COLLECTOR.put("packages", ic.pkg);

        result.output = out;
    }

    private void buildClass(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        sb.append("export class " + ic.immutableClassName + " {\n");


        buildFields2(ic, sb, mappings);
        buildStaticStarter(ic, sb, mappings);
        buildPrivateConstructor2(ic, sb, mappings);
        buildGetters(ic, sb, mappings);
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

    private void buildGetters(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            sb.append("public get " + i.nameMangled + "() : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment) + " { return this._" + i.nameMangled + "; }\n");
        }
    }


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
                        return "this._" + p.nameMangled;
                    })
                    .collect(Collectors.joining(", "));

            String returnType = ic.immutableClassName + "Nullable";
            sb.append("public with" + i.nameUpper + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment) + ") : " + ic.immutableClassName + " {\n");
            sb.append("  return new " + ic.immutableClassName + "(" + allParams + ");\n");
            sb.append("}\n");
            sb.append("\n");
        }
    }

    private void setter(StringBuilder sb, BeanDefFieldInfo i, String returnType, Set<TypescriptMapping> mappings) {
        sb.append("public " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment) + ") : " + returnType + " {\n");
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
                    .map(i -> i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment))
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
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment) + ") : " + ic.immutableClassName + "Nullable;\n");
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
                sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment) + ") : " + ic.immutableClassName + "Requires" + ii.nameUpper + ";\n");
                sb.append("}\n");
            }
            sb.append("\n");

            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
            sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment) + ") : " + ic.immutableClassName + "Nullable;\n");
            sb.append("}\n");
        }
    }

    private void buildPrivateConstructor(StringBuilder sb) {
        sb.append("private constructor() {}\n");
        sb.append("\n");
    }

    private void buildPrivateConstructor2(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment))
                .collect(Collectors.joining(", "));


        sb.append("public constructor( " + allParams + ") {\n");
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  this._" + i.nameMangled + " = " + i.nameMangled + ";\n");
        }
        sb.append("}");
        sb.append("\n");

        sb.append("public constructor() {}");
        sb.append("\n");
    }

    private void buildFields(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  _" + i.nameMangled + ": " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment) + ";\n");
        }
        sb.append("\n");
    }

    private void buildFields2(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            String typ = TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnvironment);

            String ann = "";
            if (!typ.equals("string") && !typ.equals("number") && !typ.equals("boolean")) {
                ann = "@Type(() => " + typ + ")";
            }

            sb.append(ann + "  @Expose({ name: '" + i.nameMangled + "' })" + " private _" + i.nameMangled + ": " + typ + ";\n");
        }
        sb.append("\n");
    }


    private Set<TypeMirror> referenced = Sets.newHashSet();

    private void addReferences(ExecutableElement e) {
        referenced.addAll(TSUtils.getReferences(e));
    }

}
