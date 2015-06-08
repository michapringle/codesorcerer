package ca.pandp.plugins.producers;

import ca.pandp.shared.domainobjects.BeanModel;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 * This interface is used by all producers to produce the model object required by consumers.
 *
 * After extending this interface, you must add the fully qualified class name of your implementation to the file in
 * resources/META-INF/services/ca.pandp.plugins.producers.Producible.
 *
 * All implementations of this interface ought to be immutable.
 */
public interface Producible {
    BeanModel produce();
}
