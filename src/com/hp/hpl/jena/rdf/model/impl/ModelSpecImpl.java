/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelSpecImpl.java,v 1.45 2005-02-14 15:24:52 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/**
    An abstract base class for implementations of ModelSpec. It provides the base 
    functionality of providing a ModelMaker (different sub-classes use this for different
    purposes) and utility methods for reading and creating RDF descriptions. It also
    provides a value table associating freshly-constructed bnodes with arbitrary Java
    values, so program-constructed specifications can pass on database connections,
    actual document managers, and so forth.
    
 	@author kers
*/
public abstract class ModelSpecImpl implements ModelSpec
    {
    /**
        The ModelMaker that may be used by sub-classes.
    */
    protected ModelMaker maker;
    
    /**
        The map which associates bnodes with Java values.
    */        
    private static Map values = new HashMap();
    
    /**
        Initialise this ModelSpec with the supplied non-nullModeMaker.
        
        @param maker the ModelMaker to use, or null to create a fresh one
    */
    public ModelSpecImpl( ModelMaker maker )
        {
        if (maker == null) throw new RuntimeException( "null maker not allowed" );
        this.maker = maker; 
        }
        
    public ModelSpecImpl( Resource root, Model description )
        { 
        this( createMaker( getMaker( root, description ), description ) );
        this.root = root;
        this.description = description; 
        }
    
    public static final Model emptyModel = ModelFactory.createDefaultModel();
    
    public static final Model defaultModel = ModelFactory.createDefaultModel();
    
    public static final Resource emptyResource = emptyModel.createResource();
    
    protected Model description = emptyModel;
    
    protected Resource root = ResourceFactory.createResource( "" );
        
    /**
        Answer a Model created according to this ModelSpec; left abstract for subclasses
        to implement.
    */
    public abstract Model createModel();
    
    public Model getModel() 
        { return defaultModel; }
    
    /**
        Answer a Model created according to this ModelSpec and based on an underlying
        Model with the given name.
         
     	@see com.hp.hpl.jena.rdf.model.ModelSpec#createModelOver(java.lang.String)
     */
    public abstract Model createModelOver( String name );
    
    /**
        Answer the JMS subproperty of JMS.maker that describes the relationship 
        between this specification and its ModelMaker.
        
        @return a sub-property of JMS.maker
    */
    public abstract Property getMakerProperty();
    
    /**
        Answer a ModelSpec created from the RDF model to be found using the URI of
        the (non-bnode) Resource.
        
        see com.hp.hpl.jena.rdf.model.impl.createByRoot
     	@param desc the resource who's URI names the model to read
     	@return a ModelSpec as described by the resource
    */
    public static ModelSpec create( Resource desc )
        { return create( readModel( desc ) ); }
    
    /*
     	Answer a Model, as per the specification of ModelSource; if the name
     	is useful, use it, otherwise don't bother. Default implementation is
     	to return a fresh model.
    */
    public Model openModel( String URI )
        { return ModelFactory.createDefaultModel(); }
    
    /**
     	Answer null, as ModelSpecs as ModelSources don't remember any Models.
     	This is consistent with openModel() always creating a new Model.
    */
    public Model openModelIfPresent( String URI )
        { return null; }
        
    /**
        Answer a ModelSpec created from the RDF model to be found using the URI of
        the (non-bnode) Resource and starting from the given root.
        
        see com.hp.hpl.jena.rdf.model.impl.createByRoot
        @param root the root of the description within the model
        @param desc the resource who's URI names the model to read
        @return a ModelSpec as described by the resource
    */        
    public static ModelSpec create( Resource root, Resource desc )
        { return ModelSpecFactory.createSpec( ModelSpecFactory.withSchema( readModel( desc ) ), root ); }
        
    /**
        see com.hp.hpl.jena.rdf.model.impl.createByRoot
        @param desc a model containing a JMS description
        @return a ModelSpec fitting that description
    */
    public static ModelSpec create( Model desc )
        { Model d = ModelSpecFactory.withSchema( desc );
        return ModelSpecFactory.createSpec( d, findRootByType( d, JMS.ModelSpec ) ); }
        
    public static Resource getMaker( Resource root, Model desc )
        {
        StmtIterator it = desc.listStatements( root, JMS.maker, (RDFNode) null );
        if (it.hasNext())
        	return it.nextStatement().getResource();
        else 
            {
            Resource r = desc.createResource();
            desc.add( root, JMS.maker, r );
            return r;
            // throw new BadDescriptionException( "no jms:maker for " + root, desc );
            }
        }
        
    /**
        Answer the "most specific" type of root in desc which is an instance of type.
        We assume a single inheritance thread starting with that type. 
        
    	@param desc the model the search is conducted in
    	@param root the subject whos type is to be found
    	@param type the base type for the search
    	@return T such that (root type T) and if (root type T') then (T' subclassof T)
    */
    static Resource findSpecificType( Model desc, Resource root, Resource type )
        { return ModelSpecFactory.findSpecificType( (Resource) root.inModel( desc ), type ); }
        
    /**
        Answer the ModelMaker that this ModelSpec uses.
        @return the embedded ModelMaker
    */
    public ModelMaker getModelMaker()
        { return maker; }
        
    public Model getDescription() 
        { return getDescription( ResourceFactory.createResource() ); }
        
    public Model getDescription( Resource root ) 
        { return addDescription( ModelFactory.createDefaultModel(), root ); }

    public Model addDescription( Model desc, Resource root )
        {
        Resource makerRoot = desc.createResource();
        desc.add( root, JMS.maker, makerRoot );
        maker.addDescription( desc, makerRoot );
        return desc;
        }
        
    /**
        Answer a new bnode Resource associated with the given value. The mapping from
        bnode to value is held in a single static table, and is not intended to hold many
        objects; there is no provision for garbage-collecting them [this might eventually be
        regarded as a bug].
        
        @param value a Java value to be remembered 
        @return a fresh bnode bound to <code>value</code>
    */
    public static Resource createValue( Object value )
        {
        Resource it = ResourceFactory.createResource();
        values.put( it, value );
        return it;    
        }
        
    /**
        Answer the value bound to the supplied bnode, or null if there isn't one or the
        argument isn't a bnode.
        
        @param it the RDF node to be looked up in the <code>createValue</code> table.
        @return the associated value, or null if there isn't one.
    */
    public static Object getValue( RDFNode it )
        { return values.get( it ); }
        
    /**
        Answer the unique subject with the given rdf:type.
        
        @param m the model in which the typed subject is sought
        @param type the RDF type the subject must have
        @return the unique S such that (S rdf:type type)
        @exception BadDescriptionException if there's not exactly one subject
    */        
    public static Resource findRootByType( Model description, Resource type )
        { return ModelSpecFactory.findRootByType( ModelSpecFactory.withSchema( description ), type ); }
    
    /**
        Answer a ModelMaker that conforms to the supplied description. The Maker
        is found from the ModelMakerCreatorRegistry by looking up the most 
        specific type of the unique object with type JMS.MakerSpec.
        
        @param d the model containing the description
        @return a ModelMaker fitting that description
    */
    public static ModelMaker createMaker( Model description )
        { Model d = ModelSpecFactory.withSchema( description );
        return createMakerByRoot( findRootByType( d, JMS.MakerSpec ), d ); }
        
    public static ModelMaker createMaker( Resource root, Model d )
        { return createMakerByRoot( root, ModelSpecFactory.withSchema( d ) ); }
        
    public static ModelMaker createMakerByRoot( Resource root, Model fullDesc )
        { Resource type = findSpecificType( fullDesc, root, JMS.MakerSpec );
        ModelMakerCreator mmc = ModelMakerCreatorRegistry.findCreator( type );
        if (mmc == null) throw new RuntimeException( "no maker type" );  
        return mmc.create( fullDesc, root ); }
        
    /**
        Read a model from a given URI.
     	@param source the resource who's URI specifies what to laod
     	@return the model as loaded from the resource URI
     */
    public static Model readModel( Resource source )
        {
        String uri = source.getURI();
        return FileManager.get().loadModel( uri );
        }

    protected Model loadFiles(Model m)
        {
        StmtIterator it = description.listStatements( root, JMS.loadWith, (RDFNode) null );
        while (it.hasNext()) FileManager.get().readModel( m, it.nextStatement().getResource().getURI() );
        return m;
        }
                
    }

/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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