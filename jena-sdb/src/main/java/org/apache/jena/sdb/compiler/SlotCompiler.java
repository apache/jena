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

package org.apache.jena.sdb.compiler;

import org.apache.jena.graph.Node ;
import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.core.ScopeEntry ;
import org.apache.jena.sdb.core.sqlexpr.S_Equal ;
import org.apache.jena.sdb.core.sqlexpr.SqlColumn ;
import org.apache.jena.sdb.core.sqlexpr.SqlExpr ;
import org.apache.jena.sdb.core.sqlexpr.SqlExprList ;
import org.apache.jena.sdb.core.sqlnode.SqlNode ;
import org.apache.jena.sdb.core.sqlnode.SqlTable ;
import org.apache.jena.sparql.core.Var ;

public abstract class SlotCompiler
{
    private SDBRequest request ;
    
    public abstract SqlNode start(QuadBlock quads) ;
    public abstract SqlNode finish(SqlNode sqlNode, QuadBlock quads) ;
    
    public SlotCompiler(SDBRequest request)
    {
        this.request = request ;
    }
    
    protected SDBRequest getRequest() { return request ; }
    
    public final void processSlot(SDBRequest request,
                                  SqlTable table, SqlExprList conditions,
                                  Node node, String colName)
 {
     SqlColumn thisCol = new SqlColumn(table, colName) ;
     if ( ! node.isVariable() )
     {
         constantSlot(request, node, thisCol, conditions) ;
         return ;
     }
     
     Var var = Var.alloc(node) ;
     if ( table.getIdScope().hasColumnForVar(var) )
     {
         ScopeEntry e = table.getIdScope().findScopeForVar(var) ;
         SqlColumn otherCol = e.getColumn() ;
         SqlExpr c = new S_Equal(otherCol, thisCol) ;
         conditions.add(c) ;
         c.addNote("processVar: "+node) ;
         return ;
     }
     table.setIdColumnForVar(var, thisCol) ;
 }

    protected abstract void constantSlot(SDBRequest request, Node node, SqlColumn thisCol, SqlExprList conditions) ;
}
