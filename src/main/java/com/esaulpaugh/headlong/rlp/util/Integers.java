package com.esaulpaugh.headlong.rlp.util;

import com.esaulpaugh.headlong.rlp.DecodeException;

import java.math.BigInteger;
import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;

public class Integers {

    public static byte getByte(byte[] buffer, int i, int numBytes) throws DecodeException {
        switch (numBytes) {
        case 1:
            byte lead = buffer[i];
            if(lead == 0) {
                throw new DecodeException("Deserialised integers with leading zeroes are invalid: " + i + ", " + numBytes);
            }
            return lead;
        case 0: return 0;
        default: throw new DecodeException(new IllegalArgumentException("numBytes out of range: " + numBytes));
        }
    }

    public static short getShort(byte[] buffer, int i, int numBytes) throws DecodeException {
        int shiftAmount = 0;
        int val = 0;
        switch (numBytes) { /* cases 2 through 1 fall through */
        case 2: val = buffer[i+1] & 0xFF; shiftAmount = Byte.SIZE; // & 0xFF to promote to int before left shift
        case 1:
            byte lead = buffer[i];
            val |= (lead & 0xFF) << shiftAmount;
            if(lead == 0) {
                throw new DecodeException("Deserialised integers with leading zeroes are invalid: " + i + ", " + numBytes);
            }
        case 0: return (short) val;
        default: throw new DecodeException(new IllegalArgumentException("numBytes out of range: " + numBytes));
        }
    }

    public static int getInt(byte[] buffer, int i, int numBytes) throws DecodeException {
        int shiftAmount = 0;
        int val = 0;
        switch (numBytes) { /* cases 4 through 1 fall through */
        case 4: val = buffer[i+3] & 0xFF; shiftAmount = Byte.SIZE;
        case 3: val |= (buffer[i+2] & 0xFF) << shiftAmount; shiftAmount += Byte.SIZE;
        case 2: val |= (buffer[i+1] & 0xFF) << shiftAmount; shiftAmount += Byte.SIZE;
        case 1:
            byte lead = buffer[i];
            val |= (lead & 0xFF) << shiftAmount;
            if(lead == 0) {
                throw new DecodeException("Deserialised integers with leading zeroes are invalid: " + i + ", " + numBytes);
            }
        case 0: return val;
        default: throw new DecodeException(new IllegalArgumentException("numBytes out of range: " + numBytes));
        }
    }

    public static long getLong(final byte[] buffer, final int i, final int numBytes) throws DecodeException {
        int shiftAmount = 0;
        long val = 0L;
        switch (numBytes) { /* cases 8 through 1 fall through */
        case 8: val = buffer[i+7] & 0xFF; shiftAmount = Byte.SIZE;
        case 7: val |= (buffer[i+6] & 0xFF) << shiftAmount; shiftAmount += Byte.SIZE;
        case 6: val |= (buffer[i+5] & 0xFF) << shiftAmount; shiftAmount += Byte.SIZE;
        case 5: val |= (buffer[i+4] & 0xFFL) << shiftAmount; shiftAmount += Byte.SIZE;
        case 4: val |= (buffer[i+3] & 0xFFL) << shiftAmount; shiftAmount += Byte.SIZE;
        case 3: val |= (buffer[i+2] & 0xFFL) << shiftAmount; shiftAmount += Byte.SIZE;
        case 2: val |= (buffer[i+1] & 0xFFL) << shiftAmount; shiftAmount += Byte.SIZE;
        case 1:
            byte lead = buffer[i];
            val |= (lead & 0xFFL) << shiftAmount;
            if(lead == 0) {
                throw new DecodeException("Deserialised integers with leading zeroes are invalid: " + i + ", " + numBytes);
            }
        case 0: return val;
        default: throw new DecodeException(new IllegalArgumentException("numBytes out of range: " + numBytes));
        }
    }

    public static byte[] toBytes(byte val) {
        if(val == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        return new byte[] { val };
    }

    public static byte[] toBytes(short val) {
        if(val == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        int n = numBytes(val);
        byte[] bytes = new byte[n];
        putShort(val, bytes, 0);
        return bytes;
    }

    public static byte[] toBytes(int val) {
        if(val == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        int n = numBytes(val);
        byte[] bytes = new byte[n];
        putInt(val, bytes, 0);
        return bytes;
    }

    public static byte[] toBytes(long val) {
        if(val == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        int n = numBytes(val);
        byte[] bytes = new byte[n];
        putLong(val, bytes, 0);
        return bytes;
    }

    /**
     *
     * @param val
     * @param o
     * @param i
     * @return  the number of bytes inserted
     */
    public static int putByte(byte val, byte[] o, int i) {
        if(val != 0) {
            o[i] = val;
            return 1;
        }
        return 0;
    }

    /**
     *
     * @param val
     * @param o
     * @param i
     * @return  the number of bytes inserted
     */
    public static int putShort(short val, byte[] o, int i) {
        byte b = 0;
        int n = 0;
        if(val != 0) {
            n = 1;
            b = (byte) val;
//            val = (short) (val >>> Byte.SIZE); // ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT
            val = (short) (val >> Byte.SIZE); // high bytes chopped off either way, see above
            if (val != 0) {
                n = 2;
            }
        }
        switch (n) {
        case 0: return 0;
        case 1: o[i]=b; return 1;
        default: o[i]=(byte)val; o[i+1]=b; return 2;
        }
    }

    /**
     *
     * @param val
     * @param o
     * @param i
     * @return  the number of bytes inserted
     */
    public static int putInt(int val, byte[] o, int i) {
        byte b = 0, c = 0, d = 0;
        int n = 0;
        if(val != 0) {
            n = 1;
            d = (byte) val;
            val = val >>> Byte.SIZE;
            if (val != 0) {
                n = 2;
                c = (byte) val;
                val = val >>> Byte.SIZE;
                if (val != 0) {
                    n = 3;
                    b = (byte) val;
                    val = val >>> Byte.SIZE;
                    if (val != 0) {
                        n = 4;
                    }
                }
            }
        }
        switch (n) {
        case 0: return 0;
        case 1: o[i]=d; return 1;
        case 2: o[i]=c; o[i+1]=d; return 2;
        case 3: o[i]=b; o[i+1]=c; o[i+2]=d; return 3;
        default:
        o[i]=(byte)val; o[i+1]=b; o[i+2]=c; o[i+3]=d; return 4;
        }
    }

    /**
     *
     * @param val
     * @param o
     * @param i
     * @return  the number of bytes inserted
     */
    public static int putLong(long val, byte[] o, int i) {
        byte b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0;
        int n = 0;
        if(val != 0) {
            n = 1;
            h = (byte) val;
            val = val >>> Byte.SIZE;
            if (val != 0) {
                n = 2;
                g = (byte) val;
                val = val >>> Byte.SIZE;
                if (val != 0) {
                    n = 3;
                    f = (byte) val;
                    val = val >>> Byte.SIZE;
                    if (val != 0) {
                        n = 4;
                        e = (byte) val;
                        val = val >>> Byte.SIZE;
                        if (val != 0) {
                            n = 5;
                            d = (byte) val;
                            val = val >>> Byte.SIZE;
                            if (val != 0) {
                                n = 6;
                                c = (byte) val;
                                val = val >>> Byte.SIZE;
                                if (val != 0) {
                                    n = 7;
                                    b = (byte) val;
                                    val = val >>> Byte.SIZE;
                                    if (val != 0) {
                                        n = 8;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        switch (n) {
        case 0: return 0;
        case 1: o[i]=h; return 1;
        case 2: o[i]=g; o[i+1]=h; return 2;
        case 3: o[i]=f; o[i+1]=g; o[i+2]=h; return 3;
        case 4: o[i]=e; o[i+1]=f; o[i+2]=g; o[i+3]=h; return 4;
        case 5: o[i]=d; o[i+1]=e; o[i+2]=f; o[i+3]=g; o[i+4]=h; return 5;
        case 6: o[i]=c; o[i+1]=d; o[i+2]=e; o[i+3]=f; o[i+4]=g; o[i+5]=h; return 6;
        case 7: o[i]=b; o[i+1]=c; o[i+2]=d; o[i+3]=e; o[i+4]=f; o[i+5]=g; o[i+6]=h; return 7;
        default:
        o[i]=(byte)val; o[i+1]=b; o[i+2]=c; o[i+3]=d; o[i+4]=e; o[i+5]=f; o[i+6]=g; o[i+7]=h; return 8;
        }
    }

    public static int numBytes(short val) {
        int n = 0;
        if(val != 0) {
            n = 1;
//            val = (short) (val >>> Byte.SIZE); // ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT
            val = (short) (val >> Byte.SIZE); // high bytes chopped off either way, see above
            if (val != 0) {
                return 2;
            }
        }
        return n;
    }

    public static int numBytes(int val) {
        int n = 0;
        if(val != 0) {
            n = 1;
            val = val >>> Byte.SIZE;
            if (val != 0) {
                n = 2;
                val = val >>> Byte.SIZE;
                if (val != 0) {
                    n = 3;
                    val = val >>> Byte.SIZE;
                    if (val != 0) {
                        return 4;
                    }
                }
            }
        }
        return n;
    }

    public static int numBytes(long val) {
        int n = 0;
        if(val != 0) {
            n = 1;
            val = val >>> Byte.SIZE;
            if (val != 0) {
                n = 2;
                val = val >>> Byte.SIZE;
                if (val != 0) {
                    n = 3;
                    val = val >>> Byte.SIZE;
                    if (val != 0) {
                        n = 4;
                        val = val >>> Byte.SIZE;
                        if (val != 0) {
                            n = 5;
                            val = val >>> Byte.SIZE;
                            if (val != 0) {
                                n = 6;
                                val = val >>> Byte.SIZE;
                                if (val != 0) {
                                    n = 7;
                                    val = val >>> Byte.SIZE;
                                    if (val != 0) {
                                        return 8;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return n;
    }

    public static void insertBytes(int n, byte[] b, int i, byte w, byte x, byte y, byte z) {
        insertBytes(n, b, i, (byte) 0, (byte) 0, (byte) 0, (byte) 0, w, x, y, z);
    }

    /**
     * Inserts bytes into an array in the order they are given.
     * @param n     the number of bytes to insert
     * @param b     the buffer into which the bytes will be inserted
     * @param i     the index at which to insert
     * @param s     the lead byte if eight bytes are to be inserted
     * @param t     the lead byte if seven bytes are to be inserted
     * @param u     the lead byte if six bytes are to be inserted
     * @param v     the lead byte if five bytes are to be inserted
     * @param w     the lead byte if four bytes are to be inserted
     * @param x     the lead byte if three bytes are to be inserted
     * @param y     the lead byte if two bytes are to be inserted
     * @param z     the last byte
     */
    public static void insertBytes(int n, byte[] b, int i, byte s, byte t, byte u, byte v, byte w, byte x, byte y, byte z) {
        switch (n) { /* cases fall through */
        case 8: b[i++] = s;
        case 7: b[i++] = t;
        case 6: b[i++] = u;
        case 5: b[i++] = v;
        case 4: b[i++] = w;
        case 3: b[i++] = x;
        case 2: b[i++] = y;
        case 1: b[i] = z;
        }
    }

    public static BigInteger getBigInt(byte[] bytes, int i, int numBytes) {
        return new BigInteger(Arrays.copyOfRange(bytes, i, i + numBytes));
    }

    public static int putBigInt(BigInteger val, byte[] o, int i) {
        byte[] bytes = val.toByteArray();
        final int len = bytes.length;
        System.arraycopy(bytes, 0, o, i, len);
        return len;
    }
}
