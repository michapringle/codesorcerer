package com.beautifulbeanbuilder.processor.info;

import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class InfoRestController {
    public TypeElement typeElement;

    public InfoRestController(TypeElement typeElement) {
        this.typeElement = typeElement;
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
        return InfoBuilder.getHierarchy(typeElement, x -> ElementFilter.methodsIn(x.getEnclosedElements()));
    }


}
