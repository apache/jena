/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import org.openjena.atlas.logging.Log ;

/**
 * Yield new bindings, with a fixed parent, with values from an iterator. 
 * Parent must not have variables in common with the iterator stream.
 */
public class QueryIterCommonParent extends QueryIterConvert
{
    public QueryIterCommonParent(QueryIterator input, Binding binding, ExecutionContext execCxt)
    {
        super(input, new ConverterExtend(binding) , execCxt) ;
    }

    // Extend (with checking) an iterator stream of binding to have a common parent. 
    static class ConverterExtend implements QueryIterConvert.Converter
    {
        private Binding parentBinding ;
        
        ConverterExtend(Binding parent) { parentBinding = parent ; }
        
        public Binding convert(Binding b)
        {
            if ( parentBinding == null || parentBinding.isEmpty() )
                return b ;
        
            // This is the result.  Could have BindingBase.setParent etc.  
            BindingMap b2 = BindingFactory.create(parentBinding) ;

            // Copy the resultSet bindings to the combined result binding with checking. 
            for ( Iterator<Var> iter = b.vars() ; iter.hasNext(); )
            {
                Var v = iter.next();
                Node n = b.get(v) ;
                if ( b2.contains(v) )
                {
                    Node n2 = b2.get(v) ;
                    if ( n2.equals(n) )
                        Log.warn(this, "Binding already for "+v+" (same value)" ) ;
                    else
                    {
                        Log.fatal(this, "Binding already for "+v+" (different values)" ) ;
                        throw new ARQInternalErrorException("QueryIteratorResultSet: Incompatible bindings for "+v) ;
                    }
                }
                b2.add(v, n) ;
            }
            return b2 ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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