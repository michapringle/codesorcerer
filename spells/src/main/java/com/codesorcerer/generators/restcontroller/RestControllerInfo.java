package com.codesorcerer.generators.restcontroller;

import com.codesorcerer.generators.def.BeanDefInputBuilder;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class RestControllerInfo {
    public TypeElement typeElement;
    private final String currentTypeName;
    private final String currentTypePackage;

    public RestControllerInfo(TypeElement te, ProcessingEnvironment processingEnv) {
        this.typeElement = te;
        currentTypeName = te.getSimpleName().toString();
        currentTypePackage = processingEnv.getElementUtils().getPackageOf(te).toString();
    }

    public List<ExecutableElement> getAllMethodsStomp() {
        return allMethodsWithAnnotation(SubscribeMapping.class);
    }

    public List<ExecutableElement> getAllMethodsRest() {
        return allMethodsWithAnnotation(RequestMapping.class);
    }

    private List<ExecutableElement> allMethodsWithAnnotation(Class<? extends Annotation> clazz) {
        return getAllMethods()
                .stream()
                .filter(e -> e.getAnnotation(clazz) != null)
                .collect(Collectors.toList());
    }

    private List<ExecutableElement> getAllMethods() {
        return BeanDefInputBuilder.getHierarchy(typeElement, x -> ElementFilter.methodsIn(x.getEnclosedElements()));
    }

    public String getCurrentTypeName() {
        return currentTypeName;
    }

    public String getCurrentTypePackage() {
        return currentTypePackage;
    }
}
