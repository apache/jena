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

import static org.apache.jena.query.ReadWrite.READ ;
import static org.apache.jena.query.ReadWrite.WRITE ;

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.ServiceRef ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.* ;

/**
 * HTTP action that represents the user request lifecycle. Its state is handled in the
 * {@link SPARQL_ServletBase#executeLifecycle(HttpAction)} method.
 */
public class HttpAction
{
    public final long id ;
    public final boolean verbose ;
    
    // Phase two items - set and valida after the datasetRef is known.  
    private DatasetGraph dsg ;                  // The data
    public DatasetRef dsRef ;
    public ServiceRef srvRef ;
    
    private Transactional transactional ;
    private boolean isTransactional;
    private DatasetGraph    activeDSG ;             // Set when inside begin/end.
    private ReadWrite       activeMode ;            // Set when inside begin/end.
    
    private boolean startTimeIsSet = false ;
    private boolean finishTimeIsSet = false ;

    private long startTime = -2 ;
    private long finishTime = -2 ;
    
    // Incoming
    //public final 
    
    // Outcome.
    int statusCode = -1 ;
    String message = null ;
    int contentLength = -1 ;
    String contentType = null ;
    
    // Cleared to archive:
    Map <String, String> headers = new HashMap<>() ;
    public HttpServletRequest request;
    public HttpServletResponseTracker response ;

    /**
     * Creates a new HTTP Action, using the HTTP request and response, and a given ID.
     *
     * @param id given ID
     * @param request HTTP request
     * @param response HTTP response
     * @param verbose verbose flag
     */
    public HttpAction(long id, HttpServletRequest request, HttpServletResponse response, boolean verbose) {
        this.id = id ;
        this.request = request ;
        this.response = new HttpServletResponseTracker(this, response) ;
        // Should this be set when setDataset is called from the dataset context?
        // Currently server-wide, e.g. from the command line.
        this.verbose = verbose ;
    }

    /**
     * <p>Sets the action dataset. Setting a {@link DatasetRef} will replace any existing DatasetRef, as well as
     * as the {@link DatasetGraph} of the current HTTP Action.</p>
     *
     * <p>Once it has updated its members, the HTTP Action will change its transactional state and
     * {@link Transactional} instance according to its base dataset graph.</p>
     *
     * @param desc {@link DatasetRef}
     * @see Transactional
     * @see DatasetGraphWrapper
     */
    public void setDataset(DatasetRef desc) {
        this.dsRef = desc ;
        this.dsg = desc.dataset ;
        setTransactionalPolicy(dsg) ;
    }
    
    private void setTransactionalPolicy(DatasetGraph dsg) {
        if ( dsg.supportsTransactionAbort() ) {
            // Use transactional if it looks safe - abort is necessary.
            transactional = dsg ;
            isTransactional = true ;
        } else if ( dsg.supportsTransactions() ) {
            // No abort - e.g. loading data needs buffering against syntax errors.
            transactional = dsg ;
            isTransactional = false ;
        } else {
            // Nothing to build on.  Be safe.
            transactional = TransactionalLock.create(dsg.getLock()) ;
            isTransactional = false ;
        }
    }

    /**
     * A {@link DatasetGraph} may contain other <strong>wrapped DatasetGraph's</strong>. This method will return
     * the first instance (including the argument to this method) that <strong>is not</strong> an instance of
     * {@link DatasetGraphWrapper}.
     *
     * @param dsg a {@link DatasetGraph}
     * @return the first found {@link DatasetGraph} that is not an instance of {@link DatasetGraphWrapper}
     */
    // Unused currently.
    private static DatasetGraph x_unwrap(DatasetGraph dsg) {
        while (dsg instanceof DatasetGraphWrapper) {
            dsg = ((DatasetGraphWrapper)dsg).getWrapped() ;
        }
        return dsg ;
    }

    /**
     * Sets the {@link ServiceRef}.
     *
     * @param srvRef a {@link ServiceRef}
     */
    public void setService(ServiceRef srvRef) {
        this.srvRef = srvRef ; 
    }
    
    /**
     * Returns whether or not the underlying DatasetGraph is fully transactional (supports rollback).
     * @return <code>true</code> if the underlying DatasetGraph is fully transactional (supports rollback),
     * <code>false</code> otherwise.
     */
    public boolean isTransactional() {
        return isTransactional ;
    }

    public void beginRead() {
        activeMode = READ ;
        transactional.begin(READ) ;
        activeDSG = dsg ;
        dsRef.startTxn(READ) ;
    }

    public void endRead() {
        dsRef.finishTxn(READ) ;
        activeMode = null ;
        transactional.end() ;
        activeDSG = null ;
    }

    public void beginWrite() {
        transactional.begin(WRITE) ;
        activeMode = WRITE ;
        activeDSG = dsg ;
        dsRef.startTxn(WRITE) ;
    }

    public void commit() {
        transactional.commit() ;
        activeDSG = null ;
    }

    public void abort() {
        try { transactional.abort() ; } 
        catch (Exception ex) {
            // Some datasets claim to be transactional but
            // don't provide a real abort. We tried to avoid
            // them earlier but even if they sneek through,
            // we try to continue server operation.
            Log.warn(this, "Exception during abort (operation attempts to continue): "+ex.getMessage()) ; 
        }
        activeDSG = null ;
    }

    public void endWrite() {
        dsRef.finishTxn(WRITE) ;
        activeMode = null ;

        if ( transactional.isInTransaction() ) {
            Log.warn(this, "Transaction still active in endWriter - no commit or abort seen (forced abort)") ;
            try {
                transactional.abort() ;
            } catch (RuntimeException ex) {
                Log.warn(this, "Exception in forced abort (trying to continue)", ex) ;
            }
        }
        transactional.end() ;
        activeDSG = null ;
    }
   
    public final DatasetGraph getActiveDSG() {
        return activeDSG ;
    }

    public final DatasetRef getDatasetRef() {
        return dsRef ;
    }

    /** Reduce to a size that can be kept around for sometime */
    public void minimize() {
        this.request = null ;
        this.response = null ;
    }

    public void setStartTime() {
        if ( startTimeIsSet ) 
            Log.warn(this,  "Start time reset") ;
        startTimeIsSet = true ;
        this.startTime = System.nanoTime() ;
    }

    public void setFinishTime() {
        if ( finishTimeIsSet ) 
            Log.warn(this,  "Finish time reset") ;
        finishTimeIsSet = true ;
        this.finishTime = System.nanoTime() ;
    }

    public HttpServletRequest getRequest()              { return request ; }

    public HttpServletResponseTracker getResponse()     { return response ; }
    
    /** Return the recorded time taken in milliseconds. 
     *  {@linkplain #setStartTime} and {@linkplain #setFinishTime}
     *  must have been called.
     */
    public long getTime()
    {
        if ( ! startTimeIsSet ) 
            Log.warn(this,  "Start time not set") ;
        if ( ! finishTimeIsSet ) 
            Log.warn(this,  "Finish time not set") ;
        return (finishTime-startTime)/(1000*1000) ;
    }

    public void sync() {
        SystemARQ.sync(dsg) ;
    }

    public static MediaType contentNegotationRDF(HttpAction action) {
        MediaType mt = ConNeg.chooseContentType(action.request, DEF.rdfOffer, DEF.acceptRDFXML) ;
        if ( mt == null )
            return null ;
        if ( mt.getContentType() != null )
            action.response.setContentType(mt.getContentType()) ;
        if ( mt.getCharset() != null )
            action.response.setCharacterEncoding(mt.getCharset()) ;
        return mt ;
    }

    public static MediaType contentNegotationQuads(HttpAction action) {
        return ConNeg.chooseContentType(action.request, DEF.quadsOffer, DEF.acceptNQuads) ;
    }
}
