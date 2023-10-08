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

package org.apache.jena.tdb1.transaction ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Var ;

public class TransTestLib {

    public static int count(String queryStr, DatasetGraph dsg) {
        int counter = 0 ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        try (QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(dsg))) {
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
        try (QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.wrap(dsg))) {
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
