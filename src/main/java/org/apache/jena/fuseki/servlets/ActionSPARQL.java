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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format ;
import static org.apache.jena.fuseki.server.CounterName.Requests ;
import static org.apache.jena.fuseki.server.CounterName.RequestsBad ;
import static org.apache.jena.fuseki.server.CounterName.RequestsGood ;

import java.io.InputStream ;
import java.util.zip.GZIPInputStream ;

import org.apache.commons.fileupload.FileItemIterator ;
import org.apache.commons.fileupload.FileItemStream ;
import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.query.QueryCancelledException ;

/** SPARQL request lifecycle */
public abstract class ActionSPARQL extends ActionBase
{
    protected ActionSPARQL() { super(Fuseki.requestLog) ; }
    
    protected abstract void validate(HttpAction action) ;
    protected abstract void perform(HttpAction action) ;

    @Override
    protected void execCommonWorker(HttpAction action)
    {
        DatasetRef dsRef = null ;

        String datasetUri = mapRequestToDataset(action) ;
        
        if ( datasetUri != null ) {
            dsRef = DatasetRegistry.get().get(datasetUri) ;
            if ( dsRef == null ) {
                errorNotFound("No dataset for URI: "+datasetUri) ;
                return ;
            }
        } else
            dsRef = FusekiConfig.serviceOnlyDatasetRef() ;

        action.setRequestRef(dsRef) ;
        String uri = action.request.getRequestURI() ;
        String serviceName = ActionLib.mapRequestToService(dsRef, uri, datasetUri) ;
        ServiceRef srvRef = dsRef.getServiceRef(serviceName) ;
        action.setService(srvRef) ;
        executeAction(action) ;
    }

    // Execute - no stats.
    // Intercept point for the UberServlet 
    protected void executeAction(HttpAction action) {
        executeLifecycle(action) ;
    }
    
    // This is the service request lifecycle.
    final
    protected void executeLifecycle(HttpAction action)
    {
        incCounter(action.dsRef, Requests) ;
        incCounter(action.srvRef, Requests) ;

        startRequest(action) ;
        try {
            validate(action) ;
        } catch (ActionErrorException ex) {
            incCounter(action.dsRef,RequestsBad) ;
            incCounter(action.srvRef, RequestsBad) ;
            throw ex ;
        }

        try {
            perform(action) ;
            // Success
            incCounter(action.srvRef, RequestsGood) ;
            incCounter(action.dsRef, RequestsGood) ;
        } catch (ActionErrorException ex) {
            incCounter(action.srvRef, RequestsBad) ;
            incCounter(action.dsRef, RequestsBad) ;
            throw ex ;
        } catch (QueryCancelledException ex) {
            incCounter(action.srvRef, RequestsBad) ;
            incCounter(action.dsRef, RequestsBad) ;
            throw ex ;
        } finally {
            finishRequest(action) ;
        }
    }
    
    /** Map request to uri in the registry.
     *  null means no mapping done (passthrough). 
     */
    protected String mapRequestToDataset(HttpAction action) 
    {
        return ActionLib.mapRequestToDataset(action.request.getRequestURI()) ;
    }
    
    protected static void incCounter(Counters counters, CounterName name) {
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().inc(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex) ;
        }
    }
    
    protected static void decCounter(Counters counters, CounterName name) {
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().dec(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex) ;
        }
    }

        public static void parse(HttpAction action, StreamRDF dest, InputStream input, Lang lang, String base) {
            // Need to adjust the error handler.
    //        try { RDFDataMgr.parse(dest, input, base, lang) ; }
    //        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
            LangRIOT parser = RiotReader.createParser(input, lang, base, dest) ;
            ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(action.log); 
            parser.getProfile().setHandler(errorHandler) ;
            try { parser.parse() ; } 
            catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
        }

    /**  Process an HTTP upload of RDF files (triples or quads)
     *   Stream straight into a graph or dataset -- unlike SPARQL_Upload the destination
     *   is known at the start of the multipart file body
     */
    
    static public void fileUploadWorker(HttpAction action, StreamRDF dest, boolean isGraph) {
        String base = wholeRequestURL(action.request) ;
        String item = (isGraph)?"quad":"triple" ;
        ServletFileUpload upload = new ServletFileUpload();
        long count = -1 ;
        
        //log.info(format("[%d] Upload: Field=%s ignored", action.id, fieldName)) ;
        
        try {
            FileItemIterator iter = upload.getItemIterator(action.request);
            while (iter.hasNext()) {
                FileItemStream fileStream = iter.next();
                if (fileStream.isFormField())
                    errorBadRequest("Only files accept in multipart file upload") ;
                //Ignore the field name.
                //String fieldName = fileStream.getFieldName();
    
                InputStream stream = fileStream.openStream();
                // Process the input stream
                String contentTypeHeader = fileStream.getContentType() ;
                ContentType ct = ContentType.create(contentTypeHeader) ;
                Lang lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
    
                if ( lang == null ) {
                    String name = fileStream.getName() ; 
                    if ( name == null || name.equals("") ) 
                        errorBadRequest("No name for content - can't determine RDF syntax") ;
                    lang = RDFLanguages.filenameToLang(name) ;
                    if (name.endsWith(".gz"))
                        stream = new GZIPInputStream(stream);
                }
                if ( lang == null )
                    // Desperate.
                    lang = RDFLanguages.RDFXML ;
    
                String printfilename = fileStream.getName() ; 
                if ( printfilename == null  || printfilename.equals("") )
                    printfilename = "<none>" ; 
    
                // Before
                // action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s", 
                //                        action.id, printfilename,  ct.getContentType(), ct.getCharset(), lang.getName())) ;
                
                StreamRDFCounting countingDest =  StreamRDFLib.count(dest) ;
                try {
                    ActionSPARQL.parse(action, countingDest, stream, lang, base);
                    long c = countingDest.count() ;
                    
                    action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s : %d %s%s", 
                                           action.id, printfilename,  ct.getContentType(), ct.getCharset(), lang.getName(),
                                           c, item, (c==1)?"":"s")) ;
                } catch (RiotParseException ex) {
                    action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s : %s",
                                           action.id, printfilename,  ct.getContentType(), ct.getCharset(), lang.getName(),
                                           ex.getMessage())) ;
                    throw ex ;
                }
            }
        }
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex)            { errorOccurred(ex) ; }
    }
}
