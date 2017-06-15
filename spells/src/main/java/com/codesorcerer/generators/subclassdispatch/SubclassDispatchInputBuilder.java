package com.codesorcerer.generators.subclassdispatch;

import com.codesorcerer.abstracts.AbstractInputBuilder;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.lang.model.element.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SubclassDispatchInputBuilder extends AbstractInputBuilder<SubclassDispatchInputBuilder.SubclassDispatchInfo> {

    public static class SubclassDispatchInfo {
        public TypeElement schemeType;
        public List<TypeElement> subclasses = Lists.newArrayList();
        public String name;
        public String pkg;
        public String nameLowerCase;
    }

    @Override
    public SubclassDispatchInfo buildInput(final TypeElement te) {
        try {
            SubclassDispatchInfo info = new SubclassDispatchInfo();
            info.schemeType = te;
            info.name = te.getSimpleName().toString();
            info.nameLowerCase = info.name.toLowerCase();

            info.pkg = processingEnvironment.getElementUtils().getPackageOf(te).toString();
            //TODO: Get all subclasses in different packages?

            PackageElement packageOf = elementUtils.getPackageOf(te);

            Set<Element> found = Sets.newHashSet();
            getEnclosed(packageOf, found);

            List<TypeElement> subs = found
                    .stream()
                    .map(Element::asType)
                    .filter(MoreTypes::isType)
                    .filter(t -> typeUtils.directSupertypes(t).contains(te.asType()))
                    .map(t -> MoreTypes.asTypeElement(t))
                    .sorted(Comparator.comparing(a -> a.getSimpleName().toString()))
                    .collect(Collectors.toList());

            info.subclasses = subs;

            return info;
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }

    }

    private void getEnclosed(Element e, Set<Element> found) {
        found.addAll(e.getEnclosedElements());
        for (Element x : e.getEnclosedElements()) {
            getEnclosed(x, found);
        }
    }

}