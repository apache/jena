/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            22 Feb 2003
 * Filename           $RCSfile: OntModelImpl.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-12 17:17:04 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.enhanced.*;

import java.io.*;
import java.util.*;



/**
 * <p>
 * Implementation of a model that can process general ontologies in OWL,
 * DAML and similar languages.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntModelImpl.java,v 1.1 2003-03-12 17:17:04 ian_dickinson Exp $
 */
public class OntModelImpl
    extends ModelCom
    implements OntModel
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The document manager this ontology model is using to load imports */
    protected OntDocumentManager m_docMgr;
    
    /** The graph factory this ontology model is using to build the union graph of imports */
    protected GraphFactory m_graphFactory;
    
    /** The language profile that defines what we can do in this ontology */
    protected Profile m_profile;
    
    /** List of URI strings of documents that have been imported into this one */
    protected Set m_imported = new HashSet();
    
    
    
    // Constructors
    //////////////////////////////////


    /**
     * <p>
     * Construct a new ontology model, using the given model as a base.  The document manager
     * will be used to build the imports closure of the model if its policy permits.
     * </p>
     * 
     * @param languageURI Denotes the language profile that this ontology is using
     * @param model The base model that may contain existing statements for the ontology
     * @param docMgr The ontology document manager to use when building the imports closure
     * @param gf The graph factory to use when building the union of the imported graphs
     */
    public OntModelImpl( String languageURI, Model model, OntDocumentManager docMgr, GraphFactory gf ) {
        // all ontologies are defined to be union graphs, to allow us to add the imports to the union
        super( new MultiUnion(), BuiltinPersonalities.model );
        
        Profile lang = ProfileRegistry.getInstance().getProfile( languageURI );
        if (lang == null) {
            // can't proceed after this
            throw new OntologyException( "Ontology model could not locate language profile for ontology language " + languageURI );
        }
        
        // add the base graph to the union first, so that it will receive updates
        MultiUnion union = (MultiUnion) graph;
        union.addGraph( model.getGraph() );
        
        // record pointers to the helpers we're using
        m_profile = lang;
        m_docMgr = docMgr;
        m_graphFactory = gf;
        
        // load the imports closure, according to the policies in my document manager
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
    }
    
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer an iterator that ranges over the ontology resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type Ontology</code> or equivalent. These resources
     * typically contain metadata about the ontology document that contains them. 
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model, see
     * {@link Profile#ONTOLOGY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over ontology resources. 
     */
    public Iterator listOntologies() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type Property</code> or equivalent.  An <code>OntProperty</code>
     * is equivalent to an <code>rdfs:Property</code> in a normal RDF graph; this type is
     * provided as a common super-type for the more specific {@link ObjectProperty} and
     * {@link DatatypeProperty} property types.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#PROPERTY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over property resources. 
     */
    public Iterator listOntProperties() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the object property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type ObjectProperty</code> or equivalent.  An object
     * property is a property that is defined in the ontology language semantics as a 
     * one whose range comprises individuals (rather than datatyped literals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#OBJECT_PROPERTY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over object property resources. 
     */
    public Iterator listObjectProperties() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the datatype property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type DatatypeProperty</code> or equivalent.  An datatype
     * property is a property that is defined in the ontology language semantics as a 
     * one whose range comprises datatyped literals (rather than individuals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#DATATYPE_PROPERTY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over datatype property resources. 
     */
    public Iterator listDatatypeProperties() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the individual resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type</code> corresponding to a class defined
     * in the ontology.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over individual resources. 
     */
    public Iterator listIndividuals() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the axiom resources in this model.  Example
     * axioms include the {@link AllDifferent} axiom in OWL, which allows an ontology
     * conveniently to express the unique names assumption for a bounded set of class
     * descriptions.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over axiom resources. 
     */
    public Iterator listAxioms()  {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over all of the various forms of class description resource 
     * in this model.  Class descriptions include {@link #listEnumeratedClasses enumerated}
     * classes, {@link #listUnionClasses union} classes, {@link #listComplementClasses complement}
     * classes, {@link #listIntersectionClasses intersection} classes, {@link #listOntClasses named}
     * classes and {@link #listRestrictions property restrictions}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over class description resources. 
     */
    public Iterator listClassDescriptions() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the enumerated class class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>oneOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over enumerated class resources. 
     * @see Profile#ONE_OF
     */
    public Iterator listEnumeratedClasses()  {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the union class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>unionOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over union class resources. 
     * @see Profile#UNION_OF
     */
    public Iterator listUnionClasses() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the complement class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>complementOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over complement class resources. 
     * @see Profile#COMPLEMENT_OF
     */
    public Iterator listComplementClasses() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the intersection class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>intersectionOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over complement class resources. 
     * @see Profile#INTERSECTION_OF
     */
    public Iterator listIntersectionClasses() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the named class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Class</code> (or equivalent) and a node URI. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over named class resources. 
     */
    public Iterator listOntClasses() {
        return null;  // TODO: stub
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the property restriction class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Restriction</code> (or equivalent). 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over restriction class resources. 
     * @see Profile#RESTRICTION
     */
    public Iterator listRestrictions() {
        return null;  // TODO: stub
    }


    /**
     * <p>
     * Answer the language profile (for example, OWL or DAML+OIL) that this model is 
     * working to.
     * </p>
     *  
     * @return A language profile
     */
    public Profile getProfile() {
        return m_profile;
    }


    /**
     * <p>
     * Answer true if this model has had the given URI document imported into it. This is
     * important to know since an import only occurs once, and we also want to be able to
     * detect cycles of imports.
     * </p> 
     *
     * @param uri An ontology URI 
     * @return True if the document corresponding to the URI has been successfully loaded
     * into this model
     */
    public boolean hasLoadedImport( String uri ) {
        return m_imported.contains( uri );
    }
    
    
    /**
     * <p>
     * Record that this model has now imported the document with the given
     * URI, so that it will not be re-imported in the future.
     * </p>
     * 
     * @param uri A document URI that has now been imported into the model.
     */
    public void addLoadedImport( String uri ) {
        m_imported.add( uri );
    }
    
    
    /**
     * <p>
     * Answer a list of the imported URI's in this ontology model. Detection of <code>imports</code>
     * statments will be according to the local language profile
     * </p>
     * 
     * @return The imported ontology URI's as a list. Note that since the underlying graph is
     * not ordered, the order of values in the list in successive calls to this method is
     * not guaranteed to be preserved.
     */
    public List listImportedOntologyURIs() {
        List imports = new ArrayList();
        
        // list the ontology nodes
        for (StmtIterator i = listStatements( null, RDF.type, m_profile.ONTOLOGY() );  i.hasNext(); ) {
            Resource ontology = i.nextStatement().getSubject();
            
            for (StmtIterator j = ontology.listProperties( m_profile.IMPORTS() ); j.hasNext();  ) {
                // add the imported URI to the list
                imports.add( j.nextStatement().getResource().getURI() );
            }
        }
        
        return imports;
    }
    
    
    /**
     * <p>
     * Answer the graph factory associated with this model (used for constructing the
     * constituent graphs of the imports closure).
     * </p>
     * 
     * @return The local graph factory
     */
    public GraphFactory getGraphFactory() {
        return m_graphFactory;
    }
    
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param uri URI to read from, may be mapped to a local source by the document manager
     */
    public Model read( String uri ) {
        super.read( m_docMgr.getCacheURL( uri ) );
        
        // cache this model against the public uri (if caching enabled)
        m_docMgr.addGraph( uri, getGraph() );
        
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input reader
     * @param base The base URI
     */
    public Model read(Reader reader, String base) {
        super.read( reader, base );
        
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input stream
     * @param base The base URI
     */
    public Model read(InputStream reader, String base) {
        super.read( reader, base );
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
        return this;
    } 
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param uri URI to read from, may be mapped to a local source by the document manager
     * @param lang The source syntax
     */
    public Model read(String uri, String syntax) {
        super.read( m_docMgr.getCacheURL( uri ), syntax );
                
        // cache this model against the public uri (if caching enabled)
        m_docMgr.addGraph( uri, getGraph() );

        m_docMgr.loadImports( this, new OntReadState( syntax, this ) );
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input reader
     * @param base The base URI
     * @param lang The source syntax
     */
    public Model read(Reader reader, String base, String syntax) {
        super.read( reader, base, syntax );
        
        m_docMgr.loadImports( this, new OntReadState( syntax, this ) );
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input stream
     * @param base The base URI
     * @param lang The source syntax
     */
    public Model read(InputStream reader, String base, String syntax) {
        super.read( reader, base, syntax );
        
        m_docMgr.loadImports( this, new OntReadState( syntax, this ) );
        return this;
    }
    
    
    /**
     * <p>
     * Read operation that is invoked while loading the imports closure of an
     * ontology model.  Not normally invoked by user code.
     * </p>
     *
     * @param uri The URI to load from 
     * @param readState The read-state for this operation
     */
    public void read( String uri, OntReadState readState ) {
        super.read( m_docMgr.getCacheURL( uri ), readState.getSyntax() );
        
        // cache this model against the public uri (if caching enabled)
        m_docMgr.addGraph( uri, getGraph() );
        m_docMgr.loadImports( this, readState );
    }
    
      
    /**
     * <p>
     * Answer the sub-graphs of this model. A sub-graph is defined as a graph that
     * is used to contain the triples from an imported document.
     * </p>
     * 
     * @return A list of sub graphs for this ontology model
     */
    public List getSubGraphs() {
        return ((MultiUnion) getGraph()).getSubGraphs();
    }
    
    
    /**
     * <p>
     * Answer the base-graph of this model. The base-graph is the graph that
     * contains the triples read from the source document for this ontology.
     * </p>
     * 
     * @return The base-graph for this ontology model
     */
    public Graph getBaseGraph() {
        return ((MultiUnion) getGraph()).getBaseGraph();
    }
    
    
    /**
     * <p>
     * Add the given graph as one of the sub-graphs of this ontology union graph
     * </p>
     *
     * @param graph A sub-graph to add 
     */
    public void addSubGraph( Graph graph ) {
        ((MultiUnion) getGraph()).addGraph( graph );
    }


    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
