package com.beautifulbeanbuilder.generators.beandef.generators;

import com.beautifulbeanbuilder.BBBTypescript;
import com.beautifulbeanbuilder.generators.beandef.BeanDefFieldInfo;
import com.beautifulbeanbuilder.generators.beandef.BeanDefInfo;
import com.beautifulbeanbuilder.processor.AbstractGenerator;
import com.beautifulbeanbuilder.processor.AbstractJavaGenerator;
import com.sun.tools.javac.code.Type;
import org.apache.commons.io.FileUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypescriptGenerator extends AbstractGenerator<BBBTypescript, BeanDefInfo, String> {

    //public static final File DIR = new File("/home/dphillips/git/lean-modules/ng-stomp-poc/src/app");
    public static final File DIR = new File("typescript");

    @Override
    public void processingOver(Collection<String> objects) throws IOException {
        //write package.json
        String x = "{ 'name': 'my-awesome-package', 'version': '0.0.0.0.0-SNAPSHOT' }";

        FileUtils.forceMkdirParent(DIR);
        FileUtils.write(new File(DIR, "package.json"), x, Charset.defaultCharset());
    }

    @Override
    public void write(BeanDefInfo ic, String objectToWrite, ProcessingEnvironment processingEnv) throws IOException {
        FileUtils.forceMkdirParent(DIR);
        FileUtils.write(new File(DIR, ic.pkg + "." + ic.immutableClassName + ".ts"), objectToWrite, Charset.defaultCharset());
    }

    @Override
    public String build(BeanDefInfo ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnvironment) throws IOException {

        StringBuilder sb = new StringBuilder();
//        sb.append("namespace '" + ic.pkg + "' {");
        buildBuilder(ic, sb);
        buildInterface(ic, sb);
        //   sb.append("}");

        return sb.toString();
    }

    private void buildInterface(BeanDefInfo ic, StringBuilder sb) {
        sb.append("export class " + ic.immutableClassName + " {\n");


        buildFields2(ic, sb);
        buildStaticStarter(ic, sb);
        buildPrivateConstructor2(ic, sb);
        buildGetters(ic, sb);
        buildWith(ic, sb);
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

    private void buildGetters(BeanDefInfo ic, StringBuilder sb) {
        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            sb.append("public get " + i.nameMangled + "() : " + convertTypes(i.returnType) + " { return this._" + i.nameMangled + "; }\n");
        }
    }


    private void buildSetters(BeanDefInfo ic, StringBuilder sb) {
        //NonNull stuff
        if (ic.nonNullBeanDefFieldInfos.size() > 0) {
            for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size() - 1; x++) {
                BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
                BeanDefFieldInfo ii = ic.nonNullBeanDefFieldInfos.get(x + 1);
                String returnType = ic.immutableClassName + "Requires" + ii.nameUpper;
                setter(sb, i, returnType);
                sb.append("\n");
            }

            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
            String returnType = ic.immutableClassName + "Nullable";
            setter(sb, i, returnType);
            sb.append("\n");
        }

        //Nullable
        for (int x = 0; x < ic.nullableBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nullableBeanDefFieldInfos.get(x);
            String returnType = ic.immutableClassName + "Nullable";
            setter(sb, i, returnType);
            sb.append("\n");
        }
    }

    private void buildWith(BeanDefInfo ic, StringBuilder sb) {
        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> "this._" + i.nameMangled)
                .collect(Collectors.joining(", "));

        for (int x = 0; x < ic.beanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.beanDefFieldInfos.get(x);
            final String allParamsWithOneReplaced = allParams.replace("this._" + i.nameMangled, i.nameMangled);

            String returnType = ic.immutableClassName + "Nullable";
            sb.append("public with" + i.nameUpper + "(" + i.nameMangled + " : " + convertTypes(i.returnType) + ") : " + ic.immutableClassName + " {\n");
            sb.append("  return new " + ic.immutableClassName + "(" + allParamsWithOneReplaced + ");\n");
            sb.append("}\n");
            sb.append("\n");
        }
    }

    private void setter(StringBuilder sb, BeanDefFieldInfo i, String returnType) {
        sb.append("public " + i.nameMangled + "(" + i.nameMangled + " : " + convertTypes(i.returnType) + ") : " + returnType + " {\n");
        sb.append("  this._" + i.nameMangled + " = " + i.nameMangled + ";\n");
        sb.append("  return this;\n");
        sb.append("}\n");
    }

    private void buildStaticStarter(BeanDefInfo ic, StringBuilder sb) {
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
                    .map(i -> i.nameMangled + " : " + convertTypes(i.returnType))
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

    private void buildNullableInterface(BeanDefInfo ic, StringBuilder sb) {
        sb.append("export interface " + ic.immutableClassName + "Nullable {\n");
        for (int x = 0; x < ic.nullableBeanDefFieldInfos.size(); x++) {
            BeanDefFieldInfo i = ic.nullableBeanDefFieldInfos.get(x);
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + convertTypes(i.returnType) + ") : " + ic.immutableClassName + "Nullable;\n");
        }
        sb.append("  build() : " + ic.immutableClassName + ";\n");

        sb.append("}\n");
        sb.append("\n");
    }

    private void buildRequiresInterfaces(BeanDefInfo ic, StringBuilder sb) {
        if (ic.nonNullBeanDefFieldInfos.size() > 0) {
            for (int x = 0; x < ic.nonNullBeanDefFieldInfos.size() - 1; x++) {
                BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(x);
                BeanDefFieldInfo ii = ic.nonNullBeanDefFieldInfos.get(x + 1);
                sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
                sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + convertTypes(i.returnType) + ") : " + ic.immutableClassName + "Requires" + ii.nameUpper + ";\n");
                sb.append("}\n");
            }
            sb.append("\n");

            BeanDefFieldInfo i = ic.nonNullBeanDefFieldInfos.get(ic.nonNullBeanDefFieldInfos.size() - 1);
            sb.append("export interface " + ic.immutableClassName + "Requires" + i.nameUpper + " {\n");
            sb.append("  " + i.nameMangled + "(" + i.nameMangled + " : " + convertTypes(i.returnType) + ") : " + ic.immutableClassName + "Nullable;\n");
            sb.append("}\n");
        }
    }

    private void buildPrivateConstructor(StringBuilder sb) {
        sb.append("private constructor() {}\n");
        sb.append("\n");
    }

    private void buildPrivateConstructor2(BeanDefInfo ic, StringBuilder sb) {
//        sb.append("private constructor( builder : " + ic.immutableClassName + "Builder) {\n");
//        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
//            sb.append("  this._" + i.nameMangled + " = builder._" + i.nameMangled + ";\n");
//        }
//        sb.append("}");
//        sb.append("\n");

        String allParams = ic.beanDefFieldInfos.stream()
                .map(i -> i.nameMangled + " : " + convertTypes(i.returnType))
                .collect(Collectors.joining(", "));

        sb.append("private constructor( " + allParams + ") {\n");
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  this._" + i.nameMangled + " = " + i.nameMangled + ";\n");
        }
        sb.append("}");
        sb.append("\n");
    }

    private void buildFields(BeanDefInfo ic, StringBuilder sb) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append("  _" + i.nameMangled + ": " + convertTypes(i.returnType) + ";\n");
        }
        sb.append("\n");
    }

    private void buildFields2(BeanDefInfo ic, StringBuilder sb) {
        for (BeanDefFieldInfo i : ic.beanDefFieldInfos) {
            sb.append(" private readonly _" + i.nameMangled + ": " + convertTypes(i.returnType) + ";\n");
        }
        sb.append("\n");
    }


    public static String convertTypes(TypeMirror tm) {
        if(tm instanceof Type.ClassType) {
            Type.ClassType ct = (Type.ClassType) tm;

            if (ct.asElement().toString().equals(List.class.getName())) {
                final Type firstTypeArg = ct.getTypeArguments().get(0);
                return convertTypes(firstTypeArg) + "[]";
            } else if (ct.asElement().toString().equals("io.reactivex.Observable")) {
                final Type firstTypeArg = ct.getTypeArguments().get(0);
                return "Observable<" + convertTypes(firstTypeArg) + ">";
            } else if (ct.asElement().toString().equals("io.reactivex.Single")) {
                final Type firstTypeArg = ct.getTypeArguments().get(0);
                return "Observable<" + convertTypes(firstTypeArg) + ">";
            } else {
                return convertTypes(tm.toString());
            }
        }
        else if(tm instanceof Type.JCVoidType) {
            return "void";
        }
        else {
            throw new RuntimeException("Unknown type");
        }
    }

    public static String convertTypes(String javaType) {

        if (javaType.equals("org.joda.money.Money")) {
            return "string";
        }
        if (javaType.equals(BigDecimal.class.getName())) {
            return "number";
        }
        if (javaType.equals(BigInteger.class.getName())) {
            return "number";
        }
        if (javaType.equals(String.class.getName())) {
            return "string";
        }
        if (javaType.equals(int.class.getName()) || javaType.equals(Integer.class.getName())) {
            return "number";
        }
        if (javaType.equals(boolean.class.getName()) || javaType.equals(Boolean.class.getName())) {
            return "boolean";
        }
        return javaType;
    }
}
