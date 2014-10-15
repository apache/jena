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

package examples;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.text.EntityDefinition ;
import org.apache.jena.query.text.TextDatasetFactory ;
import org.apache.jena.query.text.TextQuery ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.RAMDirectory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.vocabulary.RDFS ;

/** Build a text search dataset */
public class JenaTextExample1
{
    static { LogCtl.setLog4j() ; }
    static Logger log = LoggerFactory.getLogger("JenaTextExample") ;
    
    public static void main(String ... argv)
    {
        TextQuery.init();
        Dataset ds = createCode() ;
        //Dataset ds = createAssembler() ;
        loadData(ds , "data.ttl") ;
        queryData(ds) ;
    }
    
    public static Dataset createCode() 
    {
        log.info("Construct an in-memory dataset with in-memory lucene index using code") ;
        // Build a text dataset by code.
        // Here , in-memory base data and in-memeory Lucene index

        // Base data
        Dataset ds1 = DatasetFactory.createMem() ; 

        // Define the index mapping 
        EntityDefinition entDef = new EntityDefinition("uri", "text", RDFS.label.asNode()) ;

        // Lucene, in memory.
        Directory dir =  new RAMDirectory();
        
        // Join together into a dataset
        Dataset ds = TextDatasetFactory.createLucene(ds1, dir, entDef) ;
        
        return ds ;
    }

    public static Dataset createAssembler() 
    {
        log.info("Construct text dataset using an assembler description") ;
        // There are two datasets in the configuration:
        // the one for the base data and one with text index.
        // Therefore we need to name the dataset we are interested in. 
        Dataset ds = DatasetFactory.assemble("text-config.ttl", "http://localhost/jena_example/#text_dataset") ;
        return ds ;
    }
    
    public static void loadData(Dataset dataset, String file)
    {
        log.info("Start loading") ;
        long startTime = System.nanoTime() ;
        dataset.begin(ReadWrite.WRITE) ;
        try {
            Model m = dataset.getDefaultModel() ;
            RDFDataMgr.read(m, file) ;
            dataset.commit() ;
        } finally { dataset.end() ; }
        
        long finishTime = System.nanoTime() ;
        double time = (finishTime-startTime)/1.0e6 ;
        log.info(String.format("Finish loading - %.2fms", time)) ;
    }

    public static void queryData(Dataset dataset)
    {
        log.info("START") ;
        long startTime = System.nanoTime() ;
        String pre = StrUtils.strjoinNL
            ( "PREFIX : <http://example/>"
            , "PREFIX text: <http://jena.apache.org/text#>"
            , "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>") ;
        
        String qs = StrUtils.strjoinNL
            ( "SELECT * "
            , " { ?s text:query (rdfs:label 'X1') ;"
            , "      rdfs:label ?label"
            , " }") ; 
        
        dataset.begin(ReadWrite.READ) ;
        try {
            Query q = QueryFactory.create(pre+"\n"+qs) ;
            QueryExecution qexec = QueryExecutionFactory.create(q , dataset) ;
            QueryExecUtils.executeQuery(q, qexec) ;
        } finally { dataset.end() ; }
        long finishTime = System.nanoTime() ;
        double time = (finishTime-startTime)/1.0e6 ;
        log.info(String.format("FINISH - %.2fms", time)) ;

    }

}

