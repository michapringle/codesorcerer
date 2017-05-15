package com.beautifulbeanbuilder.processor;

import com.google.common.reflect.TypeToken;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractGenerator<T extends Annotation, Input, Output> {

    private TypeToken<T> typeAnn = new TypeToken<T>(getClass()) {
    };

    private TypeToken<Input> typeInput = new TypeToken<Input>(getClass()) {
    };

    public List<Class<? extends Annotation>> requires() {
        return Collections.emptyList();
    }

    public Class<T> getAnnotationClass() {
        return (Class<T>) typeAnn.getRawType();
    }
    public Class<Input> getInputClass() {
        return (Class<Input>) typeInput.getRawType();
    }

    public abstract Output build(Input ic, Map<AbstractJavaGenerator, Object> generatorBuilderMap, ProcessingEnvironment processingEnv) throws Exception;

    public abstract void write(Input ic, Output objectToWrite, ProcessingEnvironment processingEnv) throws Exception;

    public abstract void processingOver(Collection<Output> objects, ProcessingEnvironment processingEnv) throws Exception;
}