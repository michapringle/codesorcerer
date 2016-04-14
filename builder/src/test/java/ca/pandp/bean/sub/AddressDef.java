package ca.pandp.bean.sub;

import ca.pandp.builder.BeanTemplate;

import javax.annotation.Nonnull;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 15, 2015.
 * <p>
 * .
 */
@BeanTemplate
public interface AddressDef {

    @Nonnull
    String getStreetAddress();
}
