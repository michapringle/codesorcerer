package ca.pandp.shared.domainobjects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 22, 2015.
 * <p>
 * .
 */

@net.jcip.annotations.Immutable
@net.jcip.annotations.ThreadSafe
public final class SimpleMethodModel implements BeanModel.MethodModel {

    private final List<Annotation> annotations;
    private final List<String> methodModifiers;
    private final String methodReturnType;
    private final String methodName;

    public SimpleMethodModel(List<Annotation> annotations, List<String> methodModifiers, String methodReturnType, String methodName) {
        this.annotations = annotations;
        this.methodModifiers = methodModifiers;
        this.methodReturnType = methodReturnType;
        this.methodName = methodName;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<String> getMethodModifiers() {
        return methodModifiers;
    }

    public String getMethodReturnType() {
        return methodReturnType;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleMethodModel) {
            SimpleMethodModel that = (SimpleMethodModel) o;
            return Objects.equal(this.annotations, that.annotations) &&
                    Objects.equal(this.methodModifiers, that.methodModifiers) &&
                    Objects.equal(this.methodReturnType, that.methodReturnType) &&
                    Objects.equal(this.methodName, that.methodName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(annotations, methodModifiers, methodReturnType, methodName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("annotations", annotations)
                .add("methodModifiers", methodModifiers)
                .add("methodReturnType", methodReturnType)
                .add("methodName", methodName)
                .toString();
    }
}
