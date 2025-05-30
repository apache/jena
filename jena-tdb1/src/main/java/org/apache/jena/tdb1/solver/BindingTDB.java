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

package org.apache.jena.tdb1.solver;

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBase ;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.tdb1.TDB1Exception;
import org.apache.jena.tdb1.lib.DumpOps;
import org.apache.jena.tdb1.store.NodeId;
import org.apache.jena.tdb1.store.nodetable.NodeTable;

/** Bind that delays turning a NodeId into a Node until explicitly needed by get() */

public class BindingTDB extends BindingBase
{
    private final NodeTable nodeTable ;
    private final BindingNodeId idBinding ;

    private static final boolean caching = false ;
    // Whether the cache is worthwhile is unclear - the NodeTable keeps a cache.
    private final Map<Var,Node> cache = ( caching ? new HashMap<>() : null ) ;

    public BindingTDB(BindingNodeId idBinding, NodeTable nodeTable)
    {
        // BindingNodeId contains the bindings actually used  copied down when created.
        super(idBinding.getParentBinding()) ;
        this.idBinding = idBinding ;
        this.nodeTable = nodeTable ;
    }

    @Override
    protected int size1() { return idBinding.size(); }

    private List<Var> vars = null ;

    /** Iterate over all the names of variables. */
    @Override
    protected Iterator<Var> vars1()
    {
        if ( vars == null )
            vars = calcVars() ;
        return vars.iterator() ;
    }

    private List<Var> calcVars()
    {
        List<Var> vars = new ArrayList<>(4) ;
        // Only if not in parent.
        // A (var/value) binding may have been copied down to record it's NodeId.

        Binding b = idBinding.getParentBinding() ;

        Iterator<Var> iter = idBinding.iterator() ;
        for ( Var v : idBinding )
        {
            if ( b == null || ! b.contains(v) )
                vars.add(v) ;
        }
        return vars ;
    }

    @Override
    protected boolean isEmpty1()
    {
        return size1() == 0 ;
    }

    @Override
    public boolean contains1(Var var)
    {
        return idBinding.containsKey(var) ;
    }

    public BindingNodeId getBindingId() { return idBinding ; }

    public NodeId getNodeId(Var var)
    {
        NodeId id = idBinding.get(var) ;
        if ( id != null )
            return id ;
        // In case we are inserting known missing nodes.
        if ( NodeId.isDoesNotExist(id) )
            return null ;

        if ( parent == null )
            return null ;
        if ( parent instanceof BindingTDB bindingTDB)
            return bindingTDB.getNodeId(var) ;
        return null ;
    }

    @Override
    public Node get1(Var var)
    {
        try {
            Node n = cacheGet(var) ;
            if ( n != null )
                return n ;

            NodeId id = idBinding.get(var) ;
            if ( id == null )
                return null ;
            if ( NodeId.isDoesNotExist(id) )
                return null;
            n = nodeTable.getNodeForNodeId(id) ;
            if ( n == null ) {

                n = nodeTable.getNodeForNodeId(id) ;

                DumpOps.dumpNodeTable(nodeTable);

                // But there was to put it in the BindingNodeId.
                throw new TDB1Exception("No node in NodeTable for NodeId "+id);
            }
            // Update cache.
            cachePut(var, n) ;
            return n ;
        } catch (Exception ex)
        {
            Log.error(this, String.format("get1(%s)", var), ex) ;
            return null ;
        }
    }

    private void cachePut(Var var, Node n)
    {
        if ( cache != null ) cache.put(var, n) ;
    }

    private Node cacheGet(Var var)
    {
        if ( cache == null ) return null ;
        return cache.get(var) ;
    }

    @Override
    protected void fmtVar(StringBuilder sbuff, Var var)
    {
        NodeId id = idBinding.get(var) ;
        String extra = "" ;
        if ( id != null )
            extra = "/"+id ;
        Node node = get(var) ;
        String tmp = NodeFmtLib.displayStr(node) ;
        sbuff.append("( ?"+var.getVarName()+extra+" = "+tmp+" )") ;
    }

    @Override
    public Binding detach() {
        return BindingFactory.copy(this);
    }

    @Override
    protected Binding detachWithNewParent(Binding newParent) {
        throw new UnsupportedOperationException("Should never be called.");
    }
}
