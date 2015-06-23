package ca.pandp.plugins.producers;

import ca.pandp.shared.domainobjects.BeanModel;
import ca.pandp.shared.domainobjects.SimpleBeanModel;
import ca.pandp.shared.domainobjects.SimpleMethodModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 * .
 */

@net.jcip.annotations.Immutable
@net.jcip.annotations.ThreadSafe
public class ProducibleTest implements Producible {

    public ProducibleTest() {
    }

    public BeanModel produce() {

        final List<Annotation> annotations = Lists.newArrayList(ProducibleTest.class.getAnnotations());
        final String className = "ProducibleTest";
        final String packageName = "ca.pandp.plugins.producers";
        final List<BeanModel.MethodModel> methods = getMethods();

        return new SimpleBeanModel(annotations, className, packageName, methods);
    }

    private List<BeanModel.MethodModel> getMethods() {
        return ImmutableList.<BeanModel.MethodModel>builder()
                .add(new SimpleMethodModel(Lists.<Annotation>newArrayList(), Lists.newArrayList("public", "abstract"), "String", "getFirstName"))
                .build();
    }
}
