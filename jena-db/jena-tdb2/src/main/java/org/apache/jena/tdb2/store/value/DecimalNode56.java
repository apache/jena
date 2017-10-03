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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.jena.atlas.lib.BitsLong ;


// Decimal packed into 56 bits.
public class DecimalNode56
{
    //private static Logger log = LoggerFactory.getLogger(DecimalNode.class) ;
    
    BigDecimal decimal = null ;
    
    // signed 8 bits of scale, signed 48 bits of value.
    // Decimal precision is 47 bits (it's signed) or around 14 places.
    // Not finance industry accuracy nor XSD (18 places minimum) but still useful.

    static final int        SCALE_LEN = 8;
    static final int        VALUE_LEN = 48;
    static final int        ENC_LEN   = 48 + SCALE_LEN;

    static final long       MAX_VALUE = (1L << (VALUE_LEN - 1)) - 1;
    static final long       MIN_VALUE = -(1L << (VALUE_LEN - 1));

    static final int        MAX_SCALE = (1 << (SCALE_LEN - 1)) - 1;
    static final int        MIN_SCALE = -(1 << (SCALE_LEN - 1));

    static final BigInteger MAX_I     = BigInteger.valueOf(MAX_VALUE);
    static final BigInteger MIN_I     = BigInteger.valueOf(MIN_VALUE);

    // Bits counts
    static private int      SCALE_LO  = 56 - SCALE_LEN;
    static private int      SCALE_HI  = 56;                               // Exclusive
                                                                          // index

    static private int      VALUE_LO  = 0;
    static private int      VALUE_HI  = VALUE_LO + VALUE_LEN;

    private int             scale;
    private long            value;

    public static DecimalNode56 valueOf(BigDecimal decimal) {
        int scale = decimal.scale();
        BigInteger bigInt = decimal.unscaledValue();
        
        //decimal.longValueExact(); // Throws exception
        //new BigDecimal(long);

        if ( bigInt.compareTo(MAX_I) > 0 || bigInt.compareTo(MIN_I) < 0 )
            // This check makes sure that bigInt.longValue() is safe
            return null;
        return valueOf(bigInt.longValue(), scale);
    }

    public static DecimalNode56 valueOf(long binValue, int scale) {
        if ( scale < MIN_SCALE || scale > MAX_SCALE ) {
            // log.warn("Scale out of range: ("+binValue+","+scale+")") ;
            return null;
        }

        if ( binValue < MIN_VALUE || binValue > MAX_VALUE ) {
            // log.warn("Value out of range: ("+binValue+","+scale+")") ;
            return null;
        }

        return new DecimalNode56(binValue, scale);
    }

    private DecimalNode56(long value, int scale) {
        this.scale = scale;
        this.value = value;
    }

    public long pack() {
        return pack(value, scale);
    }

    /** Create the long value */
    public static long pack(long value, int scale) {
        // pack : scale, value
        long v = 0;
        v = BitsLong.pack(0L, scale, SCALE_LO, SCALE_HI);
        v = BitsLong.pack(v, value, VALUE_LO, VALUE_HI);
        // No need to do something about negative numbers
        return v;
    }

    public static DecimalNode56 unpack(long v) {
        int scale = (int)BitsLong.unpack(v, SCALE_LO, SCALE_HI);
        long value = BitsLong.unpack(v, VALUE_LO, VALUE_HI);
        return new DecimalNode56(value, scale);
    }

    public static BigDecimal unpackAsBigDecimal(long v) {
        int scale = (int)BitsLong.unpack(v, SCALE_LO, SCALE_HI);
        long value = BitsLong.unpack(v, VALUE_LO, VALUE_HI);
        // Sign extend value.
        if ( BitsLong.isSet(value, VALUE_HI - 1) )
            value = value | -1L << (VALUE_HI);
        return BigDecimal.valueOf(value, scale);
    }

    public BigDecimal get() {
        if ( decimal == null )
            decimal = BigDecimal.valueOf(value, scale);
        return decimal;
    }

    @Override
    public String toString() {
        return get().toPlainString();
    }

    public int getScale() {
        return scale;
    }

    public long getValue() {
        return value;
    }
}
