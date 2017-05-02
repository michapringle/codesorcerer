package com.beautifulbeanbuilder.generators.beandef.generators;

import com.beautifulbeanbuilder.BBBTypescript;
import com.beautifulbeanbuilder.generators.beandef.BeanDefFieldInfo;
import com.beautifulbeanbuilder.generators.beandef.BeanDefInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import org.apache.commons.io.FileUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

public class TypescriptGenerator extends AbstractGenerator<BBBTypescript, BeanDefInfo, String> {

    public static final File DIR = new File("/home/dphillips/git/lean-modules/ng-stomp-poc/src/app");

    @Override
    public void processingOver(Collection<String> objects) throws IOException {
        //write package.json
        String x = "{ 'name': 'my-awesome-package', 'version': '1.0.0' }";

        FileUtils.write(new File(DIR, "package.json"), x, Charset.defaultCharset());
    }

    @Override
    public void write(BeanDefInfo ic, String objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        FileUtils.write(new File(DIR, ic.immutableClassName + ".ts"), objectToWrite, Charset.defaultCharset());
    }

    @Override
    public String build(BeanDefInfo ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap) throws IOException {

        StringBuilder sb = new StringBuilder();
        buildBuilder(ic, sb);
        buildInterface(ic, sb);

        return sb.toString();
    }

    private void buildInterface(BeanDefInfo ic, StringBuilder sb) {
        sb.append("export class " + ic.immutableClassName + " {\n");


        buildFields2(ic, sb);
        buildStaticStarter(ic, sb);
        buildPrivateConstructor2(ic, sb);
        buildGetters(ic, sb);
        sb.append("}");
    }


    private void buildBuilder(BeanDefInfo ic, StringBuilder sb) {
        sb.append("export class " + ic.immutableClassName + "Builder ");
        buildImplements(ic, sb);
        sb.append(" {\n");

        buildFields(ic, sb);
        buildPrivateConstructor(sb);
        buildSetters(ic, sb);
        buildBuild(ic, sb);

        sb.append("}");

        buildRequiresInterfaces(ic, sb);
        buildNullableInterface(ic, sb);
    }

    private void buildImplements(BeanDefInfo ic, StringBuilder sb) {
        sb.append("implements ");
        for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
            sb.append(ic.immutableClassName + "Requires" + i.nameUpper + ", ");
        }
        sb.append("Nullable");
    }

    private void buildBuild(BeanDefInfo ic, StringBuilder sb) {
        sb.append("build() : " + ic.immutableClassName + " {\n");
        sb.append(" return new " + ic.immutableClassName + "(this);\n");
        sb.append("}");
        sb.append("\n");
    }

    private void buildGetters(BeanDefInfo ic, StringBuilder sb) {
        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            sb.append("get" + i.nameUpper + "() { return this." + i.nameMangled + "; }\n");
        }
    }


    private void buildSetters(BeanDefInfo ic, StringBuilder sb) {
        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            sb.append(i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") {\n");
            sb.append("  this." + i.nameMangled + " = " + i.nameMangled + ";\n");
            sb.append("}\n");
            sb.append("\n");
        }
    }

    private void buildStaticStarter(BeanDefInfo ic, StringBuilder sb) {
        String retType = "";
        if (!ic.nonNullBeanDefFieldInfos.isEmpty()) {
            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(0);
            retType = ic.immutableClassName + "Requires" + i.nameUpper;
        } else {
            retType = "Nullable";
        }
        sb.append("static build" + ic.immutableClassName + "() : " + retType + " {\n");
        sb.append("  return new " + ic.immutableClassName + "Builder();\n");
        sb.append("}\n");
        sb.append("\n");
    }

    private void buildNullableInterface(BeanDefInfo ic, StringBuilder sb) {
        sb.append("export interface " + ic.immutableClassName + "Nullable {\n");
        for (int x = 0; x < ic.nullableBeanDefFieldInfos.size() - 1; x++) {
            BeanDefFieldInfo i = ic.nullableBeanDefFieldInfos.get(x);
            sb.append("  " + i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") : " + ic.immutableClassName + "Nullable;\n");
        }
        sb.append("build() : " + ic.immutableClassName + ";\n");

        sb.append("}\n");
        sb.append("\n");
    }

    private void buildRequiresInterfaces(BeanDefInfo ic, StringBuilder sb) {
        for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size() - 1; x++) {
            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
            BeanDefFieldInfo ii = ic.nonNullBeanDefFieldInfos.get(x + 1);
            sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
            sb.append("  " + i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") : " + ic.immutableClassName + "Requires" + ii.nameUpper + ";\n");
            sb.append("}\n");
        }
        sb.append("\n");

        if (ic.nonNullBeanDefFieldInfos.size() > 0) {
            {
                BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
                sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
                sb.append("  " + i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") : " + ic.immutableClassName + "Nullable;\n");
                sb.append("}\n");
            }
        }
    }

    private void buildPrivateConstructor(StringBuilder sb) {
        sb.append("private constructor() {}\n");
        sb.append("\n");
    }

    private void buildPrivateConstructor2(BeanDefInfo ic, StringBuilder sb) {
        sb.append("public constructor( builder : " + ic.immutableClassName + "Builder) {\n");
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  this." + i.nameMangled + " = builder." + i.nameMangled + ";\n");
        }
        sb.append("}");
        sb.append("\n");
    }

    private void buildFields(BeanDefInfo ic, StringBuilder sb) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append(" private " + i.nameMangled + ": " + convertTypes(i.returnType) + ";\n");
        }
        sb.append("\n");
    }

    private void buildFields2(BeanDefInfo ic, StringBuilder sb) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append(" private readonly " + i.nameMangled + ": " + convertTypes(i.returnType) + ";\n");
        }
        sb.append("\n");
    }


    public static String convertTypes(TypeMirror tm) {
        return convertTypes(tm.toString());
    }

    public static String convertTypes(String javaType) {
        if (javaType.equals(String.class.getName())) {
            return "string";
        }
        if (javaType.equals(int.class.getName())) {
            return "number";
        }
        return javaType;
    }
}
