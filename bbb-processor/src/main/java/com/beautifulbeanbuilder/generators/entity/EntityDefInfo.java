package com.beautifulbeanbuilder.generators.entity;

import com.beautifulbeanbuilder.Expose;
import com.beautifulbeanbuilder.generators.beandef.BeanDefInfoBuilder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class EntityDefInfo
{
	public TypeElement typeElement;
	public String typePackage;

	public EntityDefInfo(TypeElement typeElement, String currentTypePackage) {
		this.typeElement = typeElement;
		this.typePackage = currentTypePackage;
	}

	public List<ExecutableElement> getAllMethodsExposed() {
		return allMethodsWithAnnotation(Expose.class);
	}

	private List<ExecutableElement> allMethodsWithAnnotation(Class<? extends Annotation> clazz) {
		return getAllMethods()
				.stream()
				.filter(e -> e.getAnnotation(clazz) != null)
				.collect( Collectors.toList());
	}

	private List<ExecutableElement> getAllMethods() {
		return BeanDefInfoBuilder.getHierarchy(typeElement, x -> ElementFilter.methodsIn(x.getEnclosedElements()));
	}
}
