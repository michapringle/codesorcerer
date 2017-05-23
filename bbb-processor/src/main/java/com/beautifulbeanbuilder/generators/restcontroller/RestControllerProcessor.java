package com.beautifulbeanbuilder.generators.restcontroller;

import com.beautifulbeanbuilder.processor.AbstractProcessor;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RestControllerProcessor extends AbstractProcessor<RestControllerInfo> {

    @Override
    public RestControllerInfo buildInput(TypeElement te, String currentTypeName, String currentTypePackage) {
        if (te.getKind() == ElementKind.ANNOTATION_TYPE) {
            return null;
        }

        return new RestControllerInfo(te, currentTypeName, currentTypePackage);
    }

}