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

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpTable extends Op0
{
    public static OpTable unit()
    { return new OpTable(TableFactory.createUnit()) ; }

    public static OpTable create(Table table)
    // Check for Unit-ness?
    { return new OpTable(table) ; }

    public static OpTable empty()
    // Check for Unit-ness?
    { return new OpTable(TableFactory.createEmpty()) ; }
    
    private Table table ;
    
    private OpTable(Table table) { this.table = table ; }
    
    public boolean isJoinIdentity()
    { return TableUnit.isTableUnit(table) ; }
    
    public Table getTable()
    { return table ; }
    
    @Override
    public String getName() { return Tags.tagTable ; }
    
    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public Op apply(Transform transform)
    { return transform.transform(this) ; }

    @Override
    public Op0 copy()
    { return new OpTable(table) ; }

    @Override
    public int hashCode()
    { return table.hashCode() ; } 

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpTable) ) return false ;
        OpTable opTable = (OpTable)other ;
        return table.equals(opTable.table) ;
    }
    

}
