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

package org.apache.jena.sparql.syntax.syntaxtransform;

import static org.junit.Assert.assertEquals;

import org.junit.Test ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps ;

public class TestQueryOps
{
    @Test public void queryOp_01() { testShallowCopy("SELECT * { }") ; }
    
    @Test public void queryOp_02() { testShallowCopy("SELECT ?x { }") ; }
    
    @Test public void queryOp_03() { testShallowCopy("SELECT * { ?s ?p ?o }") ; }

    @Test public void queryOp_04() { testShallowCopy("SELECT ?x { ?s ?p ?o }") ; }

    @Test public void queryOp_05() { testShallowCopy("SELECT (?x+1 AS ?z) ?y { }") ; }
    
    @Test public void queryOp_06() { testShallowCopy("SELECT DISTINCT (?x+1 AS ?z) ?y { }") ; }

    @Test public void queryOp_07() { testShallowCopy("SELECT REDUCED (?x+1 AS ?z) ?y { }") ; }

    @Test public void queryOp_10() { testShallowCopy("SELECT ?s { ?s ?p ?o } GROUP BY ?s") ; }

    @Test public void queryOp_11() { testShallowCopy("SELECT ?s { ?s ?p ?o } ORDER BY ?o") ; }

    @Test public void queryOp_12() { testShallowCopy("SELECT ?s { ?s ?p ?o } LIMIT 10") ; }

    @Test public void queryOp_13() { testShallowCopy("SELECT ?s { ?s ?p ?o } OFFSET 5 LIMIT 10") ; }

    private static void testShallowCopy(String queryString)
    {
        Query q1 = QueryFactory.create(queryString) ;
        Query q2 = QueryTransformOps.shallowCopy(q1) ;
        assertEquals(q1, q2) ;
    }

}

