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

package org.apache.jena.sdb.layout2.hash;

import org.apache.jena.graph.Node ;
import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.core.sqlexpr.* ;
import org.apache.jena.sdb.layout2.NodeLayout2 ;
import org.apache.jena.sdb.layout2.SlotCompiler2 ;
import org.apache.jena.sparql.util.FmtUtils ;

public class SlotCompilerHash extends SlotCompiler2
{
    public SlotCompilerHash(SDBRequest request)
    { 
        super(request) ;
    }

    @Override
    protected void constantSlot(SDBRequest request, Node node, SqlColumn thisCol, SqlExprList conditions)
    {
        long hash = NodeLayout2.hash(node) ;
        SqlExpr c = new S_Equal(thisCol, new SqlConstant(hash)) ;
        String x = FmtUtils.stringForNode(node, request.getPrefixMapping()) ;
        c.addNote("Const: "+x) ;
        conditions.add(c) ;
        return ;
    }
}
