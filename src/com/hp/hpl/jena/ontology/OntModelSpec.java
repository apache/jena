/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            13-May-2003
 * Filename           $RCSfile: OntModelSpec.java,v $
 * Revision           $Revision: 1.25 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-02-18 21:02:01 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;



// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;


/**
 * <p>
 * Encapsulates a description of the components of an ontology model, including the
 * storage scheme, reasoner and language profile.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntModelSpec.java,v 1.25 2004-02-18 21:02:01 ian_dickinson Exp $
 */
public class OntModelSpec extends ModelSpecImpl implements ModelSpec {
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /** A specification for OWL models that are stored in memory and do no additional entailment reasoning */
    public static final OntModelSpec OWL_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, null, ProfileRegistry.OWL_LANG );
    
    /** A specification for OWL models that are stored in memory and use the RDFS inferencer for additional entailments */
    public static final OntModelSpec OWL_MEM_RDFS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, RDFSRuleReasonerFactory.theInstance(), ProfileRegistry.OWL_LANG );
    
    /** A specification for OWL models that are stored in memory and use the transitive inferencer for additional entailments */
    public static final OntModelSpec OWL_MEM_TRANS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, TransitiveReasonerFactory.theInstance(), ProfileRegistry.OWL_LANG );
    
    /** A specification for OWL models that are stored in memory and use the OWL rules inference engine for additional entailments */
    public static final OntModelSpec OWL_MEM_RULE_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, OWLFBRuleReasonerFactory.theInstance(), ProfileRegistry.OWL_LANG );
    
    /** A specification for OWL DL models that are stored in memory and do no additional entailment reasoning */
    public static final OntModelSpec OWL_DL_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, null, ProfileRegistry.OWL_DL_LANG );
    
    /** A specification for OWL DL models that are stored in memory and use the RDFS inferencer for additional entailments */
    public static final OntModelSpec OWL_DL_MEM_RDFS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, RDFSRuleReasonerFactory.theInstance(), ProfileRegistry.OWL_DL_LANG );
    
    /** A specification for OWL DL models that are stored in memory and use the transitive inferencer for additional entailments */
    public static final OntModelSpec OWL_DL_MEM_TRANS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, TransitiveReasonerFactory.theInstance(), ProfileRegistry.OWL_DL_LANG );
    
    /** A specification for OWL DL models that are stored in memory and use the OWL rules inference engine for additional entailments */
    public static final OntModelSpec OWL_DL_MEM_RULE_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, OWLFBRuleReasonerFactory.theInstance(), ProfileRegistry.OWL_DL_LANG );
    
    /** A specification for OWL Lite models that are stored in memory and do no entailment additional reasoning */
    public static final OntModelSpec OWL_LITE_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, null, ProfileRegistry.OWL_LITE_LANG );
    
    /** A specification for OWL Lite models that are stored in memory and use the transitive inferencer for additional entailments */
    public static final OntModelSpec OWL_LITE_MEM_TRANS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, TransitiveReasonerFactory.theInstance(), ProfileRegistry.OWL_LITE_LANG );
    
    /** A specification for OWL Lite models that are stored in memory and use the RDFS inferencer for additional entailments */
    public static final OntModelSpec OWL_LITE_MEM_RDFS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, RDFSRuleReasonerFactory.theInstance(), ProfileRegistry.OWL_LITE_LANG );
    
    /** A specification for OWL Lite models that are stored in memory and use the OWL rules inference engine for additional entailments */
    public static final OntModelSpec OWL_LITE_MEM_RULES_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, OWLFBRuleReasonerFactory.theInstance(), ProfileRegistry.OWL_LITE_LANG );
    
    /** A specification for DAML models that are stored in memory and do no additional entailment reasoning */
    public static final OntModelSpec DAML_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, null, ProfileRegistry.DAML_LANG );
    
    /** A specification for DAML models that are stored in memory and use the transitive reasoner for entailments */
    public static final OntModelSpec DAML_MEM_TRANS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, TransitiveReasonerFactory.theInstance(), ProfileRegistry.DAML_LANG );
    
    /** A specification for DAML models that are stored in memory and use the RDFS inferencer for additional entailments */
    public static final OntModelSpec DAML_MEM_RDFS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, RDFSRuleReasonerFactory.theInstance(), ProfileRegistry.DAML_LANG );
    
    /** A specification for DAML models that are stored in memory and use a subset of the DAML semantic model additional entailments */
    public static final OntModelSpec DAML_MEM_RULE_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, DAMLMicroReasonerFactory.theInstance(), ProfileRegistry.DAML_LANG );
    
    /** A specification for RDFS ontology models that are stored in memory and do no additional entailment reasoning */
    public static final OntModelSpec RDFS_MEM = new OntModelSpec( ModelFactory.createMemModelMaker(), null, null, ProfileRegistry.RDFS_LANG );
    
    /** A specification for RDFS ontology models that are stored in memory and use the transitive reasoner for entailments */
    public static final OntModelSpec RDFS_MEM_TRANS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, TransitiveReasonerFactory.theInstance(), ProfileRegistry.RDFS_LANG );
    
    /** A specification for RDFS ontology models that are stored in memory and use the RDFS inferencer for additional entailments */
    public static final OntModelSpec RDFS_MEM_RDFS_INF = new OntModelSpec( ModelFactory.createMemModelMaker(), null, RDFSRuleReasonerFactory.theInstance(), ProfileRegistry.RDFS_LANG );
    
    
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
    
    /** The reasoner factory for creating the reasoner on demand */
    protected ReasonerFactory m_rFactory = null;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a new ontology model specification with the given specification parameters</p>
     * @param maker The model maker, which will be used to construct stores for statements in the 
     * imported ontologies and the base ontology. Use null to get a default (memory) model maker.
     * @param docMgr The document manager, or null for the default document manager.
     * @param rFactory The factory for the reasoner to use to infer additional triples in the model, or null for no reasoner
     * @param languageURI The URI of the ontology language. Required.
     */
    public OntModelSpec( ModelMaker maker, OntDocumentManager docMgr, ReasonerFactory rFactory, String languageURI ) {
        super( maker );
        setDocumentManager( docMgr );
        setReasonerFactory( rFactory );
        
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
        this( spec.getModelMaker(), spec.getDocumentManager(), 
            spec.getReasonerFactory(), spec.getLanguage() );
    }
    
    /**
        Initialise an OntModelSpec from an RDF description using the JMS vocabulary. See
        (insert reference here) for the description of the OntModel used. The root of the
        description is the unique resource with type JMS:OntMakerClass.
        
        @param description an RDF model using the JMS vocabulary
    */
    public OntModelSpec( Model description )  { 
        this( findRootByType( description, JMS.OntModelSpec ), description );
    }

    /**
        Initialise an OntModelSpec from an RDF description using the JMS vocabulary. See
        (insert reference here) for the description of the OntModel used. The root of the
        description is supplied as a parameter (so the description may describe several
        different OntModels).
        
        @param description an RDF model using the JMS vocabulary
        @param root the root of the sub-graph to use for the specification
    */    
    public OntModelSpec( Resource root, Model description )  { 
        this( getImportMaker( description, root ), getDocumentManager( description, root ),
            getReasonerFactory( description, root ), getLanguage( description, root )  );
        
    }

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer a default specification for the given language URI. This default
     * will typically use a memory model and have minimal inferencing capabilities.
     * Specifically, OWL and RDFS languages will have RDFS level inferencing 
     * capability (chosen to give a reasonable balance between power and efficiency
     * of computation), and DAML language will have the minimal DAML rule reasoner.
     * To get other (more powerful or less powerful) reasoning capabilities, users 
     * should create ontology models by passing an explicit <code>OntModelSpec</code>
     * parameter to the 
     * {@link ModelFactory#createOntologyModel( OntModelSpec, Model ) model factory}.
     * </p>
     * @param languageURI The ontology language we want a default model spec for
     * @return The default model spec for that language
     * @exception OntologyException if the URI is not a recognised name of an ontology language
     */
    public static OntModelSpec getDefaultSpec( String languageURI ) {
        if (languageURI.equals( ProfileRegistry.OWL_LANG )) {
            return OWL_MEM_RDFS_INF;
        }
        else if (languageURI.equals( ProfileRegistry.OWL_DL_LANG )) {
            return OWL_DL_MEM_RDFS_INF;
        }
        else if (languageURI.equals( ProfileRegistry.OWL_LITE_LANG )) {
            return OWL_LITE_MEM_RDFS_INF;
        }
        else if (languageURI.equals( ProfileRegistry.DAML_LANG )) {
            return DAML_MEM_RULE_INF;
        }
        else if (languageURI.equals( ProfileRegistry.RDFS_LANG )) {
            return RDFS_MEM_RDFS_INF;
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
            m_docManager = OntDocumentManager.getInstance();
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
     * <p>Set the model maker that will be used when the ontology model needs to create
     * an additional container for an imported ontology</p>
     * @param maker The new model maker to use
     */
    public void setModelMaker( ModelMaker maker ) {
        this.maker = maker;
    }
    
    /**
     * <p>Answer the reasoner that will be used to infer additional entailed 
     * triples in the ontology model.</p>
     * @return The reasoner for this specification
     */
    public Reasoner getReasoner() {
        if (m_reasoner == null && m_rFactory != null) {
            // we need to create the reasoner for the first time
            m_reasoner = m_rFactory.create( null );
        }
        
        return m_reasoner;
    }
    
    
    /**
     * <p>Set the reasoner that will be used by ontology models that conform 
     * to this specification to compute entailments.
     * <strong>Note:</strong> The reasoner is generated on demand by the reasoner
     * factory. To prevent this spec from having a reasoner, set the reasoner factory
     * to null, see {@link #setReasonerFactory}. 
     * </p>
     * @param reasoner The new reasoner
     */
    public void setReasoner( Reasoner reasoner ) {
        m_reasoner = reasoner;
    }
    
    
    /**
     * <p>Set the factory object that will be used to generate the reasoner object
     * for this model specification. <strong>Note</strong> that the reasoner itself is cached, so setting
     * the factory after a call to {@link #getReasoner()} will have no effect.</p>
     * @param rFactory The new reasoner factory, or null to prevent any reasoner being used
     */
    public void setReasonerFactory( ReasonerFactory rFactory ) {
        m_rFactory = rFactory;
    }
    
    /**
     * <p>Answer the current reasoner factory</p>
     * @return The reasoner factory, or null.
     */
    public ReasonerFactory getReasonerFactory() {
        return m_rFactory;
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
    
    /**
        Satisfy the ModelSpec interface: create an [Ont]Model according to the specification.
        The base model comes from the underlying ModelMaker.
        @return an OntModel satisfying this specification
    */
    public Model createModel() {
        return new OntModelImpl( this, maker.createModel() );
    }
    
    /**
        Satisfy the ModelSpec interface: create an [Ont]Model according to the specification.
        The base model comes from the underlying ModelMaker and is named by the
        give name.
     	@see com.hp.hpl.jena.rdf.model.ModelSpec#createModelOver(java.lang.String)
     */
    public Model createModelOver( String name ) {
        return new OntModelImpl( this, maker.createModel( name, false ) );
    }
    
    /**
    	Answer the ModelMaker to be used to construct models that are used for
        the imports of an OntModel. The ModelMaker is specified by the properties of
        the resource which is the object of the root's JMS.importMaker property.
        If no importMaker is specified, a MemModelMaker is constructed and used.
        
     	@param description the description model for [at least] this OntModel
     	@param root the root of the description for the OntModel
     	@return a ModelMaker fitting the importMaker description
     */
    public static ModelMaker getImportMaker( Model description, Resource root ) {
        Statement mStatement = description.getProperty( root, JMS.importMaker );
        return mStatement == null
            ? ModelFactory.createMemModelMaker()
            : createMaker( mStatement.getResource(), description ); 
    }
        
    /**
        Answer the URI string of the ontology language in this description.
     
        @param description the Model from which to extract the description
        @return the language string
        @exception something if the value isn't a URI resource
    */
    public static String getLanguage( Model description, Resource root ) {
        Statement langStatement = description.getRequiredProperty( root, JMS.ontLanguage );
        return langStatement.getString();
    }
    
    /**
        Answer an OntDocumentManager satisfying the docManager part of this description.
        Currently restricted to one where the object of JMS.docManager is registered with
        the value table held in ModelSpecImpl. If there's no such property, or if its bnode
        has no associated value, returns null.
        
         @param description the description of the OntModel
         @param root the root of the description
         @return the OntDocumentManager of root's JMS.docManager 
    */
    public static OntDocumentManager getDocumentManager( Model description, 
        Resource root ) {
        Statement docStatement = description.getProperty( root, JMS.docManager );
        if (docStatement == null) return null;
        Resource manager = docStatement.getResource();
        Statement policy = description.getProperty( manager, JMS.policyPath );
        if (policy == null)
            return (OntDocumentManager) getValue( manager );
        else
            return new OntDocumentManager( policy.getString() );
    }

    /**
        Answer a ReasonerFactory as described by the reasonsWith part of this discription.
        
        @param description the description of this OntModel
        @param root the root of this OntModel's description
        @return  a ReasonerFactory with URI given by root's reasonsWith's reasoner.
    */
    public static ReasonerFactory getReasonerFactory( Model description, Resource root ) {
        Statement factStatement = description.getProperty( root, JMS.reasonsWith );
        if (factStatement == null) return null;
        Statement reStatement = description.getProperty( factStatement.getResource(), JMS.reasoner );
        String factoryURI = reStatement.getResource().getURI();
        return ReasonerRegistry.theRegistry().getFactory( factoryURI );
    }

    /**
        Add the description of this OntModelSpec to the given model under the given 
        resource. This same description can be used to create an equivalent OntModelSpec.
        Serialising the description will lose the DocumentManager description.
        
        TODO allow the DocumentManager to be [de]serialised 
    */
    public Model addDescription( Model d, Resource self )  {
        super.addDescription( d, self );
        addLanguageDescription( d, self, m_languageURI );
        addManagerDescription( d, self, getDocumentManager() );
        addReasonerDescription( d, self, getReasonerFactory() );
        return d;
    }
    
    /**
        Answer the RDFS property used to attach this ModelSpec to its ModelMaker; used
        by the parent classes when constructing the RDF description for this Spec.
        
        @return JMS.importMaker
    */
    public Property getMakerProperty() {
        return JMS.importMaker;
    }
        
    /**
        Augment the description with that of our language
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param langURI the language URI 
    */
    protected void addLanguageDescription( Model d, Resource me, String langURI ) {
        d.add( me, JMS.ontLanguage, d.createLiteral( langURI ) );
    }
    
    /**
        Augment the description with that of our document manager [as a Java value]
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param man the document manager
    */
    protected  void addManagerDescription( Model d, Resource me, 
        OntDocumentManager man ) {
        d.add( me, JMS.docManager, createValue( man ) );    
    }
    
    /**
        Augment the description with that of our reasoner factory
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param rf the reasoner factory to describe 
    */        
    protected void addReasonerDescription( Model d, Resource me, ReasonerFactory rf )
        {
        Resource reasonerSelf = d.createResource();
        d.add( me, JMS.reasonsWith, reasonerSelf );  
        d.add( reasonerSelf, JMS.reasoner, d.createResource( rf.getURI() ) );  
        }
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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

