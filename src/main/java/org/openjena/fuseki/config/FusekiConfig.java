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

package org.openjena.fuseki.config;

import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.FusekiConfigException ;
import org.openjena.fuseki.server.SPARQLServer ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.QuerySolutionMap ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class FusekiConfig
{
    static { Log.setLog4j() ; }

    private static Logger log = Fuseki.configLog ;
    
    public static void main(String...argv)
    {
        List<ServiceDesc> services = FusekiConfig.configure("config.ttl") ;
        SPARQLServer server = new SPARQLServer(3030, services) ;
        server.start() ;
    }
    
    public static class ServiceDesc
    {
        public String name = null ;
        public List<String> queryEP = new ArrayList<String>() ;
        public List<String>  updateEP = new ArrayList<String>() ;
        public List<String>  uploadEP = new ArrayList<String>() ;
        public List<String>  readGraphStoreEP = new ArrayList<String>() ;
        public List<String>  readWriteGraphStoreEP = new ArrayList<String>() ;
        public Dataset dataset = null ;
        
    }
    
    //static Map<String, ServiceDesc>>
    
    public static List<ServiceDesc> configure(String filename)
    {
        // Basic checks
        // Stage one : parse file and set temporay datastructures
        // Stateg two : configure server, knowing the data is all valid.
        
        Model m = FileManager.get().loadModel(filename) ;
        
        ResultSet rs = query("SELECT * { ?s fu:services [ list:member ?member ] }", m) ; 
        if ( ! rs.hasNext() )
            log.warn("No services found") ;
        
        List<ServiceDesc> services =  new ArrayList<ServiceDesc>() ; 
        
        for ( ; rs.hasNext() ; )
        {
            QuerySolution soln = rs.next() ;
            Resource svc = soln.getResource("member") ;
            ServiceDesc sd = processService(svc) ;
            services.add(sd) ;
        }
        
        return services ;
    }

    private static ServiceDesc processService(Resource svc)
    {
        ServiceDesc sDesc = new ServiceDesc() ;

        String x = svc.getLocalName() ;
        log.info("Service: "+x) ;
        
        sDesc.name = ((Literal)getOne(svc, "fu:name")).getLexicalForm() ;
        log.info("  name = "+sDesc.name) ;

        addServiceEP("query", sDesc.name, sDesc.queryEP, svc, "fu:serviceQuery") ; 
        addServiceEP("update", sDesc.name, sDesc.updateEP, svc, "fu:serviceUpdate") ; 
        addServiceEP("upload", sDesc.name, sDesc.uploadEP, svc, "fu:serviceUpload") ; 
        addServiceEP("graphStore(RW)", sDesc.name, sDesc.readWriteGraphStoreEP, svc, "fu:serviceReadWriteGraphStore") ;
        addServiceEP("graphStore(R)", sDesc.name, sDesc.readGraphStoreEP, svc, "fu:serviceReadGraphStore") ;
        
        Resource datasetDesc = ((Resource)getOne(svc, "fu:dataset")) ;

        // Check if it is in the model.
        if ( ! datasetDesc.hasProperty(RDF.type) )
            throw new FusekiConfigException("No rdf:type for dataset "+nodeLabel(datasetDesc)) ;
        
        sDesc.dataset = (Dataset)Assembler.general.open(datasetDesc)  ;
        return sDesc ;
        
    }
    
    private static RDFNode getOne(Resource svc, String property)
    {
        String ln = property.substring(property.indexOf(':')+1) ;
        ResultSet rs = query("SELECT * { ?svc "+property+" ?x}", svc.getModel(), "svc", svc) ;
        if ( ! rs.hasNext() )
            throw new FusekiConfigException("No "+ln+" for service "+svc) ;
        RDFNode x = rs.next().get("x") ;
        if ( rs.hasNext() )
            throw new FusekiConfigException("Multiple "+ln+" for service "+svc) ;
        return x ;
    }
    
    private static void addServiceEP(String label, String name, List<String> output, Resource svc, String property)
    {
        ResultSet rs = query("SELECT * { ?svc "+property+" ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution soln = rs.next() ;
            String epName = soln.getLiteral("ep").getLexicalForm() ;
            output.add(epName) ;
            log.info("  "+label+" = /"+name+"/"+epName) ;
        }
    }

    // Dataset
    String[] s = new String[] {
        "SELECT ?x ?dft ?graphName ?graphData",
        "{ { ?x a ja:RDFDataset } UNION { ?x a [ rdfs:subClassOf ja:RDFDataset ] }",  
        "  OPTIONAL { ?x ja:defaultGraph ?dft }",
        "  OPTIONAL { ?x ja:namedGraph  [ ja:graphName ?graphName ; ja:graph ?graphData ] }",  
        "}", 
        "ORDER BY ?x ?dft ?graphName"
    } ;

    private static String prefixes = StrUtils.strjoinNL(
        "PREFIX fu:     <http://jena.apache.org/fuseki#>" ,
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
    
    // Node presentation
    public static String nodeLabel(RDFNode n)
    {
        if ( n == null )
            return "<null>" ;
        if ( n instanceof Resource )
            return strForResource((Resource)n) ;
        
        Literal lit = (Literal)n ;
        return lit.getLexicalForm() ;
    }
    
    public static String strForResource(Resource r) { return strForResource(r, r.getModel()) ; }
    
    private static String strForResource(Resource r, PrefixMapping pm)
    {
        if ( r == null )
            return "NULL ";
        if ( r.hasProperty(RDFS.label))
        {
            RDFNode n = r.getProperty(RDFS.label).getObject() ;
            if ( n instanceof Literal )
                return ((Literal)n).getString() ;
        }
        
        if ( r.isAnon() )
            return "<<blank node>>" ;

        if ( pm == null )
            pm = r.getModel() ;

        return strForURI(r.getURI(), pm ) ;
    }
    
    public static String strForURI(String uri, PrefixMapping pm)
    {
        if ( pm != null )
        {
            String x = pm.shortForm(uri) ;
            
            if ( ! x.equals(uri) )
                return x ;
        }
        return "<"+uri+">" ;
    }
}

