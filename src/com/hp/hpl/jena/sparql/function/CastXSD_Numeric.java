/*
 *  (c) Copyright 2011 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;

public class CastXSD_Numeric extends CastXSD
{
    public CastXSD_Numeric(XSDDatatype dt)
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
            if ( nv.isBoolean() )
            {
                boolean b = nv.getBoolean() ;
                if ( XSDDatatype.XSDfloat.equals(castType) || XSDDatatype.XSDdouble.equals(castType) )
                    s = ( b ? "1.0E0" : "0.0E0" ) ;
                else if ( XSDDatatype.XSDdecimal.equals(castType) )
                    s = ( b ? "1.0" : "0.0" ) ;
                else if ( XSDFuncOp.isIntegerType(castType)) 
                    s = ( b ? "1" : "0" ) ;
                // else do nothing and hope. 
            }
            
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
