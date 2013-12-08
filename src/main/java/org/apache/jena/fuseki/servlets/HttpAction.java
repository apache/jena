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

import static com.hp.hpl.jena.query.ReadWrite.READ ;
import static com.hp.hpl.jena.query.ReadWrite.WRITE ;

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.ServiceRef ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWithLock ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.core.Transactional ;

public class HttpAction
{
    public final long id ;
    public final boolean verbose ;
    public final Logger log ;
    
    // ----
    // Worth subclassing? Given this is allocated in the general lifecycle
    // it would mean there are downcasts to the specific type.
    
    // -- Valid only for operational actions (e.g. SPARQL).
    
    private DatasetGraph dsg                = null ;
    public  ServiceRef srvRef               = null ;
    private Transactional   transactional   = null ;
    private boolean         isTransactional = false ;
    private DatasetGraph    activeDSG       = null ;        // Set when inside begin/end.
    private ReadWrite       activeMode      = null ;        // Set when inside begin/end.
    
    
    // -- Valid only for administration actions.
    /** Name of dataset being acted upon in a admin operation (may be null) */
    public String datasetName               = null ;
    
    
    // -- Shared items (but exact meaning may differ)
    /** Handle to dataset+services being acted on (maybe null) */
    public  DatasetRef dsRef                = null ;

    // ----
    
    private boolean startTimeIsSet = false ;
    private boolean finishTimeIsSet = false ;

    private long startTime = -2 ;
    private long finishTime = -2 ;
    
    // Outcome.
    int statusCode = -1 ;
    String message = null ;
    int contentLength = -1 ;
    String contentType = null ;
    
    // Cleared to archive:
    Map <String, String> headers = new HashMap<String, String>() ;
    public HttpServletRequest request;
    public HttpServletResponseTracker response ;
    
    public HttpAction(long id, Logger log, HttpServletRequest request, HttpServletResponse response, boolean verbose) {
        this.id = id ;
        this.log = log ;
        this.request = request ;
        this.response = new HttpServletResponseTracker(this, response) ;
        // Should this be set when setDataset is called from the dataset context?
        // Currently server-wide, e.g. from the command line.
        this.verbose = verbose ;
    }

    /** Initialization after action creation during lifecycle setup */
    public void setRequestRef(DatasetRef desc) {
        if ( this.dsRef != null )
            throw new FusekiException("Redefintion of DatasetRef in the request action") ;
        
        this.dsRef = desc ;
        if ( desc == null || desc.dataset == null )
            throw new FusekiException("Null DatasetRef in the request action") ;
        
        this.dsg = desc.dataset ;
        DatasetGraph basedsg = unwrap(dsg) ;

        if ( isTransactional(basedsg) && isTransactional(dsg) ) {
            // Use transactional if it looks safe - abort is necessary.
            transactional = (Transactional)dsg ;
            isTransactional = true ;
        } else {
            // Unsure if safe
            transactional = new DatasetGraphWithLock(dsg) ;
            // No real abort.
            isTransactional = false ;
        }
    }
    
    public void setControlRef(DatasetRef desc) {
        if ( desc == null )
            throw new FusekiException("Null DatasetRef in the control action") ;

        if ( this.dsRef != null )
            throw new FusekiException("Redefintion of DatasetRef in the control action") ;
        this.dsRef = desc ;
    }
    
    private static boolean isTransactional(DatasetGraph dsg) {
        return (dsg instanceof Transactional) ;
    }

    private static DatasetGraph unwrap(DatasetGraph dsg) {
        while (dsg instanceof DatasetGraphWrapper) {
            dsg = ((DatasetGraphWrapper)dsg).getWrapped() ;
        }
        return dsg ;
    }
        
    public void setService(ServiceRef srvRef) {
        this.srvRef = srvRef ; 
    }
    
    /**
     * Returns whether or not the underlying DatasetGraph is fully transactional (supports rollback)
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
