package com.beautifulbeanbuilder.abstracts;

import com.beautifulbeanbuilder.processor.CodeSorcererProcessor;
import com.google.common.reflect.TypeToken;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collection;

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

    public abstract void build(CodeSorcererProcessor.Result<AbstractSpell<T, Input, Output>, Input, Output> result) throws Exception;

    public abstract void modify(CodeSorcererProcessor.Result<AbstractSpell<T, Input, Output>, Input, Output> result, Collection<CodeSorcererProcessor.Result> results) throws Exception;

    public abstract void write(CodeSorcererProcessor.Result<AbstractSpell<T, Input, Output>, Input, Output> result) throws Exception;

    public abstract void processingOver(Collection<CodeSorcererProcessor.Result> results) throws Exception;

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
}