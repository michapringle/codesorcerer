package ca.pandp.bean.sub;

import ca.pandp.builder.BeanTemplate;

import javax.annotation.Nonnull;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 15, 2015.
 * <p>
 * .
 */


@BeanTemplate
public interface PersonDef {
    @Nonnull
    NameDef getName();

    @Nonnull
    AddressDef getAddress();

    @Nonnull
    Sex getSex();

    String getOccupation();

    long getBirthDate();
}
