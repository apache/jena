/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10-Sep-2003
 * Filename           $RCSfile: OntEventManager.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-08-13 16:14:12 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.event;



// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.vocabulary.OntEventsVocab;


/**
 * <p>
 * An adapter that translates RDF model-level changes into higher level changes that
 * are appropriate to ontology users.  The large number of specific events that the
 * ontology model can produce makes the traditional Java style of listener interface
 * impractical. Instead, this event manager allows the user to register 
 * {@linkplain OntEventHandler handlers}, based on command pattern, against specific
 * {@linkplain OntEventsVocab event types}. 
 * </p>
 * <p>
 * For example, to register a handler for the declaration of an ontology class:
 * </p>
 * <pre>
 * OntModel m = ...
 * OntEventManager em = m.getEventManager();
 * em.addHandler( OntEvents.CLASS_DECLARATION,
 *                new OntEventHandler() {
 *                    public void action( Resource ev, boolean added,
 *                                        RDFNode arg0, RDFNode arg1 ) {
 *                        OntClass c = (OntClass) arg0;
 *                        if (added) {
 *                            // class c added to model
 *                        }
 *                        else {
 *                            // class c removed from model
 *                        }
 *                    }
 *                }
 *              );
 * </pre>
 * <p>
 * This listener acts as an adapter for graph events (i.e. adding and
 * removing triples), converting them to higher-level ontology events.  This is non-trivial, because
 * Jena currently doesn't have a means of batching changes so that only consistent
 * graph states are seen.  For efficiency in non event-using models, this listener
 * is only attached as a statement listener to the underlying graph when the first
 * ontology event listener is added.
 * </p>
 * 
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntEventManager.java,v 1.4 2004-08-13 16:14:12 ian_dickinson Exp $
 */
public class OntEventManager 
    extends StatementListener
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /** Initialisation data for the rdf:type to event type table */
    private static Object[][] s_rdfTypeInit = {
        {OntEventsVocab.classDeclaration,                       new ProfileAccessor() {public Resource get(Profile p) {return p.CLASS();} } },
        {OntEventsVocab.datarangeDeclaration,                   new ProfileAccessor() {public Resource get(Profile p) {return p.DATARANGE();} } }, 
        {OntEventsVocab.propertyDeclaration,                    new ProfileAccessor() {public Resource get(Profile p) {return p.PROPERTY();} } }, 
        {OntEventsVocab.objectPropertyDeclaration,             new ProfileAccessor() {public Resource get(Profile p) {return p.OBJECT_PROPERTY();} } }, 
        {OntEventsVocab.datatypePropertyDeclaration,           new ProfileAccessor() {public Resource get(Profile p) {return p.DATATYPE_PROPERTY();} } }, 
        {OntEventsVocab.transitivePropertyDeclaration,         new ProfileAccessor() {public Resource get(Profile p) {return p.TRANSITIVE_PROPERTY();} } }, 
        {OntEventsVocab.symmetricPropertyDeclaration,          new ProfileAccessor() {public Resource get(Profile p) {return p.SYMMETRIC_PROPERTY();} } }, 
        {OntEventsVocab.functionalPropertyDeclaration,         new ProfileAccessor() {public Resource get(Profile p) {return p.FUNCTIONAL_PROPERTY();} } }, 
        {OntEventsVocab.inverseFunctionalPropertyDeclaration, new ProfileAccessor() {public Resource get(Profile p) {return p.INVERSE_FUNCTIONAL_PROPERTY();} } }, 
        {OntEventsVocab.annotationPropertyDeclaration,         new ProfileAccessor() {public Resource get(Profile p) {return p.ANNOTATION_PROPERTY();} } }, 
        {OntEventsVocab.ontologyPropertyDeclaration,           new ProfileAccessor() {public Resource get(Profile p) {return p.ONTOLOGY_PROPERTY();} } }, 
        {OntEventsVocab.restrictionDeclaration,                 new ProfileAccessor() {public Resource get(Profile p) {return p.RESTRICTION();} } }, 
        {OntEventsVocab.allDifferentDeclaration,               new ProfileAccessor() {public Resource get(Profile p) {return p.ALL_DIFFERENT();} } }, 
        {OntEventsVocab.ontologyDeclaration,                    new ProfileAccessor() {public Resource get(Profile p) {return p.ONTOLOGY();} } }, 
    };
    
    /** Initialisation data for the predicate to event type table */
    private static Object[][] s_predicateInit = {
        {OntEventsVocab.intersectionOf,                         new ProfileAccessor() {public Resource get(Profile p) {return p.INTERSECTION_OF();} } }, 
        {OntEventsVocab.equivalentClass,                        new ProfileAccessor() {public Resource get(Profile p) {return p.EQUIVALENT_CLASS();} } }, 
        {OntEventsVocab.disjointWith,                           new ProfileAccessor() {public Resource get(Profile p) {return p.DISJOINT_WITH();} } }, 
        {OntEventsVocab.equivalentProperty,                     new ProfileAccessor() {public Resource get(Profile p) {return p.EQUIVALENT_PROPERTY();} } }, 
        {OntEventsVocab.sameAs,                                 new ProfileAccessor() {public Resource get(Profile p) {return p.SAME_AS();} } }, 
        {OntEventsVocab.differentFrom,                          new ProfileAccessor() {public Resource get(Profile p) {return p.DIFFERENT_FROM();} } }, 
        {OntEventsVocab.distinctMembers,                        new ProfileAccessor() {public Resource get(Profile p) {return p.DISTINCT_MEMBERS();} } }, 
        {OntEventsVocab.unionOf,                                new ProfileAccessor() {public Resource get(Profile p) {return p.UNION_OF();} } }, 
        {OntEventsVocab.intersectionOf,                         new ProfileAccessor() {public Resource get(Profile p) {return p.INTERSECTION_OF();} } }, 
        {OntEventsVocab.complementOf,                           new ProfileAccessor() {public Resource get(Profile p) {return p.COMPLEMENT_OF();} } }, 
        {OntEventsVocab.oneOf,                                  new ProfileAccessor() {public Resource get(Profile p) {return p.ONE_OF();} } }, 
        {OntEventsVocab.onProperty,                             new ProfileAccessor() {public Resource get(Profile p) {return p.ON_PROPERTY();} } }, 
        {OntEventsVocab.allValuesFrom,                         new ProfileAccessor() {public Resource get(Profile p) {return p.ALL_VALUES_FROM();} } }, 
        {OntEventsVocab.hasValue,                               new ProfileAccessor() {public Resource get(Profile p) {return p.HAS_VALUE();} } }, 
        {OntEventsVocab.someValuesFrom,                        new ProfileAccessor() {public Resource get(Profile p) {return p.SOME_VALUES_FROM();} } }, 
        {OntEventsVocab.minCardinality,                         new ProfileAccessor() {public Resource get(Profile p) {return p.MIN_CARDINALITY();} } }, 
        {OntEventsVocab.maxCardinality,                         new ProfileAccessor() {public Resource get(Profile p) {return p.MAX_CARDINALITY();} } }, 
        {OntEventsVocab.cardinalityQ,                           new ProfileAccessor() {public Resource get(Profile p) {return p.CARDINALITY_Q();} } }, 
        {OntEventsVocab.minCardinalityQ,                       new ProfileAccessor() {public Resource get(Profile p) {return p.MIN_CARDINALITY_Q();} } }, 
        {OntEventsVocab.maxCardinalityQ,                       new ProfileAccessor() {public Resource get(Profile p) {return p.MAX_CARDINALITY_Q();} } }, 
        {OntEventsVocab.cardinality,                             new ProfileAccessor() {public Resource get(Profile p) {return p.CARDINALITY();} } }, 
        {OntEventsVocab.inverseOf,                              new ProfileAccessor() {public Resource get(Profile p) {return p.INVERSE_OF();} } }, 
        {OntEventsVocab.imports,                                 new ProfileAccessor() {public Resource get(Profile p) {return p.IMPORTS();} } }, 
        {OntEventsVocab.versionInfo,                            new ProfileAccessor() {public Resource get(Profile p) {return p.VERSION_INFO();} } }, 
        {OntEventsVocab.priorVersion,                           new ProfileAccessor() {public Resource get(Profile p) {return p.PRIOR_VERSION();} } }, 
        {OntEventsVocab.backwardCompatibleWith,                new ProfileAccessor() {public Resource get(Profile p) {return p.BACKWARD_COMPATIBLE_WITH();} } }, 
        {OntEventsVocab.incompatibleWith,                       new ProfileAccessor() {public Resource get(Profile p) {return p.INCOMPATIBLE_WITH();} } }, 
    };
    
    
    // Instance variables
    //////////////////////////////////

    /** Map from event types to handlers */
    private Map m_handlers = new HashMap();
    
    /** Map from rdf:type to event type */
    private Map m_rdfTypeToEventType = new HashMap();
    
    /** Map from predicate to event type */
    private Map m_predicateToEventType = new HashMap();
    
    /** Default event handler */
    private OntEventHandler m_defaultHandler = null;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct an ontology event manager for the given ontology model.
     * This involves registering adapters for the ontology events corresponding
     * to the language profile of the given model.</p>
     * @param m An ontology model 
     */
    public OntEventManager( OntModel m ) {
        Profile p = m.getProfile();
        initialiseTable( m_rdfTypeToEventType, p, s_rdfTypeInit );
        initialiseTable( m_predicateToEventType, p, s_predicateInit );
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Handle the addition of a statement to the model.</p>
     * @param s The added statement
     */
    public void addedStatement( Statement s ) {
        processStatement( s, true );
    }
    
    
    /**
     * <p>Handle the removal of a statement to the model</p>
     * @param s The removed statement
     */
    public void removedStatement( Statement s ) {
        processStatement( s, false );
    }
    
    
    /**
     * <p>Raise an event to be handled by the attached event handlers.</p>
     * @param event The resource representing the event type
     * @param added True if this is an addition to the model, false otherwise
     * @param source The model that caused the event to be raised
     * @param arg0 The first argument to the event
     * @param arg1 The second argument to the event, or null
     * @param arg2 The third argument to the event, or null
     */
    public void raise( Resource event, boolean added, Model source, RDFNode arg0, RDFNode arg1, RDFNode arg2 ) {
        OntEventHandler h = getHandler( event );
        if (h != null) {
            h.action( event, added, source, arg0, arg1, arg2 );
        }
        else if (m_defaultHandler != null) {
            // if no assigned handler, call the default handler
            m_defaultHandler.action( event, added, source, arg0, arg1, arg2 );
        }
    }
    
    
    /**
     * <p>Add the given handler as the default event handler, which will be invoked if
     * no other handler is applicable to a given event.</p>
     * @param handler The event handler object
     */
    public void addDefaultHandler( OntEventHandler handler ) {
        m_defaultHandler = handler;
    }   


    /**
     * <p>Add the given handler as the handler for the given event type, replacing
     * any existing handler.</p>
     * @param event The event type to be handled, as a resource
     * @param handler The event handler object
     */
    public void addHandler( Resource event, OntEventHandler handler ) {
        m_handlers.put( event, handler );
    }   


    /**
     * <p>Add the given handlers as the handler for the given event types, replacing
     * any existing handlers.</p>
     * @param handlers An array of pairs, where the first element of each pair
     * is the resource denoting the event to be handled, and the second is the 
     * handler object
     */
    public void addHandlers( Object[][] handlers ) {
        for (int i = 0;  i < handlers.length;  ) {
            Resource r = (Resource) handlers[i][0];
            OntEventHandler h = (OntEventHandler) handlers[i][1];
            addHandler( r, h );
        }
    }   

    /**
     * <p>Answer the event handler for the given event, or null if not defined</p>
     * @param event An event type to look up
     * @return The current handler for the event, or null
     */
    public OntEventHandler getHandler( Resource event ) {
        return (OntEventHandler) m_handlers.get( event );
    }   


    /**
     * <p>Answer the default event handler, or null if not defined</p>
     * @return The default event handler, or null
     */
    public OntEventHandler getDefaultHandler() {
        return m_defaultHandler;
    }   


    /**
     * <p>Remove any existing handler for the given event type.</p>
     * @param event The event for which the handler is to be removed
     */
    public void removeHandler( Resource event ) {
        m_handlers.remove( event );
    }   


    /**
     * <p>Answer true if there is a defined handler for the given event type.</p>
     * @param event An event type, as a resource
     * @return True if there is a defined handler for the event
     */
    public boolean hasHandler( Resource event ) {
        return m_handlers.containsKey( event );
    }   
    
    
    /**
     * <p>Answer an iterator over the events that are registered in this
     * event manager.</p>
     * @return An iterator over the event types of events that have registered handlers
     */
    public Iterator listRegisteredEvents() {
        return m_handlers.keySet().iterator();
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Initialise the given map from static initialisation data. This will be a mapping
     * that allows us to determine the correct ontology event type for a given triple.</p>
     * @param map The map to update
     * @param p An ontology language profile
     * @param source A table of initialisation data
     */
    private void initialiseTable( Map map, Profile p, Object[][] source ) {
        for (int i = 0;  i < source.length;  i++) {
            // get the initialisation data from the table
            Resource evType = (Resource) source[i][0];
            Resource key = ((ProfileAccessor) source[i][1]).get( p );
            
            if (key != null) {
                // is defined for this term
                map.put( key, evType );
            }
        }
    }
    
    
    /**
     * <p>Process an incoming added or removed statement to raise the appropriate ontology event.</p>
     * @param s
     * @param added
     */
    private void processStatement( Statement s, boolean added ) {
        // first check if this an rdf:type statement
        if (s.getPredicate().equals( RDF.type )) {
            // yes - now is it a builtin type?
            Resource type = s.getResource();
            Resource evType = (Resource) m_rdfTypeToEventType.get( type );
            
            if (evType != null) {
                // was a known type, so we know which event to raise
                raise( evType, added, s.getModel(), s.getSubject(), null, null );
            }
            else {
                // must be an individual
                raise( OntEventsVocab.individualDeclaration, added, s.getModel(), s.getSubject(), type, null );
            }
        }
        else {
            // not rdf:type, but may still be a known predicate
            Property pred = s.getPredicate();
            Resource evType = (Resource) m_predicateToEventType.get( pred );
            
            if (evType != null) {
                // was a known predicate, so we know which event to raise
                raise( evType, added, s.getModel(), s.getSubject(), s.getObject(), null );
            }
            else {
                // default - assume user data
                raise( OntEventsVocab.userData, added, s.getModel(), s.getSubject(), pred, s.getObject() );
            }
        }
    }
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Simple wrapper to allow us to dynamically extract elements from the profile */
    private static interface ProfileAccessor {
        public Resource get( Profile p );
    }
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
