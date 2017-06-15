package com.codesorcerer.generators.packagejson;

import com.codesorcerer.Collector;
import com.codesorcerer.abstracts.AbstractSpell;
import com.codesorcerer.abstracts.Result;
import com.codesorcerer.generators.packagejson.PackageJsonInputBuilder.PackageJsonInfo;
import com.codesorcerer.targets.TypescriptMapping;
import com.codesorcerer.targets.TypescriptRoot;
import com.codesorcerer.typescript.PackageJson;
import com.codesorcerer.typescript.TSUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;

public class PackageJsonSpell extends AbstractSpell<TypescriptRoot, PackageJsonInfo, PackageJson> {

    @Override
    public int getRunOrder() {
        return 1;
    }


    @Override
    public void processingOver(Collection<Result> results) throws Exception {
    }


    @Override
    public void modify(Result<AbstractSpell<TypescriptRoot, PackageJsonInfo, PackageJson>, PackageJsonInfo, PackageJson> result, Collection<Result> results) throws Exception {

    }

    @Override
    public void write(Result<AbstractSpell<TypescriptRoot, PackageJsonInfo, PackageJson>, PackageJsonInfo, PackageJson> result) throws Exception {
        File dir = TSUtils.getDirToWriteInto(result.input.pkg);
        FileUtils.write(new File(dir, "package.json"), result.output.toJson(), Charset.defaultCharset());
    }


    @Override
    public void build(Result<AbstractSpell<TypescriptRoot, PackageJsonInfo, PackageJson>, PackageJsonInfo, PackageJson> result) throws Exception {
        TSUtils.registerPackage(result.input.pkg);

        PackageJson packageJson = new PackageJson();
        //packageJson.version = "0.0.0.0.0-SNAPSHOT";
        packageJson.version = result.input.version;
        packageJson.name = result.input.pkg;


        //Add devDependencies
        final Set<TypescriptMapping> mappings = Collector.get("mappings");
        for (TypescriptMapping tm : mappings) {
            if (!tm.typescriptPackageName().isEmpty()) {
                packageJson.peerDependencies.put(tm.typescriptPackageName(), tm.typescriptPackageVersion());
            }
        }
        packageJson.peerDependencies.put("class-transformer", "^0.1.6");
        packageJson.peerDependencies.put("@c1/stomp-client", "^0.0.1");
        packageJson.peerDependencies.put("qwest", "^4.4.6");

        result.output = packageJson;
    }


}
