/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.engine.tdb;

import java.util.Iterator ;

import org.apache.jena.riot.out.NodeFmtLib ;
import org.seaborne.jena.engine.Row ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingBase ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

final
public class BindingRow extends BindingBase {

    private final Row<NodeId> row ;
    private final NodeTable nodeTable ;

    public BindingRow(Row<NodeId> row, NodeTable nodeTable) {
        this(null, row, nodeTable) ;
    }
    
    public BindingRow(Binding parent, Row<NodeId> row, NodeTable nodeTable) {
        super(parent) ;
        this.row = row ;
        this.nodeTable = nodeTable ;
    }
    
    public Row<NodeId> getRow() { return row ; }
    
    @Override
    protected Iterator<Var> vars1() {
        return row.vars().iterator() ;
    }

    @Override
    protected int size1() {
        int x = 0;
        for ( Var v : row.vars() )
            x++ ;
        return x ;
    }

    @Override
    protected boolean isEmpty1() {
        return row.isEmpty() ;
    }

    @Override
    protected boolean contains1(Var var) {
        return row.contains(var) ;
    }

    @Override
    protected Node get1(Var var) {
        NodeId nid = row.get(var) ;
        if ( nid == null || nid == NodeId.NodeDoesNotExist )
            return null ;
        return nodeTable.getNodeForNodeId(nid) ;
    }
    
    @Override
    protected void format(StringBuffer sbuff, Var var)
    {
        NodeId id = row.get(var) ;
        String extra = "" ;
        if ( id != null )
            extra = "/"+id ;
        Node node = get(var) ;
        String tmp = NodeFmtLib.displayStr(node) ;
        sbuff.append("( ?"+var.getVarName()+extra+" = "+tmp+" )") ;
    }
}