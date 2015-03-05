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

package org.apache.jena.fuseki.server;

import java.lang.reflect.Method ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.HttpNames ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.query.ARQ ;
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
import com.hp.hpl.jena.rdf.model.ResIterator ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraphReadOnly ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class FusekiConfig
{
    static { Fuseki.init(); }

    // The datastructure that captures a servers configuration.
    
    // Server port
    int port ;
    // Management command port - -1 for none.
    int mgtPort ;           
    List<DatasetRef> datasets = null ;
    
    
    private static Logger log = Fuseki.configLog ;
    
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
    
    public static ServerConfig defaultConfiguration(String datasetPath, DatasetGraph dsg, boolean allowUpdate, boolean listenLocal)
    {
        DatasetRef dbDesc = new DatasetRef() ;
        dbDesc.name = datasetPath ;
        dbDesc.dataset = dsg ;
        dbDesc.query.endpoints.add(HttpNames.ServiceQuery) ;
        dbDesc.query.endpoints.add(HttpNames.ServiceQueryAlt) ;

        if ( allowUpdate )
        {
            dbDesc.update.endpoints.add(HttpNames.ServiceUpdate) ;
            dbDesc.upload.endpoints.add(HttpNames.ServiceUpload) ;
            dbDesc.readWriteGraphStore.endpoints.add(HttpNames.ServiceData) ;
            dbDesc.allowDatasetUpdate = true ;
        }
        else
            dbDesc.readGraphStore.endpoints.add(HttpNames.ServiceData) ;
        ServerConfig config = new ServerConfig() ;
        config.datasets = Arrays.asList(dbDesc) ;
        config.port = 3030 ;
        config.mgtPort = 3031 ;
        config.pagesPort = config.port ;
        config.loopback = listenLocal ;
        config.jettyConfigFile = null ;
        config.pages = Fuseki.PagesStatic ;
        config.enableCompression = true ;
        config.verboseLogging = false ;
        return config ;
    }
    
    public static ServerConfig configure(String filename)
    {
        // Be absolutely sure everything has initialized.
        // Some initialization registers assemblers and sets abbreviation vocabulary. 
        ARQ.init();
        TDB.init() ;
        Fuseki.init() ;
        Model m = FileManager.get().loadModel(filename) ;

        // Find one server.
        List<Resource> servers = getByType(FusekiVocab.tServer, m) ;
        if ( servers.size() == 0 )
            throw new FusekiConfigException("No server found (no resource with type "+strForResource(FusekiVocab.tServer)) ;
        if ( servers.size() > 1 )
            throw new FusekiConfigException(servers.size()+" servers found (must be exactly one in a configuration file)") ;
        
        // ---- Server 
        Resource server = servers.get(0) ;
        processServer(server) ;

        // ---- Services
        ResultSet rs = query("SELECT * { ?s fu:services [ list:member ?member ] }", m) ; 
        if ( ! rs.hasNext() )
            log.warn("No services found") ;
        
        List<DatasetRef> services =  new ArrayList<DatasetRef>() ; 
        
        for ( ; rs.hasNext() ; )
        {
            QuerySolution soln = rs.next() ;
            Resource svc = soln.getResource("member") ;
            DatasetRef sd = processService(svc) ;
            services.add(sd) ;
        }
        
        // TODO Properties for the other fields.
        ServerConfig config = new ServerConfig() ;
        config.datasets = services ;
        config.port = 3030 ;
        config.mgtPort = 3031 ;
        config.pagesPort = config.port ;
        config.jettyConfigFile = null ;
        config.pages = Fuseki.PagesStatic ;
        config.enableCompression = true ;
        config.verboseLogging = false ;
        return config ;
    }
    

    // DatasetRef used where there isn't a real Dataset e.g. the SPARQL processor.  
    
    private static DatasetRef noDataset      = new DatasetRef() ;
    private static DatasetGraph dummyDSG        = new DatasetGraphReadOnly(DatasetGraphFactory.createMemFixed()) ;
    static {
        noDataset.name = "" ;
        noDataset.dataset = dummyDSG ;
        noDataset.query.endpoints.add(HttpNames.ServiceQuery) ;
        noDataset.query.endpoints.add(HttpNames.ServiceQueryAlt) ;
        noDataset.allowDatasetUpdate = false ;
        noDataset.init();
        // Don't register it.  
        // This is used as a placeholder and shoudl not be found by "all datasets"  
        // DatasetRegistry.get().put("", noDataset) ;
    }

    /** Return the DatasetRef (read-only) for when there is no dataset, just a SPARQL Query processor */ 
    public static DatasetRef serviceOnlyDatasetRef() { return noDataset ; }

    private static void processServer(Resource server)
    {
        // Global, currently.
        AssemblerUtils.setContext(server, Fuseki.getContext()) ;
        
        StmtIterator sIter = server.listProperties(JA.loadClass) ;
        for( ; sIter.hasNext(); )
        {
            Statement s = sIter.nextStatement() ;
            RDFNode rn = s.getObject() ;
            String className = null ;
            if ( rn instanceof Resource )
            {
                String uri = ((Resource)rn).getURI() ;
                if ( uri == null )
                {
                    log.warn("Blank node for class to load") ;
                    continue ;
                }
                String javaScheme = "java:" ;
                if ( ! uri.startsWith(javaScheme) )
                {
                    log.warn("Class to load is not 'java:': "+uri) ;
                    continue ;
                }
                className = uri.substring(javaScheme.length()) ;
            }
            if ( rn instanceof Literal )
                className = ((Literal)rn).getLexicalForm() ; 
            /*Loader.*/loadAndInit(className) ;
        }
        // ----
    }

    private static void loadAndInit(String className)
    {
        try {
            Class<?> classObj = Class.forName(className);
            log.info("Loaded "+className) ;
            Method initMethod = classObj.getMethod("init");
            initMethod.invoke(null);
        } catch (ClassNotFoundException ex)
        {
            log.warn("Class not found: "+className);
        } 
        catch (Exception e)         { throw new FusekiConfigException(e) ; }
    }

    private static DatasetRef processService(Resource svc)
    {
        log.info("Service: "+nodeLabel(svc)) ;
        DatasetRef sDesc = new DatasetRef() ;
        sDesc.name = ((Literal)getOne(svc, "fu:name")).getLexicalForm() ;
        log.info("  name = "+sDesc.name) ;

        addServiceEP("query", sDesc.name, sDesc.query, svc, "fu:serviceQuery") ; 
        addServiceEP("update", sDesc.name, sDesc.update, svc, "fu:serviceUpdate") ; 
        addServiceEP("upload", sDesc.name, sDesc.upload, svc, "fu:serviceUpload") ;
        addServiceEP("graphStore(RW)", sDesc.name, sDesc.readWriteGraphStore, svc, "fu:serviceReadWriteGraphStore") ;
        addServiceEP("graphStore(R)", sDesc.name, sDesc.readGraphStore, svc, "fu:serviceReadGraphStore") ;
        // Extract timeout overriding configuration if present.
        if (svc.hasProperty(FusekiVocab.pAllowTimeoutOverride)) {
            sDesc.allowTimeoutOverride = svc.getProperty(FusekiVocab.pAllowTimeoutOverride).getObject().asLiteral().getBoolean();
            if (svc.hasProperty(FusekiVocab.pMaximumTimeoutOverride)) {
                sDesc.maximumTimeoutOverride = (int) (svc.getProperty(FusekiVocab.pMaximumTimeoutOverride).getObject().asLiteral().getFloat() * 1000);
            }
        }
        
        Resource datasetDesc = ((Resource)getOne(svc, "fu:dataset")) ;

        // Check if it is in the model.
        if ( ! datasetDesc.hasProperty(RDF.type) )
            throw new FusekiConfigException("No rdf:type for dataset "+nodeLabel(datasetDesc)) ;
        
        Dataset ds = (Dataset)Assembler.general.open(datasetDesc)  ;
        sDesc.dataset = ds.asDatasetGraph() ; 
        return sDesc ;
        
    }
    
    private static RDFNode getOne(Resource svc, String property)
    {
        String ln = property.substring(property.indexOf(':')+1) ;
        ResultSet rs = query("SELECT * { ?svc "+property+" ?x}", svc.getModel(), "svc", svc) ;
        if ( ! rs.hasNext() )
            throw new FusekiConfigException("No "+ln+" for service "+nodeLabel(svc)) ;
        RDFNode x = rs.next().get("x") ;
        if ( rs.hasNext() )
            throw new FusekiConfigException("Multiple "+ln+" for service "+nodeLabel(svc)) ;
        return x ;
    }
    
    private static List<Resource> getByType(Resource type, Model m)
    {
        ResIterator rIter = m.listSubjectsWithProperty(RDF.type, type) ;
        return Iter.toList(rIter) ;
    }

    private static void addServiceEP(String label, String name, ServiceRef service, Resource svc, String property)
    {
        ResultSet rs = query("SELECT * { ?svc "+property+" ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution soln = rs.next() ;
            String epName = soln.getLiteral("ep").getLexicalForm() ;
            service.endpoints.add(epName) ;
            log.info("  "+label+" = /"+name+"/"+epName) ;
        }
    }


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
        try(QueryExecution qExec = QueryExecutionFactory.create(query, m, initValues)) {
            ResultSet rs = ResultSetFactory.copyResults(qExec.execSelect()) ;
            return rs ;
        }
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
    private static String nodeLabel(RDFNode n)
    {
        if ( n == null )
            return "<null>" ;
        if ( n instanceof Resource )
            return strForResource((Resource)n) ;
        
        Literal lit = (Literal)n ;
        return lit.getLexicalForm() ;
    }
    
    private static String strForResource(Resource r) { return strForResource(r, r.getModel()) ; }
    
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
    
    private static String strForURI(String uri, PrefixMapping pm)
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

