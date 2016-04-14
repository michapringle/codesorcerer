package ca.pandp.template;

import ca.pandp.builder.Bean;
import ca.pandp.template.interfaces.A1OneNonnull;
import ca.pandp.template.interfaces.A2OneNull;
import ca.pandp.template.interfaces.A3TwoNonnull;
import ca.pandp.template.interfaces.A4TwoNull;
import ca.pandp.template.interfaces.B1A1A2;
import ca.pandp.template.interfaces.B2A1A3;
import ca.pandp.template.interfaces.B3A1A4;
import ca.pandp.template.interfaces.B4A2A3;
import ca.pandp.template.interfaces.B5A2A4;
import ca.pandp.template.interfaces.B6A3A4;

/**
 * Absolute insanity.
 * <p/>
 * Created by Micha "Micha did it!" Pringle on December 18, 2014.
 */
@Bean
public interface ABDef extends A1OneNonnull, A2OneNull, A3TwoNonnull, A4TwoNull, B1A1A2, B2A1A3, B3A1A4, B4A2A3, B5A2A4, B6A3A4
{
}
