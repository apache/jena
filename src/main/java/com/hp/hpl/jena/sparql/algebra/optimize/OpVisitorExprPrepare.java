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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.OpVisitorBase ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.util.Context ;

public class OpVisitorExprPrepare extends OpVisitorBase
{
    final private Context context ;

    public OpVisitorExprPrepare(Context context)
    { this.context = context ; }
    
    @Override
    public void visit(OpFilter opFilter)
    {
        opFilter.getExprs().prepareExprs(context) ;
    }
    
    // Assignment
    // ProcEval
    
    @Override
    public void visit(OpLeftJoin opLeftJoin)
    {
        if ( opLeftJoin.getExprs() != null )
            opLeftJoin.getExprs().prepareExprs(context) ;
    }
}
