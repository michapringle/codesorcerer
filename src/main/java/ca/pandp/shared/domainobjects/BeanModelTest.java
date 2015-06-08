package ca.pandp.shared.domainobjects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import java.util.Objects;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 * .
 */

@net.jcip.annotations.Immutable
@net.jcip.annotations.ThreadSafe

public class BeanModelTest implements BeanModel {

    private final String methodName;

    public BeanModelTest(final String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BeanModelTest) {
            BeanModelTest that = (BeanModelTest) o;
            return Objects.equals(this.methodName, that.methodName);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("methodName", methodName)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(methodName);
    }
}
