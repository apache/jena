/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGInfGraph.java,v $
 * Revision           $Revision: 1.11 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-07 09:56:35 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;





// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Profile;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * <p>
 * An InfGraph that performs reasoning via a DIG interface to an external reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGInfGraph.java,v 1.11 2004-12-07 09:56:35 andy_seaborne Exp $
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
        m_adapter = new DIGAdapter( reasoner.getOntLangModelSpec(), fdata.getGraph(), conn, reasoner.getAxioms() );
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
     * <p>Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.</p><p>
     * <strong>DIG implementation note:</strong> the default call into this
     * method from the base inference graph makes the continuation a query
     * of the base graph. Since {@link DIGAdapter} already queries the base
     * graph, there is no futher need to query it through the continuation.
     * Consequently, this implementation <em>does not call</em> the continuation.
     * Client code that wishes to provide a non-default continuation should
     * sub-class DIGInfGraph and provide a suitable call to the continuation.find().
     * </p>
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation Not used in this implementation
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        prepare();
        return m_adapter.find( pattern );
    }

   
    /**
     * <p>An extension of the {@link Graph#find} interface which allows the caller to 
     * encode complex expressions in RDF and then refer to those expressions
     * within the query triple. For example, one might encode a class expression
     * and then ask if there are any instances of this class expression in the
     * InfGraph. In the case of the DIGInfGraph, this is exactly the use case we assume.
     * In particular, we expect that the <code>object</code> node is the subject of
     * one or more sentences in <code>param</code> which completely define the class
     * description.<p>
     * @param subject the subject Node of the query triple, may be a Node in 
     * the graph or a node in the parameter micro-graph or null
     * @param property the property to be retrieved or null
     * @param object the object Node of the query triple, may be a Node in 
     * the graph or a node in the parameter micro-graph.    
     * @param param a small graph encoding an expression which the subject and/or
     * object nodes refer.
     */
    public ExtendedIterator find( Node subject, Node property, Node object, Graph param ) {
        OntModel premises = ModelFactory.createOntologyModel( m_adapter.getSourceSpecification(), 
                                                              ModelFactory.createModelForGraph( param ) );
        premises.setStrictMode( false );
        prepare();
        return m_adapter.find( new TriplePattern( subject, property, object ), premises );
    }
    
    /**
     * Return the schema graph, if any, bound into this inference graph.
     */
    public Graph getSchemaGraph() {
        return ((DIGReasoner) reasoner).getSchema();
    }
    
    // overriding the BaseInfGraph methods
    
    /**
     * <p>Add one triple to the data graph, mark the graph not-prepared,
     * but don't run prepare() just yet.</p>
     * @param t A triple to add to the graph
     */
    public synchronized void performAdd(Triple t) {
        fdata.getGraph().add(t);
        isPrepared = false;
    }

    /**
     * <p>Delete one triple from the data graph, mark the graph not-prepared,
     * but don't run prepare() just yet.</p>
     * @param t A triple to remove from the graph
     */
    public void performDelete(Triple t) {
        fdata.getGraph().delete(t);
        isPrepared = false;
    }

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
   
    
    /**
     * <p>Test the consistency of the model. This looks for overall inconsistency,
     * and for any unsatisfiable classes.</p>
     * @return a ValidityReport structure
     */
    public ValidityReport validate() {
        checkOpen();
        prepare();
        StandardValidityReport report = new StandardValidityReport();
        
        // look for incoherent KB by listing the individuals
        try {
            m_adapter.collectNamedTerms( DIGProfile.ALL_INDIVIDUALS,
                                         new String[] {DIGProfile.INDIVIDUAL_SET, DIGProfile.INDIVIDUAL} );
        }
        catch (DIGErrorResponseException e) {
            report.add( true, "DIG KB incoherent", e.getMessage() );
        }
        
        // now look for unsatisfiable classes
        Profile p = m_adapter.getOntLanguage();
        Property equivClass = p.EQUIVALENT_CLASS();
        DIGQueryEquivalentsTranslator q = new DIGQueryEquivalentsTranslator( equivClass.getURI(), true );
        ExtendedIterator i = q.find( new TriplePattern( null, equivClass.asNode(), p.NOTHING().asNode() ), m_adapter );
        
        while (i.hasNext()) {
            Triple t = (Triple) i.next();
            Node subj = t.getSubject();
            report.add( true, "unsatisfiable class", (subj.isBlank() ? subj.getBlankNodeId().toString() : subj.getURI()), t.getSubject() );
        }
        
        // look for incoherent instances
        Node nothing = p.NOTHING().asNode();
        DIGQueryTypesTranslator q1 = new DIGQueryTypesTranslator( RDF.type.getURI() );
        DIGValueToNodeMapper vMap = new DIGValueToNodeMapper();
        for (Iterator j = m_adapter.getKnownIndividuals().iterator(); j.hasNext(); ) {
            String ind = (String) j.next();
            Node indNode = (Node) vMap.map1( ind );
            
            try {
                ExtendedIterator i1 = q1.find( new TriplePattern( indNode, RDF.type.asNode(), null ), m_adapter );
            }
            catch (DIGErrorResponseException e) {
                // we assume this is an incoherent KB exception - should check
                report.add( true, "meaningless individual", (indNode.isBlank() ? indNode.getBlankNodeId().toString() : indNode.getURI()), ind );
            }
        }
        
        return report;
    }
    

    
    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================



}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
