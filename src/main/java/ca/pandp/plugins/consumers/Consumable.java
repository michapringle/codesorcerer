package ca.pandp.plugins.consumers;


import ca.pandp.shared.domainobjects.BeanModel;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 * .
 */

public interface Consumable {
    void consume( @javax.validation.constraints.NotNull BeanModel bean );
}
