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

package arq.examples.bgpmatching;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.engine.main.StageGenerator ;
import org.apache.jena.sparql.util.QueryExecUtils ;

/** Example to execute a query but handle the
 *  basic graph patterns in the query in some special way.
 *  Stages are one step in executing a basic graph pattern (BGP).
 *  A StageGenerator builds a StageList and the stage list
 *  is executes with the output (a QueryIterator) of the previous
 *  stage fed int the current stage. 
 */  

public class StageAltMain
{
    static String NS = "http://example/" ;

    public static void main(String[] argv)
    {
        String[] queryString = 
        {
            "PREFIX ns: <"+NS+">" ,
            "SELECT ?v ",
            "{ ?s ns:p1 'xyz' ;",
            "     ns:p2 ?v }"
        } ;

        // The stage generator to be used for a query execution 
        // is read from the context.  There is a global context, which
        // is cloned when a query execution object (query engine) is
        // created.
        
        // Normally, StageGenerators are chained - a new one inspects the
        // execution request and sees if it handles it.  If it does not,
        // it sends the request to the stage generator that was already registered. 
        
        // The normal stage generator is registerd in the global context.
        // This can be replaced, so that every query execution uses the
        // alternative stage generator, or the cloned context can be
        // alter so that just one query execution is affected.

        // Change the stage generator for all queries ...
        if ( false )
        {
            StageGenerator origStageGen = (StageGenerator)ARQ.getContext().get(ARQ.stageGenerator) ;
            StageGenerator stageGenAlt = new StageGeneratorAlt(origStageGen) ;
            ARQ.getContext().set(ARQ.stageGenerator, stageGenAlt) ;
        }
        
        Query query = QueryFactory.create( StrUtils.strjoin("\n", queryString)) ;
        QueryExecution engine = QueryExecutionFactory.create(query, makeData()) ;
        
        // ... or set on a per-execution basis.
        if ( true )
        {
            StageGenerator origStageGen = (StageGenerator)engine.getContext().get(ARQ.stageGenerator) ;
            StageGenerator stageGenAlt = new StageGeneratorAlt(origStageGen) ;
            engine.getContext().set(ARQ.stageGenerator, stageGenAlt) ;
        }
        
        QueryExecUtils.executeQuery(query, engine) ;
    }
    
    private static Model makeData()
    {
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource(NS+"r") ;
        Property p1 = model.createProperty(NS+"p1") ;
        Property p2 = model.createProperty(NS+"p2") ;
        model.add(r, p1, "xyz") ;
        model.add(r, p2, "abc") ;
        return model ;
    }
}
