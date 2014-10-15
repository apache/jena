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

package com.hp.hpl.jena.tdb.transaction ;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Var ;

public class TransTestLib {

    public static int count(String queryStr, DatasetGraph dsg) {
        int counter = 0 ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        try (QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg))) {
            ResultSet rs = qExec.execSelect() ;
            for ( ; rs.hasNext() ; ) {
                rs.nextBinding() ;
                counter++ ;
            }
            return counter ;
        }
    }

    // To QueryExecUtils?
    public static List<Node> query(String queryStr, String var, DatasetGraphTxn dsg) {
        Var v = Var.alloc(var) ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        try (QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg))) {
            List<Node> nodes = new ArrayList<>() ;
            ResultSet rs = qExec.execSelect() ;
            for ( ; rs.hasNext() ; ) {
                Node n = rs.nextBinding().get(v) ;
                nodes.add(n) ;
            }
            return nodes ;
        }
    }
}
