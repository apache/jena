/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.algebra.ExtBuilder;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpExtRegistry;
import com.hp.hpl.jena.sparql.core.DataSourceGraph;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpFetch extends OpExt
{
    private static final String TagFetch = "fetch" ; 
    // ----------------
    private static boolean enabled = false ;
    public static void enable()
    {
        if ( enabled ) return ;
        enabled = true ;
        
        OpExtRegistry.register(new ExtBuilder() {
            public OpExt make(ItemList argList) { return new OpFetch(argList.get(0).getNode()) ; }
            public String getTagName()           { return TagFetch ; }
        }) ;
    }
    // ----------------
    
    private Node node ;

    public OpFetch(Node node)
    {
        super(TagFetch) ;
        this.node = node ;
    }

    @Override
    public Op effectiveOp()
    {
        return OpTable.unit() ;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
    {
        return new QueryIterFetch(input, execCxt) ;
    }

    class QueryIterFetch extends QueryIterRepeatApply
    {

        public QueryIterFetch(QueryIterator input, ExecutionContext context)
        {
            super(input, context) ;
        }

        @Override
        protected QueryIterator nextStage(Binding binding)
        {
            DataSourceGraph ds = (DataSourceGraph)super.getExecContext().getDataset() ;
            Node n = Substitute.substitute(node, binding) ;
            String uri = n.getURI();
            if ( ds.containsGraph(n) )
                return IterLib.result(binding, getExecContext()) ;
            // DO NOT LOOK AT THIS CODE
            Model m = FileManager.get().loadModel(uri) ;
            Graph g = m.getGraph() ;
            ds.addGraph(n, g) ;
            return IterLib.result(binding, getExecContext()) ;
        }

    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpFetch) ) return false ;
        return node.equals(((OpFetch)other).node) ;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(FmtUtils.stringForNode(node, sCxt)) ;
    }

    @Override
    public int hashCode()
    {
        return TagFetch.hashCode() ^ node.hashCode() ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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