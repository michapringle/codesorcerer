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
public class SimpleBeanModel implements BeanModel {

    private final List<Annotation> annotations;
    private final String className;
    private final String packageName;
    private final List<MethodModel> methods;

    public SimpleBeanModel(List<Annotation> annotations, String className, String packageName, List<MethodModel> methods) {
        this.annotations = annotations;
        this.className = className;
        this.packageName = packageName;
        this.methods = methods;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<MethodModel> getMethods() {
        return methods;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleBeanModel) {
            SimpleBeanModel that = (SimpleBeanModel) o;
            return Objects.equal(this.annotations, that.annotations) &&
                    Objects.equal(this.className, that.className) &&
                    Objects.equal(this.packageName, that.packageName) &&
                    Objects.equal(this.methods, that.methods);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(annotations, className, packageName, methods);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("annotations", annotations)
                .add("className", className)
                .add("packageName", packageName)
                .add("methods", methods)
                .toString();
    }
}
