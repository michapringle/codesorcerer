package com.codesorcerer.generators.restcontroller;


import com.codesorcerer.abstracts.AbstractInputBuilder;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RestControllerInputBuilder extends AbstractInputBuilder<RestControllerInfo> {

    @Override
    public RestControllerInfo buildInput(TypeElement te) {

        if (te.getKind() == ElementKind.ANNOTATION_TYPE) {
            return null;
        }

        return new RestControllerInfo(te, processingEnvironment);
    }

}