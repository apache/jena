/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.NodeConst;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

public class AggCountDistinct implements AggregateFactory
{
    // ---- COUNT(DISTINCT *)

    private static AggCountDistinct singleton = new AggCountDistinct() ;
    public static AggregateFactory get() { return singleton ; }

    private AggCountDistinct() {} 

    public Aggregator create()
    {
        return new AggCountDistinctWorker() ;
    }

    static class AggCountDistinctWorker extends AggregatorBase
    {
        public AggCountDistinctWorker() { super() ; }

        @Override
        public String toString()        { return "count(distinct *)" ; }
        public String toPrefixString()  { return "(count distinct)" ; }

        @Override
        protected Accumulator createAccumulator()
        { 
            return new AccCountDistinct() ; 
        }
        
        @Override
        public Node getValueEmpty()     { return NodeConst.nodeZero ; }

        public boolean equalsAsExpr(Aggregator other)
        {
            // Stateless as expression
            return ( other instanceof AggCountDistinctWorker ) ;
        } 
    }

    // ---- COUNT(DISTINCT *)
    static class AccCountDistinct implements Accumulator
    {
        private Set<Binding> rows = new HashSet<Binding>() ;
        public AccCountDistinct()               { } 
        // The group key part of binding will be the same for all elements of the group.
        public void accumulate(Binding binding, FunctionEnv functionEnv)
        { rows.add(binding) ; }
        
        public NodeValue getValue()             { return NodeValue.makeInteger(rows.size()) ; }
    }
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