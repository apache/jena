/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** The null aggregate (which can't be written in SPARQL) 
 * calculates nothering but does help remember the group key  
 */
public class AggNull extends AggregatorBase
{
    public AggNull() { } 
    public Aggregator copy(Expr expr) { return this ; }
    
    @Override
    public String toString() { return "aggnull()" ; }
    @Override
    public String toPrefixString() { return "(aggnull)" ; }

    @Override
    public Accumulator createAccumulator()
    { 
        return createAccNull() ;
    }

    @Override
    public Node getValueEmpty()     { return null ; } 

    //@Override
    public Expr getExpr()           { return null ; }
    
    @Override
    public int hashCode()   { return HC_AggNull ; }
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ; 
        return ( other instanceof AggNull ) ;
    } 

    public static Accumulator createAccNull() { return new  AccNull() ; }
    
    // ---- Accumulator
    private static class AccNull implements Accumulator
    {
        private int nBindings = 0 ;

        public AccNull() { }

        //@Override
        public void accumulate(Binding binding, FunctionEnv functionEnv)
        { nBindings++ ; }

        //@Override
        public NodeValue getValue()
        {
            return null ;
        }
    }

}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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