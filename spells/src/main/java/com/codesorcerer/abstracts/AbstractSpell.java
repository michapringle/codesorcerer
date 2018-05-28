package com.codesorcerer.abstracts;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

public abstract class AbstractSpell<T extends Annotation, Input, Output> implements Comparable {

    public ProcessingEnvironment processingEnvironment;
    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;

    private TypeToken<T> typeAnn = new TypeToken<T>(getClass()) {
    };

    private TypeToken<Input> typeInput = new TypeToken<Input>(getClass()) {
    };


    public Class<T> getAnnotationClass() {
        return (Class<T>) typeAnn.getRawType();
    }

    public Class<Input> getInputClass() {
        return (Class<Input>) typeInput.getRawType();
    }

    public abstract int getRunOrder();

    public abstract void prebuild(Result<AbstractSpell<T, Input, Output>, Input, Output> result, Collection<Result> results) throws Exception;

    public abstract void build(Result<AbstractSpell<T, Input, Output>, Input, Output> result) throws Exception;

    public abstract void postbuild(Result<AbstractSpell<T, Input, Output>, Input, Output> result, Collection<Result> results) throws Exception;

    public abstract void write(Result<AbstractSpell<T, Input, Output>, Input, Output> result) throws Exception;

    public abstract void processingOver(Collection<Result> results) throws Exception;

    @Override
    public int compareTo(Object o) {
        AbstractSpell g = (AbstractSpell) o;

        if (getClass() == g.getClass()) {
            return 0;
        }

        int order = getRunOrder() - g.getRunOrder();
        if (order == 0) {
            return getClass().getName().compareTo(g.getClass().getName());
        }
        return order;
    }



    public static Set<String> getAllAnnotations(Element te) {
        Set<String> res = Sets.newHashSet();
        getAllAnnotations(te, res);
        return res;
    }


    public static Set<AnnotationMirror> getAllAnnotationMirrors(Element te) {
        Set<AnnotationMirror> res = Sets.newHashSet();
        getAllAnnotationMirrors(te, res);
        return res;
    }

    public static void getAllAnnotations(Element te, Set<String> res) {
        for (AnnotationMirror am : te.getAnnotationMirrors()) {
            String annFQN = am.getAnnotationType().toString();
            if (res.add(annFQN)) {
                getAllAnnotations(am.getAnnotationType().asElement(), res);
            }
        }
    }

    public static void getAllAnnotationMirrors(Element te, Set<AnnotationMirror> res) {
        for (AnnotationMirror am : te.getAnnotationMirrors()) {
            if (res.add(am)) {
                getAllAnnotationMirrors(am.getAnnotationType().asElement(), res);
            }
        }
    }

    public static boolean hasAnnotation(Element te, Class<? extends Annotation> ann)
    {
        if(te == null) {
            return false;
        }
        return getAllAnnotations(te)
                .stream()
                .anyMatch(a -> a.equals(ann.getName()));
    }



    public static AnnotationMirror getAnnotationMirror(Element te, Class<? extends Annotation> ann) {
        return getAllAnnotationMirrors(te)
                .stream()
                .filter(a -> a.getAnnotationType().toString().equals(ann.getName()))
                .findFirst()
                .orElse(null);
    }


}