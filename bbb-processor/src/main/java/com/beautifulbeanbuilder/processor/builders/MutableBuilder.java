package com.beautifulbeanbuilder.processor.builders;

import com.beautifulbeanbuilder.processor.info.InfoClass;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public class MutableBuilder extends AbstractBuilder {

    public TypeSpec.Builder build(InfoClass ic, TypeSpec.Builder immutableClassBuilder) throws IOException {
        ClassName typeMutable = ClassName.get(ic.pkg, ic.immutableClassName + "Mutable");

        final TypeSpec.Builder classBuilder = buildClass(typeMutable);
        addSerialVersionUUID(classBuilder);

        //Members
        ic.infos.forEach(i -> {
            final FieldSpec f = i.buildField(Modifier.PRIVATE);
            classBuilder.addField(f);
        });

        //Getters
        ic.infos.forEach(i -> {
            final MethodSpec getter = i.buildGetter();
            classBuilder.addMethod(getter);
        });

        //Setters
        ic.infos.forEach(i -> {
            final MethodSpec setter = i.buildSetter();
            classBuilder.addMethod(setter);
        });

        addToImmutable(ic, classBuilder);
        addToMutable(ic, immutableClassBuilder, typeMutable);

        return classBuilder;
    }

    private void addToMutable(InfoClass ic, TypeSpec.Builder classBuilder, ClassName typeMutable) {
        MethodSpec.Builder toMutable1 = MethodSpec.methodBuilder("toMutable");
        toMutable1.addModifiers(Modifier.PUBLIC);
        toMutable1.returns(typeMutable);
        toMutable1.addStatement("return toMutable(this)");
        classBuilder.addMethod(toMutable1.build());

        MethodSpec.Builder toMutable = MethodSpec.methodBuilder("toMutable");
        toMutable.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        toMutable.returns(typeMutable);
        toMutable.addParameter(ic.typeImmutable, "immutable");
        toMutable.addStatement("$T mutable = new $T()", typeMutable, typeMutable);
        toMutable.addStatement("if(immutable != null) {");
        ic.infos.forEach(i -> toMutable.addStatement("mutable.set" + i.nameUpper + "(immutable." + i.prefix + i.nameUpper + "())"));
        toMutable.addStatement("}");
        toMutable.addStatement("return mutable");
        classBuilder.addMethod(toMutable.build());

    }

    private void addToImmutable(InfoClass ic, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder toMutable1 = MethodSpec.methodBuilder("toImmutable");
        toMutable1.addModifiers(Modifier.PUBLIC);
        toMutable1.returns(ic.typeImmutable);

        CodeBlock.Builder cb = CodeBlock.builder();
        cb.add("return $T.build" + ic.immutableClassName + "()", ic.typeImmutable);
        cb.indent();
        ic.nonNullInfos.forEach(i -> cb.add("." + i.nameMangled + "(" + i.nameMangled + ")"));
        ic.nullableInfos.forEach(i -> cb.add("." + i.nameMangled + "(" + i.nameMangled + ")"));
        cb.add(".build();");
        cb.unindent();
        toMutable1.addCode(cb.build());

        classBuilder.addMethod(toMutable1.build());
    }
}
