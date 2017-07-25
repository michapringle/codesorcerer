package com.codesorcerer.abstracts;

import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.*;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collection;

public abstract class AbstractJavaSpell<T extends Annotation, Input> extends AbstractSpell<T, Input, TypeSpec.Builder> {
    private TypeToken<T> type = new TypeToken<T>(getClass()) {
    };

    public Class<T> getAnnotationClass() {
        return (Class<T>) type.getRawType();
    }


    @Override
    public void processingOver(Collection<Result> results) throws Exception {
        //Do nothing?
    }

    @Override
    public void modify(Result<AbstractSpell<T, Input, TypeSpec.Builder>, Input, TypeSpec.Builder> result, Collection<Result> results) throws Exception {
        //Do nothing?
    }

    @Override
    public void write(Result<AbstractSpell<T, Input, TypeSpec.Builder>, Input, TypeSpec.Builder> result) throws Exception {
        if (result.output != null) {
            TypeSpec build = result.output.build();
            String pkg = processingEnvironment.getElementUtils().getPackageOf(result.te).toString();
            JavaFile javaFile = JavaFile.builder(pkg, build).build();
            javaFile.writeTo(filer);
            //System.out.println("Conjured " + pkg + "." + build.name);
            //System.out.println(javaFile.toString());
        }
    }

    protected static <T extends Annotation, Input, Output, G extends AbstractSpell<T, Input, Output>>
    Result<G, Input, Output>
    getResult(Class<G> generatorClass, TypeElement te, Collection<Result> results) {

        for (Result r : results) {
            if (r.spell.getClass() == generatorClass && r.te == te) {
                return r;
            }
        }
//		for (Map.Entry<AbstractSpell, Object> e : generatorBuilderMap.entrySet()) {
//			if (e.getKey().getClass() == generatorClass) {
//				return (TypeSpec.Builder)e.getValue();
//			}
//		}
        throw new RuntimeException("Cant find generatorClass of type " + generatorClass.getName());
    }


    protected TypeSpec.Builder buildClass(ClassName c) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(c);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        addGeneratedAnnotation(classBuilder);
        addSuppressWarningsAnnotation(classBuilder);
        return classBuilder;
    }

    protected void addSerialVersionUUID(TypeSpec.Builder classBuilder) {
        FieldSpec.Builder builder = FieldSpec.builder(Long.class, "serialVersionUID");
        builder.addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC);
        builder.initializer("$LL", 1L);
        classBuilder.addField(builder.build());

        classBuilder.addSuperinterface(Serializable.class);
    }


    protected void addSuppressWarningsAnnotation(TypeSpec.Builder classBuilder) {
        classBuilder.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", CodeBlock.of("$S", "all"))
                .build());
    }

    protected void addGeneratedAnnotation(TypeSpec.Builder classBuilder) {
        classBuilder.addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", "CodeSorcerer"))
                .build());
    }


    protected MethodSpec.Builder startHashcode() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("hashCode");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(TypeName.INT);
        builder.addAnnotation(Override.class);
        return builder;
    }

    protected MethodSpec.Builder startToString() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toString");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(String.class);
        builder.addAnnotation(Override.class);
        return builder;
    }

    protected MethodSpec.Builder startEquals() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("equals");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(TypeName.BOOLEAN);
        builder.addAnnotation(Override.class);
        builder.addParameter(Object.class, "o");
        return builder;
    }
}
