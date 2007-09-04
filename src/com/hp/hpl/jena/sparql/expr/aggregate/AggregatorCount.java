/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class AggregatorCount implements AggregateFactory
{
    private static AggregatorCount singleton = new AggregatorCount() ;
    public static AggregateFactory get() { return singleton ; }
    
    private AggregatorCount() {} 
    
    public Aggregator create()
    {
        return new AggCountWorker() ;
    }
    
    public String toString() { return "count(*)" ; }
}

// There must be one too many level of indirection and class here.
// -- AggregatorCount global factory for engines.
// -- AggCountWorker per query (strictly SELECT level) factory/engine.
// -- AccCount worker - one per group element.
// ==> all needed in some form.

class AggCountWorker extends AggregatorBase
{
    static boolean distinct = false ;
    
    public AggCountWorker()
    {
        super() ;
    }

    public int hashCode() { return "(count *)".hashCode() ; }
    public boolean equals(Object other)
    {
        return other instanceof AggCountWorker ;
    }
    
    
    public String toString() { return "count(*)" ; }
    public String toPrefixString() { return "(count *)" ; }
    
    protected Accumulator createAccumulator()
    { 
        if ( distinct )
            return new AccCountDistinct() ; 
        else
            return new AccCount() ;
    }
    
    // ---- COUNT(*)
    public static class AccCount implements Accumulator
    {
        private long count = 0 ;
        public AccCount()   { }
        public void accumulate(Binding binding) { count++ ; }
        public NodeValue getValue()             { return NodeValue.makeInteger(count) ; }
    }
    
    // ---- COUNT(DISTINCT *)
    public static class AccCountDistinct implements Accumulator
    {
        private Set rows = new HashSet() ;
        public AccCountDistinct()               { } 
        // The group key part of binding will be the same for all elements of the group.
        public void accumulate(Binding binding) { rows.add(binding) ; }
        public NodeValue getValue()             { return NodeValue.makeInteger(rows.size()) ; }
    }

    // ---- COUNT(?var)
    // ---- COUNT( DISTINCT ?var)

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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