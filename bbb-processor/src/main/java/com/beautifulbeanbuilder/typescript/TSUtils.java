package com.beautifulbeanbuilder.typescript;

import com.beautifulbeanbuilder.TypescriptMapping;
import com.beautifulbeanbuilder.generators.beandef.BeanDefInfoBuilder;
import com.google.common.collect.Sets;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacFiler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class TSUtils {

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


    public static String convertToImportStatements(Set<TypeMirror> referencedTypes, Set<TypescriptMapping> mappings, ProcessingEnvironment processingEnv) {
        return referencedTypes.stream()
                .flatMap(t -> findMappingForClass(t, mappings, processingEnv).stream())
                .filter(m -> m != null)
                .map(TSUtils::convertToImportStatement)
                .distinct()
                .sorted()
                .collect(Collectors.joining("\n", "", "\n\n"));
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
            //final String typeName = BeanDefInfoBuilder.getBBBFQName(t);
            return find(mappings, typeName, processingEnv);
        } else if (t instanceof Type.JCVoidType) {
            return null;
        } else if (t instanceof Type.JCPrimitiveType) {
            Type.JCPrimitiveType ct = (Type.JCPrimitiveType) t;
            final String typeName = ct.asElement().getQualifiedName().toString();
            //final String typeName = BeanDefInfoBuilder.getBBBFQName(t);
            return find(mappings, typeName, processingEnv);
        } else if (t instanceof Type.ClassType) {
            //Type.ClassType ct = (Type.ClassType) t;
            //final String typeName = ct.asElement().getQualifiedName().toString();
            final String typeName = BeanDefInfoBuilder.getBBBFQName(t);
            return find(mappings, typeName, processingEnv);
        } else {
            throw new RuntimeException("Unknown compiler type " + t.getClass());
        }
    }


    private static TypescriptMapping find(Set<TypescriptMapping> mappings, String typeName, ProcessingEnvironment processingEnvironment) {

        Optional<TypescriptMapping> first = mappings.stream()
                .filter(x -> getClassName(x).equals(typeName))
                .findFirst();

        if (first.isPresent()) {
            return first.get();
        }

        String name2;
        if (typeName.contains(".")) {
            name2 = typeName;
        } else {
            JavacFiler f = (JavacFiler) processingEnvironment.getFiler();
            name2 = f.getGeneratedSourceNames().stream()
                    .filter(n -> n.equals(typeName) || n.endsWith("." + typeName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("ErrorType - No Typescript mapping to " + typeName + " in " + mappings));
        }


        String typeName2;
        if (typeName.contains(".")) {
            typeName2 = StringUtils.substringAfterLast(typeName, ".");
        } else {
            typeName2 = typeName;
        }

        return new TypescriptMapping() {
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
    }

    private static String convertToImportStatement(TypescriptMapping mapping) {

        //If there is not location, then we dont need to import it
        // eg: boolean
        if (mapping.typescriptImportLocation().isEmpty()) {
            return "";
        }
        final String simpleName = mapping.typescriptClassName();
        final String loc = mapping.typescriptImportLocation();
        final String className = getClassName(mapping);
        //System.out.println("Mapping " + className + " to " + simpleName + " " + loc);
        return "import {" + simpleName + "} from '" + loc + "';";
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
                        .collect(Collectors.joining(",")));
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
        RequestMapping rm = e.getAnnotation(RequestMapping.class);
        e.getParameters().stream().forEach(p -> refs.add(p.asType()));
        return refs;
    }


}
