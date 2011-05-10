/*
 *  (c) Copyright 2011 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;

import java.math.BigDecimal ;
import java.math.BigInteger ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

public class CastXSD_Boolean extends CastXSD
{
    public CastXSD_Boolean(XSDDatatype dt)
    {
        super(dt) ;
    }
    
    @Override
    public Function create(String uri)
    {        
        return new Instance(castType) ;
    }


    protected static class Instance extends CastXSD.Instance 
    {
        Instance(XSDDatatype dt)
        {
            super(dt) ;
        }

        @Override
        protected NodeValue cast(String s, NodeValue nv, XSDDatatype castType)
        {
            if ( nv.isNumber() )
            {
                if ( nv.isFloat() || nv.isDouble() )
                {
                    // 0, +0, -0, 0.0, 0.0E0 or NaN, then TV is false.
                    // else true.
                    double d = ( nv.isDouble() ? nv.getDouble() : nv.getFloat() ) ;
                    if ( d == 0.0e0 || Double.isNaN(d) )
                        return NodeValue.FALSE ;
                    return NodeValue.TRUE ;
                }
                if ( nv.isDecimal() ) 
                {
                    if ( nv.getDecimal().compareTo(BigDecimal.ZERO) == 0 )
                        return NodeValue.FALSE ;
                    return NodeValue.TRUE ;
                }
                
                if ( nv.isInteger() )
                {
                    if ( nv.getInteger().compareTo(BigInteger.ZERO) == 0 )
                        return NodeValue.FALSE ;
                    return NodeValue.TRUE ;
                }
            }
            
            if ( nv.isBoolean() )
                return nv ;
            return super.cast(s, nv, castType) ;
        }
    }
}
/*
 *  (c) Copyright 2011 Epimorphics Ltd.
 *  All rights reserved.
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
