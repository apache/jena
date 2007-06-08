/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;

public class BuilderExec
{
    static public void main(String[] argv)
    {
        Item item = SSE.readFile("SSE/all.sse") ;
        exec(item) ;
    }
    
    static protected final String symEval        = "exec" ;
    
    static public void exec(Item item)
    {
        if (item.isNode() )
            BuilderBase.broken(item, "Attempt to build evaluation from a plain node") ;

        if (item.isWord() )
            BuilderBase.broken(item, "Attempt to build evaluation from a bare word") ;

        if ( ! item.isTagged(symEval) )
            throw new BuildException("Wanted ("+symEval+"...) : got: "+BuilderBase.shortPrint(item));

        ItemList list = item.getList() ;  // Loose the tag.
        if ( list.size() != 3 )
            throw new BuildException(BuilderBase.shortPrint(item)+ " does have 2 components");
        
        DatasetGraph dsg = BuilderGraph.buildDataset(list.get(1)) ;
//        Graph graph = BuilderGraph.buildGraph(list.get(1)) ;
        Op op = BuilderOp.build(list.get(2)) ;
//        DatasetGraph dsg = new DataSourceGraphImpl(graph) ;
        QueryExecUtils.executeAlgebra(op, dsg, ResultsFormat.FMT_TEXT) ;
    }
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