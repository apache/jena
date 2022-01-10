/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.tdb2.store.value;

import org.apache.jena.atlas.lib.BitsLong;

/**
 * Doubles packed into 62 bits.
 * <p>
 * Uses java's 64 bit long format (which is IEEE754 binary64) except that 2 bits are taken
 * from the exponent. This keeps the precision but reduces the range.
 * <p>
 * <b>
 * <a href="https://en.wikipedia.org/wiki/Double-precision_floating-point_format">IEEE 754 binary64</a>
 * </b>
 *
 * <pre>
 * bit  63    : sign bit
 * bits 52-62 : exponent, 11 bits, the power of 2, bias -1023.
 * bits 0-51  : mantissa (significand) 52 bits (the leading one is not stored).
 *
 * Exponents are 11 bits, with values -1022 to +1023 held as 1 to 2046 (11 bits, bias -1023)
 * Exponents 0x000 and 0x7ff have a special meaning:
 *    0x000 is signed zero.
 *    0x7FF is +/- infinity when the mantissa is zero
 *    0x7FF is NaN if the the mantissa is not zero
 * The canonical NaN is 0x7FF8000000000000L, i.e. mantissa 0x8000...
 * The different NaN values.
 * </pre>
 *
 * The different NaN bit patterns are not distinguishable in Java
 * by floating point operations, only by {@link Double#doubleToRawLongBits}.
 *
 * The maximum value is 1.797693e+308 = (2-2^-52)*2^1023 and smallest denormalized of
 * (1-2^-52)*2^-1022 = 2.225...e-308.
 * <p>
 * <b>DoubleNode62</b>
 * <p>
 * In a 62 bit double:
 * <pre>
 * <i>NodeId</i>
 * bit 63    : pointer or value bit.
 * bit 62    : double type bit
 *
 * <i>Double62</i>
 * bit  61    : sign bit
 * bits 52-60 : exponent, 9 bits, the power of 2, bias -255
 * bits 0-51  : mantissa (significand) 52 bits (the leading one is not stored).
 *
 * Exponents are 9 bits, with values -254 to 255, held as 1 to 512 (9 bits, bias -255)
 * Exponents 0x000 and 0x1ff have a special meaning:
 *    0x000 is signed zero.
 *    0x1FF is +/- infinity if the mantissa is zero
 *    0x1FF is NaN if the the mantissa is not zero
 * The canonical NaN is 0x1FF8000000000000L, i.e. mantissa 0x8000...
 * </pre>
 *
 * for a maximum value of (2-2^-52)*2^255 = 1.157921e+77 and smallest denormlized of
 * (1-2^-52)*2^-254 = 3.4544674e-77
 * <p>
 * 0 is not a legal encoding because the high bit is the pointer/value bit.
 * <p>
 * "No encoding" is 0xFF00_0000_0000_0000L which would otherwise be the smallest (most negative) denormalized value:
 *  -3.5336941295567687E72
 * <p>All unencodeable numbers will end up in the node table in full lexical form.
 */
public class DoubleNode62 {
    /**
     * An encoded value that is not possible.
     * (it has high bits set).
     */
    public static final long NO_ENCODING = 0xFF00_0000_0000_0000L;

    /** Encode as a 62 bit long.
     * Return {@link #NO_ENCODING} for "not possible".
     * The top two bits are zero if packing was possible.
     */
    public static long pack(double v) {
        // Not "raw" , so NaNs end up as the same bit pattern when packed.
        long x = Double.doubleToLongBits(v);
        long sign = BitsLong.unpack(x, 63, 64);
        long exp11 = BitsLong.unpack(x, 52, 63);
        long exp9 = encode11to9(exp11);
        if ( exp9 == -1 )
            return NO_ENCODING;
        long significand = BitsLong.unpack(x, 0, 52);
        // Do not set the value bit or double bit.
        // This is done in NodeId.toBytes and NodeId.toByteBuffer.
        long z = 0;
        z = BitsLong.pack(z, sign, 61, 62);
        z = BitsLong.pack(z, exp9, 52, 61);
        z = BitsLong.pack(z, significand, 0, 52);
        return z;
    }

    public static long insertType(long x) {
        // bits 63 1 (pointer/value), bit 62 1(double)
        return x | 0x3L<<62;
    }

    public static long removeType(long x) {
        // bits 63 1 (pointer/value), bit 62 1(double)
        return x & ~(0x3L<<62);
    }

    public static double unpack(long x) {
        if ( x == NO_ENCODING )
            throw new IllegalArgumentException("No encoding inline");
        long sign = BitsLong.unpack(x, 61, 62);
        long exp9 = BitsLong.unpack(x, 52, 61);
        long significand = BitsLong.unpack(x, 0, 52);
        long exp11 = decode9to11(exp9);
        long z = 0;
        z = BitsLong.pack(z, sign, 63, 64);
        z = BitsLong.pack(z, exp11, 52, 63);
        z = BitsLong.pack(z, significand, 0, 52);
        double d = Double.longBitsToDouble(z);
        return d;
    }

    // Exponent: returns -1 for out of inlien range.
    private static long encode11to9(long exp11) {
        if ( exp11 == 0 )
            return 0L;
        if ( exp11 == 0x7FF )
            return 0x3FFL;

        // Remove bias.
        long expRebase = exp11 - 1023;
        if ( expRebase < -254 || expRebase > 255 )
            // Out of range.
            return -1;
        long exp9 = expRebase + 255;
        return exp9;
    }

    // Exponent
    private static long decode9to11(long exp9) {
        if ( exp9 == 0 )
            return 0L;
        else if ( exp9 == 0x1FF )
            return 0x7FFL;
        long expRebase = exp9 - 255;
        long exp11 = expRebase + 1023;
        return exp11;

    }

    // ---- Constants without type bits (so bits 62 and 63 are zero).
    /**
     * 0x1ff0000000000000L
     * @see Double#POSITIVE_INFINITY
     */
    public static final long POSITIVE_INFINITY_BITS = pack(Double.POSITIVE_INFINITY);

    /**
     * @see Double#POSITIVE_INFINITY
     */
    public static final double POSITIVE_INFINITY = unpack(POSITIVE_INFINITY_BITS);

    /**
     * 0x3ff0000000000000L
     * @see Double#NEGATIVE_INFINITY
     */
    public static final long NEGATIVE_INFINITY_BITS = pack(Double.NEGATIVE_INFINITY);

    /**
     * @see Double#NEGATIVE_INFINITY
     */
    public static final double NEGATIVE_INFINITY = unpack(NEGATIVE_INFINITY_BITS);

    /**
     * 0x1ff8000000000000L
     * @see Double#NaN
     */
    public static final long NaN_BITS = pack(Double.NaN);

    /**
     * @see Double#NaN
     */
    public static final double NaN = unpack(NaN_BITS);

    /**
     * 0x3fefffffffffffffL
     * <br/>
     * (2-2<sup>-52</sup>)&middot;2<sup>255</sup>.
     *
     * @see Double#MAX_VALUE
     */
    public static final long MAX_VALUE_BITS = 0x3fefffffffffffffL;

    /**
     * 0x3fefffffffffffffL
     * <br/>
     * (2-2<sup>-52</sup>)&middot;2<sup>255</sup>.
     *
     * @see Double#MAX_VALUE
     */
    public static final double MAX_VALUE = unpack(MAX_VALUE_BITS);

    /**
     * 0x0010000000000000L
     * @see Double#MIN_NORMAL
     */
    public static final long MIN_NORMAL_BITS = 0x0010000000000000L;

    /**
     * @see Double#MIN_NORMAL
     */
    public static final double MIN_NORMAL = unpack(0x0010000000000000L);

    /**
     * 0x01L
     * @see Double#MIN_VALUE
     */
    public static final long MIN_VALUE_BITS = 0x01L;

    /**
     * 0x01L
     * @see Double#MIN_VALUE
     */
    public static final double MIN_VALUE = unpack(MIN_VALUE_BITS);

    /**
     * @see Double#MAX_EXPONENT
     */
    public static final int MAX_EXPONENT = 255;

    /**
     * @see Double#MIN_EXPONENT
     */
    public static final int MIN_EXPONENT = -254;

    /**
     * @see Double#SIZE
     */
    public static final int SIZE = 62;
}
