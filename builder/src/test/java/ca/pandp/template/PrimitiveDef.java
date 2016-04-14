package ca.pandp.template;

import javax.annotation.Nonnull;

import ca.pandp.builder.BeanTemplate;

/**
 * Test for primitives, and wrapped primitive type names. Includes String and Object, as those are so commonly used.
 * <p/>
 * Created by Micha "Micha did it!" Pringle on December 12, 2014.
 */
@BeanTemplate
public interface PrimitiveDef
{
    @Nonnull
    public boolean getBooleanNN();

    @Nonnull
    public byte getByteNN();

    @Nonnull
    public short getShortNN();

    @Nonnull
    public char getCharNN();

    @Nonnull
    public int getIntNN();

    @Nonnull
    public long getLongNN();

    @Nonnull
    public float getFloatNN();

    @Nonnull
    public double getDoubleNN();

    @Nonnull
    public Object getObjectNN();

    @Nonnull
    public String getStringNN();

    @Nonnull
    public Number getNumberNN();

    @Nonnull
    public String getIntegerNN();

    @Nonnull
    public String getCharacterNN();

    @Nonnull
    public boolean[] getBooleanArrayNN();

    @Nonnull
    public byte[] getByteArrayNN();

    @Nonnull
    public short[] getShortArrayNN();

    @Nonnull
    public char[] getCharArrayNN();

    @Nonnull
    public int[] getIntArrayNN();

    @Nonnull
    public long[] getLongArrayNN();

    @Nonnull
    public float[] getFloatArrayNN();

    @Nonnull
    public double[] getDoubleArrayNN();

    @Nonnull
    public Object getObjectArrayNN();

    @Nonnull
    public String getStringArrayNN();

    @Nonnull
    public Number getNumberArrayNN();

    @Nonnull
    public String getIntegerArrayNN();

    @Nonnull
    public String getCharacterArrayNN();

    public boolean getBoolean();

    public byte getByte();

    public short getShort();

    public char getChar();

    public int getInt();

    public long getLong();

    public float getFloat();

    public double getDouble();

    public Object getObject();

    public String getString();

    public Number getNumber();

    public String getInteger();

    public String getCharacter();

    public boolean[] getBooleanArray();

    public byte[] getByteArray();

    public short[] getShortArray();

    public char[] getCharArray();

    public int[] getIntArray();

    public long[] getLongArray();

    public float[] getFloatArray();

    public double[] getDoubleArray();

    public Object getObjectArray();

    public String getStringArray();

    public Number getNumberArray();

    public String getIntegerArray();

    public String getCharacterArray();
}
