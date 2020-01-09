/**
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

package org.apache.jena.sparql.engine.iterator;

import static org.junit.Assert.fail;

import java.util.List ;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.util.Context ;
import org.junit.Test;

public class TestDataBagDistinctOrder  {
    
    /** JENA-1771 - interaction of sort and DISTINCT when spilling. */ 
    @Test
    public void distinctOrderSpill_1() {
        String qs = StrUtils.strjoinNL
            ("PREFIX  :     <http://example/>"
            ,"SELECT DISTINCT *"
            ,"{ ?x  :p  ?v }"
            ,"ORDER BY ASC(?v)"
            );

        // This test data exhibits the problem.
        Query query = QueryFactory.create(qs);
        Model model = RDFDataMgr.loadModel("testing/ARQ/Extra/sort-distinct-data.ttl");
        QueryExecution qExec = QueryExecutionFactory.create(query, model);
        Context cxt = qExec.getContext();
        cxt.set(ARQ.spillToDiskThreshold, 2L);

        List<QuerySolution> x = ResultSetFormatter.toList(qExec.execSelect());
        List<Integer> z = x.stream().map(qsoln->qsoln.getLiteral("v").getInt()).collect(Collectors.toList());
        for ( int i = 0 ; i < z.size()-1; i++ ) {
            int v1 = z.get(i);
            int v2 = z.get(i+1);
            if ( v2 < v1 )
                fail("Data not sorted at ("+v1+", "+v2+") "+z);
        }
    }
}

