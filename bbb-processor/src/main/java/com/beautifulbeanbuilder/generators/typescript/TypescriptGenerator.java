package com.beautifulbeanbuilder.generators.typescript;

import com.beautifulbeanbuilder.BBBTypescript;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.beautifulbeanbuilder.processor.info.Info;
import com.beautifulbeanbuilder.processor.info.InfoClass;
import org.apache.commons.io.FileUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

public class TypescriptGenerator extends AbstractGenerator<BBBTypescript, InfoClass, String> {

    @Override
    public void processingOver(Collection<String> objects) {
        //write package.json
        String x = "{ 'name': 'my-awesome-package', 'version': '1.0.0' }";
    }

    @Override
    public void write(InfoClass ic, String objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        File dir = FileUtils.getTempDirectory();
        FileUtils.write(new File(dir, ic.immutableClassName + ".ts"), objectToWrite, Charset.defaultCharset());
    }

    @Override
    public String build(InfoClass ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap) throws IOException {

        StringBuilder sb = new StringBuilder();
        buildBuilder(ic, sb);
        buildInterface(ic, sb);

        return sb.toString();
    }

    private void buildInterface(InfoClass ic, StringBuilder sb) {
        sb.append("export class " + ic.immutableClassName + " {\n");


        buildFields2(ic, sb);
        buildStaticStarter(ic, sb);
        buildPrivateConstructor2(ic, sb);
        buildGetters(ic, sb);
        sb.append("}");
    }


    private void buildBuilder(InfoClass ic, StringBuilder sb) {
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

    private void buildImplements(InfoClass ic, StringBuilder sb) {
        sb.append("implements ");
        for (int x = 0; x < ic.nonNullInfos.size(); x++) {
            Info i = ic.nonNullInfos.get(x);
            sb.append(ic.immutableClassName + "Requires" + i.nameUpper + ", ");
        }
        sb.append("Nullable");
    }

    private void buildBuild(InfoClass ic, StringBuilder sb) {
        sb.append("build() : " + ic.immutableClassName + " {\n");
        sb.append(" return new " + ic.immutableClassName + "(this);\n");
        sb.append("}");
        sb.append("\n");
    }

    private void buildGetters(InfoClass ic, StringBuilder sb) {
        for (int x = 0; x < ic.infos.size(); x++) {
            Info i = ic.infos.get(x);
            sb.append("get" + i.nameUpper + "() { return this." + i.nameMangled + "; }\n");
        }
    }


    private void buildSetters(InfoClass ic, StringBuilder sb) {
        for (int x = 0; x < ic.infos.size(); x++) {
            Info i = ic.infos.get(x);
            sb.append(i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") {\n");
            sb.append("  this." + i.nameMangled + " = " + i.nameMangled + ";\n");
            sb.append("}\n");
            sb.append("\n");
        }
    }

    private void buildStaticStarter(InfoClass ic, StringBuilder sb) {
        String retType = "";
        if (!ic.nonNullInfos.isEmpty()) {
            Info i = ic.nonNullInfos.get(0);
            retType = ic.immutableClassName + "Requires" + i.nameUpper;
        } else {
            retType = "Nullable";
        }
        sb.append("static build" + ic.immutableClassName + "() : " + retType + " {\n");
        sb.append("  return new " + ic.immutableClassName + "Builder();\n");
        sb.append("}\n");
        sb.append("\n");
    }

    private void buildNullableInterface(InfoClass ic, StringBuilder sb) {
        sb.append("export interface " + ic.immutableClassName + "Nullable {\n");
        for (int x = 0; x < ic.nullableInfos.size() - 1; x++) {
            Info i = ic.nullableInfos.get(x);
            sb.append("  " + i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") : " + ic.immutableClassName + "Nullable;\n");
        }
        sb.append("build() : " + ic.immutableClassName + ";\n");

        sb.append("}\n");
        sb.append("\n");
    }

    private void buildRequiresInterfaces(InfoClass ic, StringBuilder sb) {
        for (int x = 0; x < ic.nonNullInfos.size() - 1; x++) {
            Info i = ic.nonNullInfos.get(x);
            Info ii = ic.nonNullInfos.get(x + 1);
            sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
            sb.append("  " + i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") : " + ic.immutableClassName + "Requires" + ii.nameUpper + ";\n");
            sb.append("}\n");
        }
        sb.append("\n");

        {
            Info i = ic.nonNullInfos.get(ic.nonNullInfos.size() - 1);
            sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
            sb.append("  " + i.nameMangled + "(" + convertTypes(i.returnType) + ": " + i.nameMangled + ") : " + ic.immutableClassName + "Nullable;\n");
            sb.append("}\n");
        }
    }

    private void buildPrivateConstructor(StringBuilder sb) {
        sb.append("private constructor() {}\n");
        sb.append("\n");
    }

    private void buildPrivateConstructor2(InfoClass ic, StringBuilder sb) {
        sb.append("public constructor( builder : " + ic.immutableClassName + "Builder) {\n");
        for (Info i : ic.infos) {
            sb.append("  this." + i.nameMangled + " = builder." + i.nameMangled + ";\n");
        }
        sb.append("}");
        sb.append("\n");
    }

    private void buildFields(InfoClass ic, StringBuilder sb) {
        for (Info i : ic.infos) {
            sb.append(" private " + i.nameMangled + ": " + convertTypes(i.returnType) + ";\n");
        }
        sb.append("\n");
    }

    private void buildFields2(InfoClass ic, StringBuilder sb) {
        for (Info i : ic.infos) {
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
        throw new RuntimeException("No conversion from " + javaType + " to typescript!");
    }
}
