/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.bgpmatching;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.GraphBase;

import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterTriplePattern;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;

/** Example stage generator that compiles a BasicPattern into a sequence of
 *  individual triple matching steps.
 */   

public class StageGeneratorAlt implements StageGenerator
{
    StageGenerator other ;
    
    public StageGeneratorAlt(StageGenerator other)
    {
        this.other = other ;
    }
    
    
    public QueryIterator execute(BasicPattern pattern, 
                                 QueryIterator input,
                                 ExecutionContext execCxt)
    {
        // Just want to pick out some BGPs (e.g. on a particualr graph)
        // Test ::  execCxt.getActiveGraph() 
        if ( ! ( execCxt.getActiveGraph() instanceof GraphBase ) )
            // Example: pass on up to the original StageGenerator if
            // not based on GraphBase (which most Graph implementations are). 
            return other.execute(pattern, input, execCxt) ;
        
        System.err.println("MyStageGenerator.compile:: triple patterns = "+pattern.size()) ;

        // Stream the triple matches together, one triple matcher at a time. 
        QueryIterator qIter = input ;
        for ( Iterator iter = pattern.getList().iterator() ; iter.hasNext() ; )
        {
            Triple triple = (Triple)iter.next();
            qIter = new QueryIterTriplePattern(qIter, triple, execCxt) ;
        }
        return qIter ;
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