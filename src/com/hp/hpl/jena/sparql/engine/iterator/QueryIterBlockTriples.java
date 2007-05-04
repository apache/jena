/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

public class QueryIterBlockTriples extends QueryIter1
{
    private BasicPattern pattern ;
    private Graph graph ;
    private QueryIterator output ;
    
    public static QueryIterator create(QueryIterator input,
                                       BasicPattern pattern , 
                                       ExecutionContext execContext)
    {
        return new QueryIterBlockTriples(input, pattern, execContext) ;
    }
    
    private QueryIterBlockTriples(QueryIterator input,
                                    BasicPattern pattern , 
                                    ExecutionContext execContext)
    {
        super(input, execContext) ;
        this.pattern = pattern ;
        graph = execContext.getActiveGraph() ;
        // Create a chain of triple iterators.
        // This code is elsewhere (Group? build serial?)
        QueryIterator chain = getInput() ;
        for ( Iterator iter = pattern.iterator(); iter.hasNext(); )
        {
            Triple t = (Triple) iter.next() ;
            chain = new QueryIterTriplePattern(chain, t, execContext) ;
        }
        output = chain ;
    }

    //@Override
    protected boolean hasNextBinding()
    {
        return output.hasNext() ;
    }

    //@Override
    protected Binding moveToNextBinding()
    {
        return output.nextBinding() ;
    }

    protected void releaseResources()
    {
        output.close() ;
        output = null ;
    }

    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(Utils.className(this)) ;
        out.println() ;
        out.incIndent() ;
        FmtUtils.formatPattern(out, pattern, sCxt) ;
        out.decIndent() ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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