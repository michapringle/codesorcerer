package com.codesorcerer.typescript;

import com.codesorcerer.Collector;
import com.codesorcerer.generators.def.BeanDefInputBuilder;
import com.codesorcerer.targets.TypescriptMapping;
import com.google.auto.common.MoreTypes;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacFiler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.*;

public final class TSUtils {


//    public static String getMostCommonPackage() {
//        String pkg = null;
//        final Set<String> packages = Collector.get("packages");
//        for (String p : packages) {
//            if (pkg == null) {
//                pkg = p;
//            } else {
//                pkg = StringUtils.removeEnd(StringUtils.getCommonPrefix(pkg, p), ".");
//            }
//        }
//        return pkg;
//    }

    public static void registerPackage(String pkg) {
        //Store in collector...
        if (Collector.get("packageJson") == null) {
            Collector.COLLECTOR.put("packageJson", newHashSet());
        }
        Set<String> packages = Collector.get("packageJson");
        packages.add(pkg);

    }

    public static final File DIR = new File("typescript");

    public static File getDirToWriteInto(String pkg) throws IOException {
        Set<String> packages = Collector.get("packageJson");

        String mostCommonPackage = pkg;
        while (true) {
            if (!mostCommonPackage.contains(".")) {
                mostCommonPackage = null;
                break;
            }
            if (packages == null || packages.contains(mostCommonPackage)) {
                break;
            } else {
                mostCommonPackage = StringUtils.substringBeforeLast(mostCommonPackage, ".");
            }
        }

        File dir;
        if (mostCommonPackage == null) {
            dir = DIR;
        } else {
            dir = new File(DIR, mostCommonPackage);
        }

        List<String> pkgs = Splitter.on(".").splitToList(removeStart(pkg, mostCommonPackage));
        for (String p : pkgs) {
            dir = new File(dir, p);
            FileUtils.forceMkdirParent(dir);
        }
        return dir;
    }

    public static String getFQName(TypeMirror returnTypeMirror) {
        //test.ParentDef.SubDef ==> test.Sub
        TypeElement te = MoreTypes.asTypeElement(returnTypeMirror);

        Element cur = te;
        while (!(cur instanceof PackageElement)) {
            cur = cur.getEnclosingElement();
        }
        String pkgOfBean = ((PackageElement) cur).getQualifiedName().toString();
        String beanName = te.getSimpleName().toString();
        return pkgOfBean + "." + beanName;
    }

    public static Set<TypescriptMapping> getAllMappings(TypeElement e) {
        return getAllAnnotationOnElement(e, TypescriptMapping.class);

    }

    public static <A extends Annotation> Set<A> getAllAnnotationOnElement(TypeElement e, Class<A> a) {
        final Set<A> res = Sets.newHashSet();
        final Set<Element> seen = Sets.newHashSet();
        getAllAnnotationOnElement0(e, res, seen, a);
        return res;
    }

    private static <A extends Annotation> void getAllAnnotationOnElement0(Element e,
                                                                          Set<A> accum,
                                                                          Set<Element> seen,
                                                                          Class<A> aClass) {

        final A[] aArray = e.getAnnotationsByType(aClass);
        if (aArray != null) {
            for (A tm : aArray) {
                accum.add(tm);
            }
        }

        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            final Element e1 = am.getAnnotationType().asElement();
            if (seen.add(e1)) {
                getAllAnnotationOnElement0(e1, accum, seen, aClass);
            }
        }

        //TODO: Pull from inheritance hierarchy of e
    }

    public static String getClassName(TypescriptMapping tm) {

        String fqClassName = "";
        try {
            if (tm.javaClass() == null) {
                return "???";
            }
            fqClassName = tm.javaClass().getName();
        } catch (MirroredTypeException e) {
            fqClassName = e.getTypeMirror().toString();
        }

        return fqClassName.equals("void")
                ? tm.javaClassName().toString()
                : fqClassName;
    }

    public static String getFQClassName(Class c) {
        try {
            return c.getName();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror().toString();
        }
    }


    public static String convertToImportStatements(String includingElementsPackage, Set<TypeMirror> referencedTypes, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        return referencedTypes.stream()
                .flatMap(t -> findMappingForClass(t, mappings, processingEnv).stream())
                .filter(m -> m != null)
                .map(m -> TSUtils.convertToImportStatement(includingElementsPackage, m))
                .distinct()
                .sorted()
                .collect(joining("\n", "", "\n\n"));
    }

    private static Set<TypescriptMapping> findMappingForClass(TypeMirror t, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        Set<TypescriptMapping> used = Sets.newHashSet();

        used.add(findMappingForNonParameritizedClass(t, mappings, processingEnv));

        //Loop through parameters and recurse
        if (t instanceof Type.ClassType) {
            Type.ClassType ct = (Type.ClassType) t;
            for (TypeMirror tt : ct.getTypeArguments()) {
                used.addAll(findMappingForClass(tt, mappings, processingEnv));
            }
        }

        return used;
    }

    private static TypescriptMapping findMappingForNonParameritizedClass(TypeMirror t, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {

        //Recurseive step
        if (t instanceof Type.ErrorType) {
            final String typeName = t.toString();
            //final String typeName = BeanDefInputBuilder.getBBBFQName(t);
            return find(mappings, typeName, processingEnv);
        } else if (t instanceof Type.JCVoidType) {
            return null;
        } else if (t instanceof Type.JCPrimitiveType) {
            Type.JCPrimitiveType ct = (Type.JCPrimitiveType) t;
            final String typeName = ct.asElement().getQualifiedName().toString();
            //final String typeName = BeanDefInputBuilder.getBBBFQName(t);
            return find(mappings, typeName, processingEnv);
        } else if (t instanceof Type.ClassType) {
            //Type.ClassType ct = (Type.ClassType) t;
            //final String typeName = ct.asElement().getQualifiedName().toString();
            final String typeName = BeanDefInputBuilder.getBBBFQName(t);
            return find(mappings, typeName, processingEnv);
        } else {
            throw new RuntimeException("Unknown compiler type " + t.getClass());
        }
    }


    private static TypescriptMapping find(Set<TypescriptMapping> mappings, String typeName, ProcessingEnvironment processingEnvironment) {

        System.out.println("I'm looking for " + typeName + " in mappings:");
        mappings.forEach(m -> System.out.print("  " + m.typescriptClassName() + " " + m.typescriptImportLocation()));

        //final Iterable<TypescriptMapping> allMappings = Iterables.concat(mappings, Collector.get("mappings"));
        final Iterable<TypescriptMapping> allMappings = Iterables.concat(mappings);

        for (TypescriptMapping tm : allMappings) {
            if (getClassName(tm).equals(typeName)) {
              //  System.out.println("found it " + typeName + "! " + tm.typescriptImportLocation());
                System.out.println("Got it " + tm.typescriptClassName() + " " + tm.typescriptImportLocation());
                return tm;
            }
        }

        String name2;
//        if (typeName.contains(".")) {
            name2 = typeName;
//        } else {
//            JavacFiler f = (JavacFiler) processingEnvironment.getFiler();
//            f.getGeneratedSourceNames().forEach(x -> System.out.println("   =gen=" + x));
//
//            name2 = f.getGeneratedSourceNames().stream()
//                    .filter(n -> n.equals(typeName) || n.endsWith("." + typeName))
//                    .findFirst()
//                    .orElse(typeName);
//
//            if (name2.equals(typeName)) {
//               // System.out.println("not found in filer " + name2);
//            }
//            //.orElseThrow(() -> new RuntimeException("ErrorType - No Typescript mapping to " + typeName + " in " + mappings));
//        }
//
//
        String typeName2;
        if (typeName.contains(".")) {
            typeName2 = StringUtils.substringAfterLast(typeName, ".");
       } else {
            typeName2 = typeName;
        }

//        //Look into results...
//        if (name2 == null || name2.isEmpty()) {
//            System.out.println("null " + typeName2 + " " + typeName);
//            //throw new RuntimeException("ErrorType - No Typescript mapping to " + typeName + " in " + allMappings);
//        }

        TypescriptMapping x = new TypescriptMapping() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TypescriptMapping.class;
            }

            @Override
            public String javaClassName() {
                return null;
            }

            @Override
            public Class javaClass() {
                return null;
            }

            @Override
            public String typescriptClassName() {
                return typeName2;
            }

            @Override
            public String typescriptImportLocation() {
                return name2;
            }

            @Override
            public String typescriptPackageName() {
                return null;
            }

            @Override
            public String typescriptPackageVersion() {
                return null;
            }
        };

        System.out.println("MADE it " + x.typescriptClassName() + " " + x.typescriptImportLocation() + "!!!!!");
        return x;
    }



    private static String convertToImportStatement(String includingElementsPackage, TypescriptMapping mapping) {

        //If there is not location, then we dont need to import it
        //This is like string, number or boolean, Map, Set, Array
        if (isBlank(mapping.typescriptImportLocation())) {
            return "";
        }

        final String loc = mapping.typescriptImportLocation();
        final String commonPrefix = getCommonPackagePrefix(includingElementsPackage, loc);

        final String simpleName = mapping.typescriptClassName();
        if (commonPrefix.isEmpty()) {
            if(loc.equals(mapping.typescriptClassName())) {
                //Strange case of Requests
                System.out.println("loc: " + loc + " inc: " + includingElementsPackage + " CP: " + commonPrefix + "... " + loc);
                return "import {" + simpleName + "} from './" + loc + "';  //Same package???";
            }
            System.out.println("loc: " + loc + " inc: " + includingElementsPackage + " CP: " + commonPrefix + "... " + loc);
            return "import {" + simpleName + "} from '" + loc + "';  //No common prefix - use loc from Annotation";
        }

        String locEnd = removeStart(loc, commonPrefix+".").replace('.', '/');
        String incEnd = removeStart(includingElementsPackage, commonPrefix).replace('.', '/');

        String loc2 = repeat("../", countMatches(incEnd, "/")) + locEnd;
        System.out.println("loc: " + loc + " inc: " + includingElementsPackage + " CP: " + commonPrefix + " locE: " + locEnd + " incEnd: " + incEnd + "... " + loc2);

        //Must be a relative path...
        if(!loc2.startsWith(".")) {
            loc2 = "./"  + loc2;
        }

        return "import {" + simpleName + "} from '" + loc2 + "';  //relative import";
    }

    private static String getCommonPackagePrefix(String class1, String class2) {
        List<String> locToks = Splitter.on(".").splitToList(class2);
        List<String> includingToks = Splitter.on(".").splitToList(class1);

        int count = 0;
        for (int i = 0; i < Math.min(locToks.size(), includingToks.size()); i++) {
            if (locToks.get(i).equals(includingToks.get(i))) {
                count++;
            }
            else {
                break;
            }
        }

        return locToks.stream()
                .limit(count)
                .collect(joining("."));
    }


    public static String convertToTypescriptType(TypeMirror tm, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        if (tm instanceof Type.ErrorType) {
            return tm.toString();
        } else if (tm instanceof Type.ClassType) {
            Type.ClassType ct = (Type.ClassType) tm;

            StringBuilder sb = new StringBuilder();
            TypescriptMapping mapping = findMappingForNonParameritizedClass(tm, mappings, processingEnv);
            sb.append(mapping.typescriptClassName());
            if (!ct.getTypeArguments().isEmpty()) {
                sb.append("<");
                sb.append(ct.getTypeArguments().stream()
                        .map(tt -> convertToTypescriptType(tt, mappings, processingEnv))
                        .collect(joining(",")));
                sb.append(">");
            }
            return sb.toString();
        } else if (tm instanceof Type.JCVoidType) {
            return "void";
        } else if (tm instanceof Type.JCPrimitiveType) {
            TypescriptMapping mapping = findMappingForNonParameritizedClass(tm, mappings, processingEnv);
            return mapping.typescriptClassName();
        } else {
            throw new RuntimeException("Unknown type " + tm);
        }
    }


    public static Set<TypeMirror> getReferences(ExecutableElement e) {
        Set<TypeMirror> refs = Sets.newHashSet();

        refs.add(e.getReturnType());

        e.getParameters().stream().forEach(p -> {
            refs.add(p.asType());
        });

      //  System.out.println("Adding references... " + refs);

        return refs;
    }


}
