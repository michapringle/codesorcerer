package com.test.template;

import com.beautifulbeanbuilder.BeautifulBean;
import com.test.template.interfaces.A1OneNonnull;
import com.test.template.interfaces.A2OneNull;
import com.test.template.interfaces.A3TwoNonnull;
import com.test.template.interfaces.A4TwoNull;
import com.test.template.interfaces.B1A1A2;
import com.test.template.interfaces.B2A1A3;
import com.test.template.interfaces.B3A1A4;
import com.test.template.interfaces.B4A2A3;
import com.test.template.interfaces.B5A2A4;
import com.test.template.interfaces.B6A3A4;

/**
 * Absolute insanity.
 * <p/>
 * Created by Micha "Micha did it!" Pringle on December 18, 2014.
 */
@BeautifulBean
public interface ABDef extends A1OneNonnull, A2OneNull, A3TwoNonnull, A4TwoNull, B1A1A2, B2A1A3, B3A1A4, B4A2A3, B5A2A4, B6A3A4
{
}
