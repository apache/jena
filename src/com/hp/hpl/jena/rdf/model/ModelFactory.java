/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelFactory.java,v 1.10 2003-05-08 15:19:34 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.impl.OntModelImpl;
import com.hp.hpl.jena.enhanced.*;


/**
    Factory provides methods for creating standard kinds of Model. 
*/

public class ModelFactory
{
    /** 
        construct a new memory-based model that captures reification triples 
    */
    public static Model createDefaultModel()
        { return new ModelCom( GraphBase.withReification( new GraphMem() ), BuiltinPersonalities.model );}

    /**
        Answer a new persistent model as described by <code>name</code>.
        The named model must not already exist.
        
        @return a fresh, empty persistent model.
        @exception [perhaps] AlreadyExistsException a model with that name already exists.
    */
    
//    public static Model createPersistentModel( String name )
//        { return new ModelCom( Factory.createPersistentGraph( name ) ); }
        
    /**
        Answer a persistent model with the given <code>name</code>. The
        named model must already exist.
        
        @return a model mapping to the persisted name
        @exception [perhaps] NoSuchModelException if it doesn't exist
    */
//    public static Model openPersistentModel( String name )
//        { return new ModelCom( Factory.openPersistentGraph( name ) ); }
        
    /**
        construct a new memory-based model that does not capture reification triples
        (but still handles reifyAs() and .as(ReifiedStatement).
    */
    public static Model createNonreifyingModel()
        { return new ModelCom( new GraphMem(), BuiltinPersonalities.model );}
        
    /** 
     * <p>
     * Answer a model that encapsulates the given graph.
     * </p>
     * 
     * @param g A graph structure
     * @return A model presenting an API view of graph g
     */
    public static Model createModelForGraph( Graph g ) {
        return new ModelCom( g ); 
    }
        
    /**
     * Return a Model through which all the RDFS entailments 
     * derivable from the given model are accessible. Some work is done
     * when the inferenced model is created but each query will also trigger some
     * additional inference work.
     * <p> The current implementation is <em>very</em> preliminary
     * and will not scale to large models. In particular, it will make redundant 
     * passes over the data when asked a very ungrounded query (such as list all 
     * statements!). </p>
     * 
     * @param model the Model containing both instance data and schema assertions to be inferenced over
     */
    public static Model createRDFSModel(Model model) {
         ReasonerFactory rf = RDFSReasonerFactory.theInstance();
         Reasoner reasoner  = rf.create(null);
         InfGraph graph     = reasoner.bind(model.getGraph());
         return createModelForGraph(graph);
    }
        
    /**
     * Return a Model through which all the RDFS entailments 
     * derivable from the given data and schema models are accessible. 
     * There is no strict requirement to separate schema and instance data between the two
     * arguments.
     * <p>Some work is donewhen the inferenced model is created. This work can be reused if the
     * same schema is to be applied to multiple datasets though use of the direct SPI.</p>
     * <p> The current implementation is <em>very</em> preliminary
     * and will not scale to large models. In particular, it will make redundant 
     * passes over the data when asked a very ungrounded query (such as list all 
     * statements!). </p>
     * 
     * @param model a Model containing instance data assertions 
     * @param schema a Model containing RDFS schema data
     */
    public static Model createRDFSModel(Model schema, Model model) {
         ReasonerFactory rf = RDFSReasonerFactory.theInstance();
         Reasoner reasoner  = rf.create(null);
         InfGraph graph     = reasoner.bindSchema(schema.getGraph()).bind(model.getGraph());
         return createModelForGraph(graph);
    }
    
    
    /**
     * <p>
     * Answer a new ontology model which will process ontologies expressed in the given language.
     * The default (global) document manager
     * will be used to load the ontology's included documents.
     * </p>
     * 
     * @param languageURI A URI denoting the ontology language that will be used in this model
     * @return An empty ontology model
     * @see ProfileRegistry
     */
    public static OntModel createOntologyModel( String languageURI ) {
        return createOntologyModel( languageURI, null, null, null );
    }
    
    
    /**
     * <p>
     * Answer a new ontology model which will process ontologies expressed in the given language,
     * starting with the ontology data in the given model. The default (global) document manager
     * will be used to load the ontology's included documents.
     * </p>
     * 
     * @param languageURI A URI denoting the ontology language that will be used in this model
     * @param model An existing model to treat as an ontology model
     * @return An ontology model containing the statements in <code>model</code>
     * @see ProfileRegistry
     */
    public static OntModel createOntologyModel( String languageURI, Model model ) {
        return createOntologyModel( languageURI, model, null, null ); 
    }
    
    
    /**
     * <p>
     * Answer a new ontology model which will process ontologies expressed in the given language,
     * starting with the ontology data in the given model.
     * </p>
     * 
     * @param languageURI A URI denoting the ontology language that will be used in this model
     * @param model An existing model to treat as an ontology model, or null
     * @param docMgr A document manager to use to load the imports closure of the ontology (if desired)
     * @return An ontology model containing the statements in <code>model</code>
     * @see ProfileRegistry
     */
    public static OntModel createOntologyModel( String languageURI, Model model, OntDocumentManager docMgr ) {
        return createOntologyModel( languageURI, model, docMgr, null );
    }

    /**
     * <p>
     * Answer a new ontology model which will process ontologies expressed in the given language,
     * starting with the ontology data in the given model.
     * </p>
     * 
     * @param languageURI A URI denoting the ontology language that will be used in this model.
     * @param model An existing model to treat as an ontology model, or null.
     * @param docMgr A document manager to use to load the imports closure of the ontology (if desired), or null.
     * @param graphFactory A factory for accessing the graph that imported ontologies will be added to, or null.
     * @return An ontology model containing the statements in <code>model</code>, if any.
     * @see ProfileRegistry
     * @exception IllegalArgumentException if languageURI is null
     */
    public static OntModel createOntologyModel( String languageURI, Model model, OntDocumentManager docMgr, GraphMaker graphFactory ) {
        if (languageURI == null) {
            throw new IllegalArgumentException( "Cannot create an ontology model with a null languageURI" );
        }
        
        // ensure we have all the helpers we need, getting defaults if necessary
        OntDocumentManager dm = (docMgr == null) ? OntDocumentManager.getInstance() : docMgr;
        GraphMaker gf = (graphFactory == null) ? dm.getDefaultGraphFactory() : graphFactory;
        Model m = (model == null) ? createModelForGraph( gf.getGraph() ) : model;
         
        return new OntModelImpl( languageURI, m, dm, gf );
    }
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