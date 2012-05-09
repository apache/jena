/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.algebra.op;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.algebra.OpExtBuilder ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpExtRegistry ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.IterLib ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.util.FileManager ;

public class OpFetch extends OpExt
{
    private static final String TagFetch = "fetch" ; 
    // ----------------
    private static boolean enabled = false ;
    public static void enable()
    {
        if ( enabled ) return ;
        enabled = true ;
        
        OpExtRegistry.register(new OpExtBuilder() {
            
            @Override
            public OpExt make(ItemList argList)
            { return new OpFetch(argList.get(0).getNode()) ; }
            
            @Override
            public String getTagName()
            { return TagFetch ; }
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
            DatasetGraph ds = super.getExecContext().getDataset() ;
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
