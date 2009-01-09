/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.util.Utils;

/** Group aggregation functions calculate a value during grouping and
 *  place it in the output binding.  This class is relationship of 
 *  an aggregation expression and that variable.  Evaluation returns
 *  the variable's bound value. 
 */

public class E_Aggregator extends ExprVar
{
    protected Aggregator aggregator ;
    
    public E_Aggregator(String name, Aggregator agg)    { super(name) ; aggregator = agg ; }
    public E_Aggregator(Node n, Aggregator agg)         { super(n) ; aggregator = agg ; }
    public E_Aggregator(Var v, Aggregator agg)          { super(v) ; aggregator = agg ; }
    
    public void setVar(Var var)
    { 
        if ( super.varNode != null )
            throw new ARQInternalErrorException(Utils.className(this)+": Attempt to set variable to "+var+" when already set as "+super.varNode) ;
        super.varNode = var ;
    }
    
    public Aggregator getAggregator()   { return aggregator ; }
    
    @Override
    public int hashCode() { return super.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! super.equals(other) ) return false ;
        if ( ! ( other instanceof E_Aggregator ) )
            return false ;
        E_Aggregator agg = (E_Aggregator)other ;
        return aggregator.equalsAsExpr(agg.aggregator) ;
    }

    // As an expression suitable for outputting the calculation. 
    @Override
    public String asSparqlExpr()        
    { return aggregator.toString() ; }
    
    // DEBUGGING
    @Override
    public String toString()
    // Don't call super.toString - that will call asSparqlExpr()!
    // varNode can be null temporarily as a structure is built.
    { return "(AGG "+
                (varNode==null?"<>":"?"+super.varNode.getVarName())+
                " "+aggregator.toString()+")"; }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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