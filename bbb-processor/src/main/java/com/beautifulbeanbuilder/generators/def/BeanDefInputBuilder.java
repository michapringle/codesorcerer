package com.beautifulbeanbuilder.generators.def;

import com.beautifulbeanbuilder.abstracts.AbstractInputBuilder;
import com.beautifulbeanbuilder.generators.def.spells.Types;
import com.beautifulbeanbuilder.typescript.TSUtils;
import com.beautifulbeanbuilder.generators.def.BeanDefInfo.BeanDefFieldInfo;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Type;

import javax.annotation.Nonnull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.collect.Iterables.toArray;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

public class BeanDefInputBuilder extends AbstractInputBuilder<BeanDefInfo> {

    @Override
    public BeanDefInfo buildInput(TypeElement te) {
        checkBBBUsage(te);
        printBeanStatus(te);
        return init(processingEnvironment, te);
    }


    public static final Set<String> KEYWORDS = ImmutableSet.of("abstract", "continue", "for", "new", "switch",
            "assert", "default", "goto", "package", "synchronized",
            "boolean", "do", "if", "private", "this",
            "break", "double", "implements", "protected", "throw",
            "byte", "else", "import", "public", "throws",
            "case", "enum", "instanceof", "return", "transient",
            "catch", "extends", "int", "short", "try",
            "char", "final", "interface", "static", "void",
            "class", "finally", "long", "strictfp", "volatile",
            "const", "float", "native", "super", "while");


    private BeanDefInfo init(ProcessingEnvironment processingEnv, TypeElement te) {

        final BeanDefInfo ic = new BeanDefInfo();
        String currentTypeName = te.getSimpleName().toString();
        String currentTypePackage = processingEnv.getElementUtils().getPackageOf(te).toString();

        String pkg = removeEnd(currentTypePackage, ".def");
        String bbbWithNoDef = removeEnd(currentTypeName, "Def");
        ic.typeElement = te;
        ic.isInterfaceDef = te.getKind() == ElementKind.INTERFACE;
        ic.typeDef = ClassName.get(te);
        ic.typeImmutable = ClassName.get(pkg, bbbWithNoDef);
        ic.pkg = pkg;
        ic.immutableClassName = bbbWithNoDef;
        ic.beanDefFieldInfos = parseGetters(processingEnv, te);
        ic.nonNullBeanDefFieldInfos = ic.beanDefFieldInfos.stream().filter(i -> i.isNonNull).collect(toList());
        ic.nullableBeanDefFieldInfos = ic.beanDefFieldInfos.stream().filter(i -> !i.isNonNull).collect(toList());
        ic.typeCallbackImpl = ParameterizedTypeName.get(Types.jpCallback, ic.typeImmutable);
        return ic;
    }

    private List<BeanDefFieldInfo> parseGetters(ProcessingEnvironment processingEnv, TypeElement te) {
        final Set<String> removeDups = Sets.newHashSet();
        return getAllMethods(te)
                .stream()
                .filter(this::isGetter)
                .filter(ee -> removeDups.add(ee.getSimpleName().toString()))
                .map(e -> buildInfo(processingEnv, e))
                .collect(toList());
    }


    private BeanDefFieldInfo buildInfo(ProcessingEnvironment processingEnv, ExecutableElement getter) {
        BeanDefFieldInfo beanDefFieldInfo = new BeanDefFieldInfo();
        beanDefFieldInfo.getter = getter;
        TypeMirror returnTypeMirror = getter.getReturnType();

        beanDefFieldInfo.prefix = getPrefix(getter);
        beanDefFieldInfo.nameUpper = removePrefix(getter);
        beanDefFieldInfo.nameAllUpper = beanDefFieldInfo.nameUpper.toUpperCase();
        beanDefFieldInfo.name = uncapitalize(beanDefFieldInfo.nameUpper);
        beanDefFieldInfo.nameMangled = beanDefFieldInfo.name + (KEYWORDS.contains(beanDefFieldInfo.name.toLowerCase()) ? "_" : "");


        beanDefFieldInfo.isComparable = isComparable(processingEnv, returnTypeMirror);
//        beanDefFieldInfo.isBB = isBBB(returnTypeMirror);
        beanDefFieldInfo.isNonNull = isNonNull(getter, isPrimitive(returnTypeMirror));
        beanDefFieldInfo.nReturnType = calcReturnTypes(returnTypeMirror);

        beanDefFieldInfo.returnType = beanDefFieldInfo.nReturnType.toString();


        return beanDefFieldInfo;
    }


    public static boolean isArray(TypeMirror returnTypeMirror) {
        return returnTypeMirror instanceof ArrayType;
    }

    public static boolean isPrimitive(TypeMirror returnTypeMirror) {
        return returnTypeMirror instanceof PrimitiveType;
    }

    private boolean isComparable(ProcessingEnvironment processingEnv, TypeMirror returnTypeMirror) {
        TypeMirror comparable = processingEnv.getTypeUtils().erasure(processingEnv.getElementUtils().getTypeElement(Comparable.class.getName()).asType());
        return isPrimitive(returnTypeMirror) || processingEnv.getTypeUtils().isAssignable(returnTypeMirror, comparable);
    }

    private boolean isNonNull(ExecutableElement getter, boolean isPrimitive) {
        return !isPrimitive && getter.getAnnotation(Nonnull.class) != null;
    }

    private TypeName calcReturnTypes(TypeMirror returnTypeMirror) {

        if (endsWith(returnTypeMirror.toString(), "Def")) {
            return ClassName.bestGuess(getBBBFQName(returnTypeMirror));
        }

        //Single Container
        if (returnTypeMirror instanceof Type.ClassType) {
            TypeElement e = MoreTypes.asTypeElement(returnTypeMirror);
            Type.ClassType ct = (Type.ClassType) returnTypeMirror;
            com.sun.tools.javac.util.List<Type> tes = ct.getTypeArguments();

            List<TypeName> typesFixed = tes
                    .stream()
                    .map(tt -> {
                        if (tt instanceof WildcardType) {
                            TypeMirror extendsBound = ((WildcardType) tt).getExtendsBound();
                            return calcReturnTypes(extendsBound);
                        }
                        return TypeName.get(tt);
                    })
                    .collect(toList());

            if (!typesFixed.isEmpty()) {
                return ParameterizedTypeName.get(ClassName.get(e), toArray(typesFixed, TypeName.class));
            }

        }

        return TypeName.get(returnTypeMirror);
    }

    public static String getBBBFQName(TypeMirror returnTypeMirror) {
        return removeEnd(TSUtils.getFQName(returnTypeMirror), "Def");
    }


    private String removePrefix(ExecutableElement getter) {
        String name = getter.getSimpleName().toString();
        return removeStart(name, getPrefix(getter));
    }

    private String getPrefix(ExecutableElement getter) {
        String name = getter.getSimpleName().toString();
        return startsWith(name, "get")
                ? "get"
                : "is";
    }


    private boolean isGetter(ExecutableElement m) {
        return MoreElements.hasModifiers(Modifier.ABSTRACT).apply(m) &&
                (startsWith(m.getSimpleName(), "get") || startsWith(m.getSimpleName(), "is"));
    }


    public List<ExecutableElement> getAllMethods(TypeElement te) {
        return getHierarchy(te, x -> ElementFilter.methodsIn(x.getEnclosedElements()));
    }

    /**
     * @param f A function that converts a TypeElement (Class/Interface) to a list of things for that level in the hierarchy
     */
    public static <T> List<T> getHierarchy(TypeElement te, Function<TypeElement, List<T>> f) {
        final List<T> elems = Lists.newArrayList();
        getHierarchy(te, elems, f);
        return elems;
    }

    private static <T> void getHierarchy(TypeElement te, List<T> list, Function<TypeElement, List<T>> f) {
        //Recursivly go up the interface hierarchy
        for (TypeMirror i : te.getInterfaces()) {
            getHierarchy(asTypeElement(i), list, f);
        }

        //Go up the class hierarchy
        if (te.getSuperclass().getKind() != TypeKind.NONE) {
            final TypeElement superC = asTypeElement(te.getSuperclass());
            if (!superC.getQualifiedName().toString().equals(Object.class.getName())) {
                getHierarchy(superC, list, f);
            }
        }

        //Filter out those Ts that are already in the list...
        final List<T> newTs = f.apply(te)
                .stream()
                .filter(x -> !list.contains(x))
                .collect(toList());

        //Add them
        list.addAll(newTs);
    }

    private void printBeanStatus(TypeElement te) {
//        System.out.println("* Making it beautiful - " + te.getQualifiedName());
    }

    private void checkBBBUsage(TypeElement te) {
        if (!endsWith(te.getSimpleName(), "Def")) {
            throw new RuntimeException("Must end with Def");
        }
    }


}