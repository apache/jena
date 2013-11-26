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

package org.apache.jena.fuseki.mgt;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.List ;

import javax.servlet.ServletException ;
import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.fuseki.servlets.ActionErrorException ;
import org.apache.jena.fuseki.servlets.ServletBase ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class DatasetsCollectionServlet extends ServletBase {
    
    public DatasetsCollectionServlet() {}
    
    // Move doCommon from SPARQL_ServletBase to between ServiceBase and SPARQL_ServletBase??
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        // POST a new description.
        try {
            execPost(request, response) ;
        } catch (ActionErrorException ex) {
            if ( ex.exception != null )
                ex.exception.printStackTrace(System.err) ;
            // XXX Log message done by printResponse in a moment.
            if ( ex.message != null )
                responseSendError(response, ex.rc, ex.message) ;
            else
                responseSendError(response, ex.rc) ;
        } 
    }
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        String name = request.getRequestURI() ;
        int idx = name.lastIndexOf('/') ;
        name = name.substring(idx) ;
        String datasetPath = DatasetRef.canocialDatasetPath(name) ;
        DatasetRef dsDesc = DatasetRegistry.get().get(datasetPath) ;
        if ( dsDesc == null )
            errorNotFound("Not found: "+name);
        
        JsonBuilder builder = new JsonBuilder() ;
        JsonDescription.describe(builder, dsDesc) ;
        JsonValue v = builder.build() ;
        ServletOutputStream out = response.getOutputStream() ;
        response.setContentType(WebContent.contentTypeJSON);
        response.setCharacterEncoding(WebContent.charsetUTF8) ;
        JSON.write(out, v) ;
        out.println() ; 
        out.flush() ;
    }

    protected void execPost(HttpServletRequest request, HttpServletResponse response) {
        Model m = ModelFactory.createDefaultModel() ;
        StreamRDF dest = StreamRDFLib.graph(m.getGraph()) ;
        bodyAsGraph(request, dest) ;
        Resource t = m.createResource("http://jena.apache.org/fuseki#Service") ;
        List<Resource> services = getByType(t, m) ; 
            
//        ResultSet rs = query("SELECT * { ?service rdf:type fuseki:Service }", m) ; 
//        List<Resource> services = new ArrayList<Resource>() ;
//        for ( ; rs.hasNext() ; )
//        {
//            QuerySolution soln = rs.next() ;
//            Resource svc = soln.getResource("service") ;
//            services.add(svc) ;
//        }
        
        if ( services.size() == 0 ) {
            log.error("No services found") ;
            throw new FusekiConfigException() ;
        }
        if ( services.size() > 1 ) {
            log.error("Multiple services found") ;
            throw new FusekiConfigException() ;
        }

        // Test name.
        
        Resource service = services.get(0) ;
        String name = ((Literal)getOne(service, "fuseki:name")).getLexicalForm() ;
        //log.info("name = "+name); 
        DatasetRef dsDesc = FusekiConfig.processService(service) ;
        String datasetPath = dsDesc.name ;
        if ( DatasetRegistry.get().isRegistered(datasetPath) )
            // Remove?
            errorBadRequest("Already registered: "+name);
        SPARQLServer.registerDataset(datasetPath, dsDesc) ;
    }

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
//    {
//        
//    }
    
    
    // XXX Merge with SPARQL_REST_RW.incomingData
    
    protected static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;

    private static void bodyAsGraph(HttpServletRequest request, StreamRDF dest) {
        String base = wholeRequestURL(request) ;
        ContentType ct = FusekiLib.getContentType(request) ;
        Lang lang = WebContent.contentTypeToLang(ct.getContentType()) ;
        if ( lang == null ) {
            errorBadRequest("Unknown content type for triples: " + ct) ;
            return ;
        }
        InputStream input = null ;
        try { input = request.getInputStream() ; } 
        catch (IOException ex) { IO.exception(ex) ; }

        int len = request.getContentLength() ;
//        if ( verbose ) {
//            if ( len >= 0 )
//                log.info(format("[%d]   Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s", action.id, len,
//                                ct.getContentType(), ct.getCharset(), lang.getName())) ;
//            else
//                log.info(format("[%d]   Body: Content-Type=%s, Charset=%s => %s", action.id, ct.getContentType(),
//                                ct.getCharset(), lang.getName())) ;
//        }

        parse(dest, input, lang, base) ;
    }

    public static void parse(StreamRDF dest, InputStream input, Lang lang, String base) {
        // Need to adjust the error handler.
//        try { RDFDataMgr.parse(dest, input, base, lang) ; }
//        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
        LangRIOT parser = RiotReader.createParser(input, lang, base, dest) ;
        parser.getProfile().setHandler(errorHandler) ;
        try { parser.parse() ; } 
        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
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
    
    // Temprary -- XXX Copies from original FusekiConfig
    
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

