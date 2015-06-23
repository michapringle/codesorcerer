package ca.pandp.plugins.consumers;


import ca.pandp.shared.domainobjects.BeanModel;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 * This interface is used by all consumers to generate an output java file based on the input bean.
 *
 * After extending this interface, you must add the fully qualified class name of your implementation to the file in
 * resources/META-INF/services/ca.pandp.plugins.consumers.Consumable.
 *
 * All implementations of this interface ought to be immutable.
 */

public interface Consumable {
    void consume( @javax.validation.constraints.NotNull BeanModel bean );
}
