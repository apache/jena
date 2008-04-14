/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// NO SIGN
public class DecimalNode
{
    private static Logger log = LoggerFactory.getLogger(DecimalNode.class) ;
    
    BigDecimal decimal = null ;
    
    // 48 nibbles (12 chars) of accuracy 
    static final long MAX = 999999999999L ; 
    static final BigInteger MAX_I = BigInteger.valueOf(MAX) ;
    
    int scale ;     // 2^7 - 2^-7
    long value ;    // BCD nibbles.  16 nibbles is more than an int's worth.

    public static DecimalNode valueOf(BigDecimal decimal)
    {
        int scale = decimal.scale() ;
        BigInteger bigInt = decimal.unscaledValue() ;
        if ( bigInt.compareTo(MAX_I) > 0 )
        {
            log.warn("Value out of range: ("+decimal.scale()+","+decimal.unscaledValue()+")") ;
            return null ;
        }
        return valueOf(bigInt.longValue(), scale) ;
    }
    
    public static DecimalNode valueOf(long binValue, int scale)
    {
        if ( scale >= 128 || scale < -128 )
        {
            log.warn("Scale out of range: ("+binValue+","+scale+")") ;
            return null ;
        }
        
        if ( binValue > MAX )
        {
            log.warn("Value out of range: ("+binValue+","+scale+")") ;
            return null ;
        }
        
        return new DecimalNode(binValue, scale) ;
    }
    
    private DecimalNode(long binVal, int scale)
    {
        this.scale = scale ;
        this.value = BCD.asBCD(binVal) ;
    }
    
    public BigDecimal get()
    {
        if ( decimal == null )
            decimal = BigDecimal.valueOf(BCD.asLong(value), scale) ;
        return decimal ;
    }

    @Override
    public String toString()
    {
        return get().toPlainString() ;
    }
    
    public int getScale()
    {
        return scale ;
    }

    public long getValue()
    {
        return value ;
    }
    
    
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */