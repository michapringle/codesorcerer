package com.beautifulbeanbuilder.generators.beandef;

import com.beautifulbeanbuilder.processor.AbstractProcessor;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static org.apache.commons.lang3.StringUtils.endsWith;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BeanDefProcessor extends AbstractProcessor<BeanDefInfo> {

    @Override
    public BeanDefInfo buildInput(TypeElement te, String currentTypeName, String currentTypePackage) {
        if (te.getKind() == ElementKind.ANNOTATION_TYPE) {
            return null;
        }
        checkBBBUsage(te);
        printBeanStatus(te);
        return new BeanDefInfoBuilder().init(processingEnv, te, currentTypeName, currentTypePackage);
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