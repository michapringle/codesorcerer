package com.beautifulbeanbuilder.generators.restcontroller;

import com.beautifulbeanbuilder.generators.beandef.BeanDefInfoBuilder;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    public RestControllerInfo(TypeElement typeElement, String currentTypeName, String currentTypePackage) {
        this.typeElement = typeElement;
        this.currentTypeName = currentTypeName;
        this.currentTypePackage = currentTypePackage;
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
        return BeanDefInfoBuilder.getHierarchy(typeElement, x -> ElementFilter.methodsIn(x.getEnclosedElements()));
    }

    public String getCurrentTypeName() {
        return currentTypeName;
    }

    public String getCurrentTypePackage() {
        return currentTypePackage;
    }
}
