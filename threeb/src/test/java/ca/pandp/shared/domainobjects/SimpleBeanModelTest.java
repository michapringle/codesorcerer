package ca.pandp.shared.domainobjects;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 22, 2015.
 * <p>
 * .
 */

@net.jcip.annotations.ThreadSafe
@net.jcip.annotations.Immutable
public final class SimpleBeanModelTest {

    @org.junit.Test
    public void testEqualHashcodeContract() {

        EqualsVerifier.forClass(SimpleBeanModel.class).verify();
    }
}