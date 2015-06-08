package ca.pandp.plugins.producers;

import ca.pandp.shared.domainobjects.BeanModel;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 *
 * This interface is used by all producers to produce the model object required by consumers.
 */
public interface Producible {

    BeanModel produce();
}
