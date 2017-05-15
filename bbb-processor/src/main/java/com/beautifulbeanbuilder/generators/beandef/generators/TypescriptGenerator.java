package com.beautifulbeanbuilder.generators.beandef.generators;

import com.beautifulbeanbuilder.BBBTypescript;
import com.beautifulbeanbuilder.TypescriptMapping;
import com.beautifulbeanbuilder.generators.beandef.BeanDefFieldInfo;
import com.beautifulbeanbuilder.generators.beandef.BeanDefInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.beautifulbeanbuilder.typescript.TSUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TypescriptGenerator extends AbstractGenerator<BBBTypescript, BeanDefInfo, TypescriptGenerator.Out> {

    public static class Out {
        BeanDefInfo ic;
        String ts;
        Set<TypeMirror> mappings;
    }

    @XmlRootElement
    public static class PackageJson {
        public String name;
        public String version;
        public Map<String, String> dependencies = Maps.newHashMap();
    }


    public static final File DIR = new File("typescript");

    @Override
    public void processingOver(Collection<Out> objects, ProcessingEnvironment processingEnv) throws Exception {
        //write package.json

        PackageJson packageJson = new PackageJson();
        packageJson.version = "0.0.0.0.0-SNAPSHOT";

        //Calc lowest common package name
        String pkg = null;
        for (Out o : objects) {

            if (pkg == null) {
                pkg = o.ic.pkg;
            } else {
                pkg = StringUtils.removeEnd(StringUtils.getCommonPrefix(pkg, o.ic.pkg), ".");
            }
        }
        packageJson.name = pkg;


        //Add dependencies
        for (Out o : objects) {
            Set<TypescriptMapping> mappings = TSUtils.getAllMappings(o.ic.typeElement);
            for (TypescriptMapping tm : mappings) {
                if (!tm.typescriptPackageName().isEmpty()) {
                    packageJson.dependencies.put(tm.typescriptPackageName(), tm.typescriptPackageVersion());
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String x = mapper.writeValueAsString(packageJson);

        FileUtils.forceMkdirParent(DIR);
        FileUtils.write(new File(DIR, "package.json"), x, Charset.defaultCharset());
    }

    @Override
    public void write(BeanDefInfo ic, Out objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        FileUtils.forceMkdirParent(DIR);
        FileUtils.write(new File(DIR, ic.pkg + "." + ic.immutableClassName + ".ts"), objectToWrite.ts, Charset.defaultCharset());
    }

    @Override
    public Out build(BeanDefInfo ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnv) throws IOException {

        Set<TypescriptMapping> mappings = TSUtils.getAllMappings(ic.typeElement);

        StringBuilder sb = new StringBuilder();
        sb.append("*IMPORTS*");

        buildBuilder(ic, sb, mappings, processingEnv);
        buildInterface(ic, sb, mappings, processingEnv);

        //Register
        ic.beanDefFieldInfos.forEach(i -> addReferences(i.getter));
        String imports = TSUtils.convertToImportStatements(referenced, mappings, processingEnv);
        String x = sb.toString().replace("*IMPORTS*", imports);

        Out out = new Out();
        out.ts = x;
        out.ic = ic;
        out.mappings = referenced;
        return out;
    }

    private void buildInterface(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        sb.append("export class " + ic.immutableClassName + " {\n");


        buildFields2(ic, sb, mappings, processingEnv);
        buildStaticStarter(ic, sb, mappings, processingEnv);
        buildPrivateConstructor2(ic, sb, mappings, processingEnv);
        buildGetters(ic, sb, mappings, processingEnv);
        buildWith(ic, sb, mappings, processingEnv);
        sb.append("}");
    }


    private void buildBuilder(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        sb.append("export class " + ic.immutableClassName + "Builder ");
        buildImplements(ic, sb);
        sb.append(" {\n");

        buildFields(ic, sb, mappings, processingEnv);
        buildPrivateConstructor(sb);
        buildSetters(ic, sb, mappings, processingEnv);
        buildBuild(ic, sb);

        sb.append("}");

        buildRequiresInterfaces(ic, sb, mappings, processingEnv);
        buildNullableInterface(ic, sb, mappings, processingEnv);
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

    private void buildGetters(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            sb.append("public get " + i.nameMangled + "() : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + " { return this._" + i.nameMangled + "; }\n");
        }
    }


    private void buildSetters(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        //NonNull stuff
        if (ic.nonNullBeanDefFieldInfos.size() > 0) {
            for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size() - 1; x++) {
                BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
                BeanDefFieldInfo ii = ic.nonNullBeanDefFieldInfos.get(x + 1);
                String returnType = ic.immutableClassName + "Requires" + ii.nameUpper;
                setter(sb, i, returnType, mappings, processingEnv);
                sb.append("\n");
            }

            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
            String returnType = ic.immutableClassName + "Nullable";
            setter(sb, i, returnType, mappings, processingEnv);
            sb.append("\n");
        }

        //Nullable
        for (int x = 0; x < ic.nullableBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nullableBeanDefFieldInfos.get(x);
            String returnType = ic.immutableClassName + "Nullable";
            setter(sb, i, returnType, mappings, processingEnv);
            sb.append("\n");
        }
    }

    private void buildWith(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> "this._" + i.nameMangled)
                .collect(Collectors.joining(", "));

        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            final String allParamsWithOneReplaced = allParams.replace("this._" + i.nameMangled, i.nameMangled);

            String returnType = ic.immutableClassName + "Nullable";
            sb.append("public with" + i.nameUpper + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + ") : " + ic.immutableClassName + " {\n");
            sb.append("  return new " + ic.immutableClassName + "(" + allParamsWithOneReplaced + ");\n");
            sb.append("}\n");
            sb.append("\n");
        }
    }

    private void setter(StringBuilder sb, BeanDefFieldInfo i, String returnType, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        sb.append("public " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + ") : " + returnType + " {\n");
        sb.append("  this._" + i.nameMangled + " = " + i.nameMangled + ";\n");
        sb.append("  return this;\n");
        sb.append("}\n");
    }

    private void buildStaticStarter(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
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
                    .map(i -> i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv))
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

    private void buildNullableInterface(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        sb.append("export interface " + ic.immutableClassName + "Nullable {\n");
        for (int x = 0; x < ic.nullableBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nullableBeanDefFieldInfos.get(x);
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + ") : " + ic.immutableClassName + "Nullable;\n");
        }
        sb.append("  build() : " + ic.immutableClassName + ";\n");

        sb.append("}\n");
        sb.append("\n");
    }

    private void buildRequiresInterfaces(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        if (ic.nonNullBeanDefFieldInfos.size() > 0) {
            for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size() - 1; x++) {
                BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
                BeanDefFieldInfo ii = ic.nonNullBeanDefFieldInfos.get(x + 1);
                sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
                sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + ") : " + ic.immutableClassName + "Requires" + ii.nameUpper + ";\n");
                sb.append("}\n");
            }
            sb.append("\n");

            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
            sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + ") : " + ic.immutableClassName + "Nullable;\n");
            sb.append("}\n");
        }
    }

    private void buildPrivateConstructor(StringBuilder sb) {
        sb.append("private constructor() {}\n");
        sb.append("\n");
    }

    private void buildPrivateConstructor2(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> i.nameMangled + " : " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv))
                .collect(Collectors.joining(", "));

        sb.append("private constructor( " + allParams + ") {\n");
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  this._" + i.nameMangled + " = " + i.nameMangled + ";\n");
        }
        sb.append("}");
        sb.append("\n");
    }

    private void buildFields(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  _" + i.nameMangled + ": " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + ";\n");
        }
        sb.append("\n");
    }

    private void buildFields2(BeanDefInfo ic, StringBuilder sb, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append(" private readonly _" + i.nameMangled + ": " + TSUtils.convertToTypescriptType(i.getter.getReturnType(), mappings, processingEnv) + ";\n");
        }
        sb.append("\n");
    }


    private Set<TypeMirror> referenced = Sets.newHashSet();

    private void addReferences(ExecutableElement e) {
        referenced.addAll(TSUtils.getReferences(e));
    }

}
