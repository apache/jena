/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            13-May-2003
 * Filename           $RCSfile: OntModelSpec.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-14 14:58:29 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;



// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;


/**
 * <p>
 * Encapsulates a description of the components of an ontology model, including the
 * storage scheme, reasoner and language profile.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntModelSpec.java,v 1.1 2003-05-14 14:58:29 ian_dickinson Exp $
 */
public class OntModelSpec {
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /** A specification for OWL models that are stored in memory and use the Transitive reasoner for simple entailments */
    public static final OntModelSpec OWL_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.TRANSITIVE, ProfileRegistry.OWL_LANG );
    
    /** A specification for OWL models that are stored in memory and use the RDFS inferencer for additional entailments */
    //public static final OntModelSpec OWL_MEM_RDFSINF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.RDFS, ProfileRegistry.OWL_LANG );
    
    /** A specification for OWL DL models that are stored in memory and use the Transitive reasoner for simple entailments */
    public static final OntModelSpec OWL_DL_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.TRANSITIVE, ProfileRegistry.OWL_DL_LANG );
    
    /** A specification for OWL DL models that are stored in memory and use the RDFS inferencer for additional entailments */
    //public static final OntModelSpec OWL_DL_MEM_RDFSINF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.RDFS, ProfileRegistry.OWL_DL_LANG );
    
    /** A specification for OWL Lite models that are stored in memory and use the Transitive reasoner for simple entailments */
    public static final OntModelSpec OWL_LITE_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.TRANSITIVE, ProfileRegistry.OWL_LITE_LANG );
    
    /** A specification for OWL Lite models that are stored in memory and use the RDFS inferencer for additional entailments */
    //public static final OntModelSpec OWL_LITE_MEM_RDFSINF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.RDFS, ProfileRegistry.OWL_LITE_LANG );
    
    /** A specification for DAML models that are stored in memory and use the Transitive reasoner for simple entailments */
    public static final OntModelSpec DAML_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.TRANSITIVE, ProfileRegistry.DAML_LANG );
    
    /** A specification for DAML models that are stored in memory and use the RDFS inferencer for additional entailments */
    //public static final OntModelSpec DAML_MEM_RDFSINF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, ReasonerRegistry.RDFS, ProfileRegistry.DAML_LANG );
    
    
    // Instance variables
    //////////////////////////////////
    
    /** The specifcation document manager */
    protected OntDocumentManager m_docManager = null;
    
    /** The specification reasoner */
    protected Reasoner m_reasoner = null;
    
    /** The language URI for the ontology */
    protected String m_languageURI;
    
    /** The ontology language profile */
    protected Profile m_profile = null;
    
    /** The model maker for this specification, or null to use the default from the doc manager */
    protected ModelMaker m_maker = null;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a new ontology model specification with the given specification parameters</p>
     * @param maker The model maker, which will be used to construct stores for statements in the 
     * imported ontologies and the base ontology. Use null to get a default (memory) model maker.
     * @param docMgr The document manager, or null for the default document manager.
     * @param reasoner The reasoner to use to infer additional triples in the model, or null for no reasoner
     * @param languageURI The URI of the ontology language. Required.
     */
    public OntModelSpec( ModelMaker maker, OntDocumentManager docMgr, Reasoner reasoner, String languageURI ) {
        setDocumentManager( docMgr );
        setModelMaker( maker );
        setReasoner( reasoner );
        
        if (languageURI == null) {
            throw new IllegalArgumentException( "Cannot create OntModelSpec with a null ontology language" );
        }
        setLanguage( languageURI );
    }
    
    
    /**
     * <p>Create one model spec as a copy of another. This is useful when what is required is similar to 
     * an existing spec, but with some changes. <strong>Note:</strong> this is only a shallow copy, so the
     * structured objects (reasoners, document managers, etc) are not themselves copied. Thus, even after
     * calling this copy constructor, making a change to the document manager in the copy specification
     * will also affect the one that the copy was made from. The correct idiom is to replace the object
     * before side-effecting it, e.g:
     * <code><pre>
     *   OntModelSpec newSpec = new OntModelSpec( existingSpec );
     *   newSpec.setDocumentManager( new OntDocumentManager() );
     *   newSpec.getDocumentManager().setMetaDataSearchPath( "..." );
     * </pre></code>
     * @param spec
     */
    public OntModelSpec( OntModelSpec spec ) {
        setDocumentManager( spec.getDocumentManager() );
        setModelMaker( spec.getModelMaker() );
        setReasoner( spec.getReasoner() );
        setLanguage( spec.getLanguage() );
    }
    
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer a default specification for the given language URI. This default
     * will typically use a memory model and have minimal inferencing capabilities.
     * </p>
     * @param languageURI The ontology language we want a default model spec for
     * @return The default model spec for that language
     * @exception OntologyException if the URI is not a recognised name of an ontology language
     */
    public static OntModelSpec getDefaultSpec( String languageURI ) {
        if (languageURI.equals( ProfileRegistry.OWL_LANG )) {
            return OWL_MEM;
        }
        else if (languageURI.equals( ProfileRegistry.OWL_DL_LANG )) {
            return OWL_DL_MEM;
        }
        else if (languageURI.equals( ProfileRegistry.OWL_LITE_LANG )) {
            return OWL_LITE_MEM;
        }
        else if (languageURI.equals( ProfileRegistry.DAML_LANG )) {
            return DAML_MEM;
        }
        else {
            throw new OntologyException( "Did not recognise this language URI, so cannot determine default model spec: " + languageURI );
        }
    }
    
    
    /**
     * <p>Answer the document manager for this model specification. Defaults to 
     * a standard instance of {@link OntDocumentManager}</p>
     * @return The document manager to be used by models matching this specification
     */
    public OntDocumentManager getDocumentManager() {
        if (m_docManager == null) {
            // need to set the default document manager
            m_docManager = new OntDocumentManager();
        }
        
        return m_docManager;
    }
    
    
    /**
     * <p>Set the document manager in this specification</p>
     * @param docMgr The new document manager 
     */
    public void setDocumentManager( OntDocumentManager docMgr ) {
        m_docManager = docMgr;
    }
    
    
    /**
     * <p>Answer the model maker that will be used to create new models to hold
     * ontologies loaded as imports for a given ontology document</p>
     * @return The model maker for this ontology specification
     */
    public ModelMaker getModelMaker() {
        if (m_maker == null) {
            m_maker = ModelFactory.createMemModelMaker();
        }
        
        return m_maker;
    }
    
    
    /**
     * <p>Set the model maker that will be used when the ontology model needs to create
     * an additional container for an imported ontology</p>
     * @param maker The new model maker to use
     */
    public void setModelMaker( ModelMaker maker ) {
        m_maker = maker;
    }
    
    
    /**
     * <p>Answer the reasoner that will be used to infer additional entailed 
     * triples in the ontology model.</p>
     * @return The reasoner for this specification
     */
    public Reasoner getReasoner() {
        return m_reasoner;
    }
    
    
    /**
     * <p>Set the reasoner that will be used by ontology models that conform 
     * to this specification to compute entailments.</p>
     * @param reasoner The new reasoner
     */
    public void setReasoner( Reasoner reasoner ) {
        m_reasoner = reasoner;
    }
    
    
    /**
     * <p>Answer the URI of the ontology lanuage to use when constructing
     * models from this specification.  Well known language URI's are
     * available from the {@link ProfileRegistry}</p>
     * @return The ontology language URI
     */
    public String getLanguage() {
        return m_languageURI;
    }
    
    
    /**
     * <p>Set the URI of the ontology to use for models that conform to 
     * this specification.</p>
     * @param languageURI The new language URI
     * @exception OntologyException if the URI does not map to a known language profile
     */
    public void setLanguage( String languageURI ) {
        m_languageURI = languageURI;
        m_profile = ProfileRegistry.getInstance().getProfile( m_languageURI ); 
        
        if (m_profile == null) {
            throw new OntologyException( "Could not determine an ontology language profile for URI " + m_languageURI );
        }
    }
    
    
    /**
     * <p>Answer the language profile for this ontology specification</p>
     * @return An ontology langauge profile object
     */
    public Profile getProfile() {
        return m_profile;
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

