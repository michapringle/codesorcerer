package ca.pandp.bean.sub;

import ca.pandp.builder.Bean;

import javax.annotation.Nonnull;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 15, 2015.
 * <p>
 * .
 */

@Bean
public interface NameDef {

    @Nonnull
    String getFirstName();

    @Nonnull
    String getLastName();

    String getMiddleName();

}
