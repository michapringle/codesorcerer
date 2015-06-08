package ca.pandp.plugins.producers;

import ca.pandp.shared.domainobjects.BeanModel;
import ca.pandp.shared.domainobjects.BeanModelTest;

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
        return new BeanModelTest("Brillant");
    }
}
