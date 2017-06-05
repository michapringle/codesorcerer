package com.codesorcerer.generators.packagejson;

import com.codesorcerer.Collector;
import com.codesorcerer.abstracts.AbstractInputBuilder;
import com.codesorcerer.targets.TypescriptRoot;

import javax.lang.model.element.TypeElement;

public class PackageJsonInputBuilder extends AbstractInputBuilder<PackageJsonInputBuilder.PackageJsonInfo> {

    public static class PackageJsonInfo {
        public String pkg;
        public String version;
    }

    @Override
    public PackageJsonInfo buildInput(TypeElement te) {
        PackageJsonInfo info = new PackageJsonInfo();
        info.pkg = elementUtils.getPackageOf(te).toString();
        info.version = te.getAnnotation(TypescriptRoot.class).version();
        return info;
    }

}