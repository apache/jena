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

package com.hp.hpl.jena.sparql.core.describe;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.util.Closure ;
import com.hp.hpl.jena.sparql.util.Context ;

/** DescribeHandler that calculates the bNode closure.
 *  Takes all the statements of this resource, and for every object that is
 *  a bNode, it recursively includes its statements. */

public class DescribeBNodeClosure implements DescribeHandler
{
    Model acc ;
    Dataset dataset ;
    
    public DescribeBNodeClosure() {}
    
    @Override
    public void start(Model accumulateResultModel, Context cxt)
    {
        acc = accumulateResultModel ;
        this.dataset = (Dataset)cxt.get(ARQConstants.sysCurrentDataset) ;
    }

    // DISTINCT - we only need each ?g once. 
    private static Query query = QueryFactory.create("SELECT DISTINCT ?g { GRAPH ?g { ?s ?p ?o } }") ; 
    
    // Check all named graphs
    @Override
    public void describe(Resource r)
    {
        // Default model.
        Closure.closure(otherModel(r, dataset.getDefaultModel()), false, acc) ;

        // Find all the named graphs in which this resource
        // occurs as a subject.  Faster than iterating in the
        // names of graphs in the case of very large numbers
        // of graphs, few of which contain the resource, in
        // some kind of persistent storage.
        
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        qsm.add("s", r) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset, qsm) ;
        ResultSet rs = qExec.execSelect() ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution qs = rs.next() ;
            String gName = qs.getResource("g").getURI() ;
            Model model =  dataset.getNamedModel(gName) ;
            Resource r2 = otherModel(r, model) ;
            Closure.closure(r2, false, acc) ;
        }
        
//        // Named graphs
//        for ( Iterator<String> iter = dataset.listNames() ; iter.hasNext() ; )
//        {
//            String name = iter.next();
//            Model model =  dataset.getNamedModel(name) ;
//            Resource r2 = otherModel(r, model) ;
//            Closure.closure(r2, false, acc) ;
//        }
        
        Closure.closure(r, false, acc) ;
    }

    private static Resource otherModel(Resource r, Model model)
    {
        if ( r.isURIResource() )
            return model.createResource(r.getURI()) ;
        if ( r.isAnon() )
            return model.createResource(r.getId()) ;
        // Literals do not need converting.
        return r ;
    }

    @Override
    public void finish()
    { }
}
