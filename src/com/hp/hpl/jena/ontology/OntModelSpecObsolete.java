/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: OntModelSpecObsolete.java,v 1.1 2007-02-14 10:53:16 chris-dollin Exp $
*/

package com.hp.hpl.jena.ontology;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecImpl;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.WrappedReasonerFactory;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.JenaModelSpec;

public abstract class OntModelSpecObsolete extends ModelSpecImpl
    {
    public OntModelSpecObsolete( ModelMaker maker )
        { super( maker ); }

    public abstract Model implementCreateModelOver( String name );

    protected abstract Model doCreateModel();

    /**
        Answer the RDFS property used to attach this ModelSpec to its ModelMaker; used
        by the parent classes when constructing the RDF description for this Spec.

        @return JenaModelSpec.importMaker
    */
    public Property getMakerProperty() 
        {
        return JenaModelSpec.importMaker;
        }
    
    /**
        Answer a ModelMaker described by the <code>makerProperty</code> of
        <code>root</code> in <code>description</code>; if there is no such statement,
        answer a memory-model maker.
     */
    protected static ModelMaker getMaker( Model description, Resource root, Property makerProperty )    
        {
        Statement mStatement = description.getProperty( root, makerProperty );
        return mStatement == null
            ? ModelFactory.createMemModelMaker()
            : createMaker( mStatement.getResource(), description );
        }

    /**
         Answer a ReasonerFactory described by the properties of the resource
         <code>R</code> in the model <code>rs</code>. Will throw 
         NoReasonerSuppliedException if no jms:reasoner is supplied, or
         NoSuchReasonerException if the reasoner value isn't known to
         ReasonerRegistry. If any <code>ruleSetURL</code>s are supplied, the
         reasoner factory must be a RuleReasonerFactory, and is wrapped so that
         the supplied rules are specific to this Factory.
    */
    public static ReasonerFactory getReasonerFactory( Resource R, Model rs )
        {
        StmtIterator r = rs.listStatements( R, JenaModelSpec.reasoner, (RDFNode) null );
        if (r.hasNext() == false) throw new NoReasonerSuppliedException();
        Resource rr = r.nextStatement().getResource();
        String rrs = rr.getURI();
        ReasonerFactory rf = ReasonerRegistry.theRegistry().getFactory( rrs );
        if (rf == null) throw new NoSuchReasonerException( rrs );
        return new WrappedReasonerFactory( rf, ((Resource) R.inModel( rs )) );
        }

    /**
        Answer a ReasonerFactory as described by the reasonsWith part of this discription,
        or null if no reasoner specification has been supplied.

        @param description the description of this OntModel
        @param root the root of this OntModel's description
        @return  a ReasonerFactory with URI given by root's reasonsWith's reasoner.
    */
    public static ReasonerFactory getReasonerFactory( Model description, Resource root ) 
        {
        Statement factStatement = description.getProperty( root, JenaModelSpec.reasonsWith );
        if (factStatement == null) return null;
        return OntModelSpec.getReasonerFactory( factStatement.getResource(), description );
        }

    /**
        Answer an OntDocumentManager satisfying the docManager part of this description.
        Currently restricted to one where the object of JenaModelSpec.docManager is registered with
        the value table held in ModelSpecImpl. If there's no such property, or if its bnode
        has no associated value, returns null.

         @param description the description of the OntModel
         @param root the root of the description
         @return the OntDocumentManager of root's JenaModelSpec.docManager
    */
    public static OntDocumentManager getDocumentManager( Model description, Resource root ) 
        {
        Statement docStatement = description.getProperty( root, JenaModelSpec.docManager );
        if (docStatement == null) return null;
        Resource manager = docStatement.getResource();
        Statement policy = description.getProperty( manager, JenaModelSpec.policyPath );
        if (policy == null)
            return (OntDocumentManager) getValue( manager );
        else
            return new OntDocumentManager( policy.getString() );
        }

    /**
     * </p>Answer the ModelMaker to be used to construct models that are used for
     * the imports of an OntModel. The ModelMaker is specified by the properties of
     * the resource which is the object of the root's <code>jms:importMaker</code> property.
     * If no importMaker is specified, a MemModelMaker is returned as a default.</p>
     * @param description the description model for this OntModel
     * @param root the root of the description for the OntModel
     * @return a ModelMaker fitting the given description
     */
    public static ModelMaker getImportMaker( Model description, Resource root ) 
        {
        return getMaker( description, root, JenaModelSpec.importMaker );
        }

    /**
     * </p>Answer the ModelMaker to be used to construct models that are used for
     * the base model of an OntModel. The ModelMaker is specified by the properties of
     * the resource which is the object of the root's <code>jms:maker</code> property.
     * If no importMaker is specified, a MemModelMaker is returned as a default.</p>
     * @param description the description model for this OntModel
     * @param root the root of the description for the OntModel
     * @return a ModelMaker fitting the given description
     */
    public static ModelMaker getBaseMaker( Model description, Resource root ) 
        {
        return getMaker( description, root, JenaModelSpec.maker );
        }

    /**
        Answer the value of the jms:modelName property of <code>root</code>,
        or <code>null</code> id there isn't one.
    */
    protected static String getBaseModelName( Model description, Resource root ) 
        {
        Statement s = description.getProperty( root, JenaModelSpec.modelName );
        return s == null ? null : s.getString();
        }

    /**
        Answer the URI string of the ontology language in this description.

        @param description the Model from which to extract the description
        @return the language string
        @exception something if the value isn't a URI resource
    */
    public static String getLanguage( Model description, Resource root ) 
        {
        Statement langStatement = description.getRequiredProperty( root, JenaModelSpec.ontLanguage );
        return langStatement.getResource().getURI();
        }
    
    /**
        Augment the description with that of our language
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param langURI the language URI
    */
    protected void addLanguageDescription( Model d, Resource me, String langURI ) 
        {
        d.add( me, JenaModelSpec.ontLanguage, d.createResource( langURI ) );
        }

    protected void addImportsDescription( Model d, Resource me, ModelMaker m ) 
        {
        Resource importSelf = d.createResource();
        d.add( me, JenaModelSpec.importMaker, importSelf );
        m.addDescription( d, importSelf );
        }
    
    /**
        Augment the description with that of our document manager [as a Java value]
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param man the document manager
    */
    protected  void addManagerDescription( Model d, Resource me, OntDocumentManager man ) 
        {
        d.add( me, JenaModelSpec.docManager, createValue( man ) );
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
        d.add( me, JenaModelSpec.reasonsWith, reasonerSelf );
        if (rf != null)
            d.add( reasonerSelf, JenaModelSpec.reasoner, d.createResource( rf.getURI() ) );
        }

    protected Model addDescription( Model d, Resource self, ModelMaker importsMaker, String language, OntDocumentManager documentManager, ReasonerFactory reasonerFactory )
        {
        super.addDescription( d, self );
        addImportsDescription( d, self, importsMaker );
        addLanguageDescription( d, self, language );
        addManagerDescription( d, self, documentManager );
        addReasonerDescription( d, self, reasonerFactory );
        return d;
        }
    }

