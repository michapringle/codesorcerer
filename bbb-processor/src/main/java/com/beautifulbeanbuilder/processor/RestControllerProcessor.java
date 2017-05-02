package com.beautifulbeanbuilder.processor;

import com.beautifulbeanbuilder.processor.info.InfoRestController;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RestControllerProcessor extends AbstractProcessor<InfoRestController> {

    @Override
    public InfoRestController buildInput(TypeElement te, String currentTypeName, String currentTypePackage) {
        return new InfoRestController(te);
    }

}