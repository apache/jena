/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGInfGraph.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-08 09:31:39 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;





// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * An InfGraph that performs reasoning via a DIG interface to an external reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGInfGraph.java,v 1.3 2003-12-08 09:31:39 ian_dickinson Exp $
 */
public class DIGInfGraph
    extends BaseInfGraph
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The DIG adapter we will use to communicate with the external reasoner */
    protected DIGAdapter m_adapter;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * Constructor
     * @param data the raw data file to be augmented with entailments
     * @param reasoner the engine, with associated tbox data, whose find interface
     * can be used to extract all entailments from the data.
     */
    public DIGInfGraph( Graph data, DIGReasoner reasoner ) {
        super( data, reasoner );
        
        // we want a union of data and tbox
        if (reasoner.getSchema() != null) {
            fdata = new FGraph( new MultiUnion( new Graph[] {data, reasoner.getSchema()} ) );
        }
        
        // create or re-use a free connector
        DIGConnection conn = DIGConnectionPool.getInstance().allocate( reasoner.getReasonerURL() );
        m_adapter = new DIGAdapter( reasoner.getOntLangModelSpec(), fdata.getGraph(), conn );
    }
        

    // External signature methods
    //////////////////////////////////

    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    public void prepare() {
        if (!isPrepared) {
            m_adapter.resetKB();
            m_adapter.uploadKB();
            isPrepared = true;
        }
    }
    
    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results. 
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        prepare();
        ExtendedIterator i = m_adapter.find( pattern );
        return (continuation == null) ? i : i.andThen( continuation.find( pattern ) ); 
    }

   
    /**
     * Return the schema graph, if any, bound into this inference graph.
     */
    public Graph getSchemaGraph() {
        return ((DIGReasoner) reasoner).getSchema();
    }
    
    // overriding the BaseInfGraph methods
    
    /**
     * Replace the underlying data graph for this inference graph and start any
     * inferences over again. This is primarily using in setting up ontology imports
     * processing to allow an imports multiunion graph to be inserted between the
     * inference graph and the raw data, before processing.
     * @param data the new raw data graph
     */
    public void rebind( Graph data ) {
        if (getSchemaGraph() == null) {
            fdata = new FGraph(data);
        }
        else {
            fdata = new FGraph( new MultiUnion( new Graph[] {data, getSchemaGraph()} ) );
        }
        
        isPrepared = false;
    }
    
    
    /**
     * Switch on/off drivation logging - not supported with DIG reasoner
     */
    public void setDerivationLogging(boolean logOn) {
        throw new UnsupportedOperationException( "Cannot set derivation logging on DIG reasoner" );
    }
   
    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================



}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
