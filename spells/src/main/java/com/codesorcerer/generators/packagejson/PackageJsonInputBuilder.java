package com.codesorcerer.generators.packagejson;

import com.codesorcerer.Collector;
import com.codesorcerer.abstracts.AbstractInputBuilder;

import javax.lang.model.element.TypeElement;

public class PackageJsonInputBuilder extends AbstractInputBuilder<PackageJsonInputBuilder.PackageJsonInfo> {

    public static class PackageJsonInfo {
        public String pkg;
    }

    @Override
    public PackageJsonInfo buildInput(TypeElement te) {
        PackageJsonInfo info = new PackageJsonInfo();
        info.pkg = elementUtils.getPackageOf(te).toString();
        return info;
    }

}