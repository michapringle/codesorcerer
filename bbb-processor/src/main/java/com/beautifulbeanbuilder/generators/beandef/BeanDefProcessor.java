package com.beautifulbeanbuilder.generators.beandef;

import com.beautifulbeanbuilder.processor.AbstractProcessor;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static org.apache.commons.lang3.StringUtils.endsWith;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BeanDefProcessor extends AbstractProcessor<BeanDefInfo> {

    @Override
    public BeanDefInfo buildInput(TypeElement te, String currentTypeName, String currentTypePackage) {
        printBeanStatus(te);
        return new BeanDefInfoBuilder().init(processingEnv, te, currentTypeName, currentTypePackage);
    }

    private void printBeanStatus(TypeElement te) {
        System.out.println("* Making it beautiful - " + te.getQualifiedName());
    }



}