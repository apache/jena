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

package dev;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.riot.RDFDataMgr ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class DevFuseki
{
	// SOH default to not needing 'default'
	// More error handling.

    // Migrate ContentType to RIOT
    // use in WebContent.
    
    //soh : --accept application/turtle for CONSTRUCT queries.
    
    // ?? Slug header:
    // http://bitworking.org/projects/atom/rfc5023.html#rfc.section.9.7
    
    // ETags.
    
    // Authentication
    
    // Content-Length: SHOULD
    //   Transfer-Encoding: identity
    // "chunked" encoding
    // gzip
    
    private static Logger log = LoggerFactory.getLogger("Devel") ;
    
    public static void main(String ... argv) {
        Model m = read("config2.ttl") ;
        // One rdf:type fuseki:Service
        
        ResultSet rs = query("SELECT * { ?service rdf:type fuseki:Service }", m) ; 
        List<Resource> services = new ArrayList<Resource>() ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution soln = rs.next() ;
            Resource svc = soln.getResource("service") ;
            services.add(svc) ;
        }
        
        if ( services.size() == 0 ) {
            log.error("No services found") ;
            throw new FusekiConfigException() ;
        }
        if ( services.size() > 1 ) {
            log.error("Multiple services found") ;
            throw new FusekiConfigException() ;
        }

        Resource service = services.get(0) ;
        DatasetRef sd = FusekiConfig.processService(service) ;
        sd.init() ;
        System.out.println("DONE");
    }

    private static Model read(String filename) {

        Model m = RDFDataMgr.loadModel(filename) ;
        String x1 = StrUtils.strjoinNL
            ( "PREFIX tdb: <http://jena.hpl.hp.com/2008/tdb#>" ,
              "PREFIX ja:  <http://jena.hpl.hp.com/2005/11/Assembler#>", 
              "INSERT                    { [] ja:loadClass 'com.hp.hpl.jena.tdb.TDB' }",
              "WHERE { FILTER NOT EXISTS { [] ja:loadClass 'com.hp.hpl.jena.tdb.TDB' } }"
             ) ;
        String x2 = StrUtils.strjoinNL
            ("PREFIX tdb: <http://jena.hpl.hp.com/2008/tdb#>" ,
             "PREFIX ja:  <http://jena.hpl.hp.com/2005/11/Assembler#>",
             "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>",
             "INSERT DATA {",
             "   tdb:DatasetTDB  rdfs:subClassOf  ja:RDFDataset .",
             "   tdb:GraphTDB    rdfs:subClassOf  ja:Model .",
             "}" 
             ) ;
        execute(m, x1) ;
        execute(m, x2) ;
        return m ;
        
    }

    private static void execute(Model m, String x) {
        UpdateRequest req = UpdateFactory.create(x) ;
        UpdateAction.execute(req, m);
    }
    
    // Copies from original FusekiConfig
    
    private static String prefixes = StrUtils.strjoinNL(
    "PREFIX fuseki: <http://jena.apache.org/fuseki#>" ,
    "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
    "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
    "PREFIX tdb:    <http://jena.hpl.hp.com/2008/tdb#>",
    "PREFIX list:   <http://jena.hpl.hp.com/ARQ/list#>",
    "PREFIX list:   <http://jena.hpl.hp.com/ARQ/list#>",
    "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>",
    "PREFIX apf:     <http://jena.hpl.hp.com/ARQ/property#>", 
    "PREFIX afn:     <http://jena.hpl.hp.com/ARQ/function#>" ,
    "") ;
    
    private static ResultSet query(String string, Model m)
    {
        return query(string, m, null, null) ;
    }

    private static ResultSet query(String string, Model m, String varName, RDFNode value)
    {
        Query query = QueryFactory.create(prefixes+string) ;
        QuerySolutionMap initValues = null ;
        if ( varName != null )
            initValues = querySolution(varName, value) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, m, initValues) ;
        ResultSet rs = ResultSetFactory.copyResults(qExec.execSelect()) ;
        qExec.close() ;
        return rs ;
    }
    
    private static QuerySolutionMap querySolution(String varName, RDFNode value)
    {
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        querySolution(qsm, varName, value) ;
        return qsm ;
    }
    
    private static QuerySolutionMap querySolution(QuerySolutionMap qsm, String varName, RDFNode value)
    {
        qsm.add(varName, value) ;
        return qsm ;
    }
}
