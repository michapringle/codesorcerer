package com.beautifulbeanbuilder.processor;

import com.beautifulbeanbuilder.processor.info.InfoBuilder;
import com.beautifulbeanbuilder.processor.info.InfoClass;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static org.apache.commons.lang3.StringUtils.endsWith;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InfoClassProcessor extends AbstractProcessor<InfoClass> {

    @Override
    public InfoClass buildInput(TypeElement te, String currentTypeName, String currentTypePackage) {
        printBeanStatus(te);
        checkBBBUsage(te);
        return new InfoBuilder().init(processingEnv, te, currentTypeName, currentTypePackage);
    }

    private void printBeanStatus(TypeElement te) {
        System.out.println("* Making it beautiful - " + te.getQualifiedName());
    }


    private void checkBBBUsage(TypeElement te) {
        if (!endsWith(te.getSimpleName(), "Def")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Must end with Def", te);
        }
    }


}