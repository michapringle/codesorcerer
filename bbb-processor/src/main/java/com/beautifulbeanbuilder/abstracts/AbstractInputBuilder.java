package com.beautifulbeanbuilder.abstracts;

import com.google.common.reflect.TypeToken;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class AbstractInputBuilder<Input>  {

    public ProcessingEnvironment processingEnvironment;
    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;


    private TypeToken<Input> type = new TypeToken<Input>(getClass()) {
    };

    public Class<Input> getInputClass() {
        return (Class<Input>) type.getRawType();
    }


    public abstract Input buildInput(TypeElement te);

}