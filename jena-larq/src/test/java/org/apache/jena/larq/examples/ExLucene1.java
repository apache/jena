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

package org.apache.jena.larq.examples;


import org.apache.jena.larq.IndexBuilderString;
import org.apache.jena.larq.IndexLARQ;
import org.apache.jena.larq.LARQ;
import org.apache.jena.atlas.lib.StrUtils;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileManager;

/** Example code to load a model from a file, index all string literals,
 * then execute a SPARQL query with a Lucene search in it. */

public class ExLucene1
{
    
    public static void main(String[] a) throws Exception
    {
        System.out.println("ARQ Example: "+Utils.classShortName(ExLucene1.class)) ;
        System.out.println("ARQ: "+ARQ.VERSION) ;
        System.out.println() ;
        
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = buildIndex(model,  "src/test/resources/LARQ/data-1.ttl") ;
        
        // Search for 
        String searchString = "+document" ;
        
        String queryString = StrUtils.strjoin("\n", 
            "PREFIX pf:     <http://jena.hpl.hp.com/ARQ/property#>",
            "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>" ,
            "PREFIX :       <http://example/>" ,
            "SELECT * {" ,
            "    ?lit pf:textMatch '"+searchString+"'.",
            "}") ;
        
        // Three documents should match. 
        performQuery(model, index, queryString) ;
        index.close() ;
    }

    static IndexLARQ buildIndex(Model model, String datafile)
    {
        // ---- Read and index all literal strings.
        IndexBuilderString larqBuilder = new IndexBuilderString() ;
        
        // Index statements as they are added to the model.
        model.register(larqBuilder) ;
        
        // To just build the index, create a model that does not store statements 
        // Model model2 = ModelFactory.createModelForGraph(new GraphSink()) ;
        
        FileManager.get().readModel(model, datafile) ;
        
        // ---- Alternatively build the index after the model has been created. 
        // larqBuilder.indexStatements(model.listStatements()) ;
        
        // ---- Finish indexing
        larqBuilder.closeWriter() ;
        model.unregister(larqBuilder) ;
        
        // ---- Create the access index  
        IndexLARQ index = larqBuilder.getIndex() ;
        return index ; 
    }

    static void performQuery(Model model, IndexLARQ index, String queryString)
    {  
        // Make globally available
        LARQ.setDefaultIndex(index) ;
        
        Query query = QueryFactory.create(queryString) ;
        query.serialize(System.out) ;
        System.out.println();
                                          
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        //LARQ.setDefaultIndex(qExec.getContext(), index) ;
        ResultSetFormatter.out(System.out, qExec.execSelect(), query) ;
        qExec.close() ;
    }

}
