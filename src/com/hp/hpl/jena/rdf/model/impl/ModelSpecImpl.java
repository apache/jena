/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelSpecImpl.java,v 1.62 2007-02-12 15:51:21 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;
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
    @deprecated ModelSpecs are dead
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
    
    protected Model defaultModel = null;
    
    public static final Resource emptyResource = emptyModel.createResource();
    
    protected Model description = emptyModel;
    
    protected Resource root = ResourceFactory.createResource( "" );

    protected static final RDFNode nullObject = (RDFNode) null;
        
    /**
        Answer a Model created according to this ModelSpec, with any required
        files loaded into it.
    */
    public final Model createFreshModel()
        { return loadFiles( doCreateModel() ); }
    
    /**
        Answer a Model created according to this ModelSpec; subclasses must 
        implement. The resulting model is returned by <code>createModel</code>
        after loading any files specified by jms:loadFile properties.
    */
    protected abstract Model doCreateModel();
    
    public Model createDefaultModel() 
        { if (defaultModel == null) defaultModel = makeDefaultModel();
        return defaultModel; }
    
    protected Model makeDefaultModel()
        {
        Statement s = root.getProperty( JenaModelSpec.modelName );
        return loadFiles( s == null ? maker.createDefaultModel() : maker.createModel( s.getString() ) );
        }
    
    /**
        Answer a Model created according to this ModelSpec and based on an underlying
        Model with the given name.
         
     	@see com.hp.hpl.jena.rdf.model.ModelSpec#createModelOver(java.lang.String)
     */
    public Model createModelOver( String name )
        { return loadFiles( implementCreateModelOver( name ) ); }
    
    public abstract Model implementCreateModelOver( String name );
    
    /**
        Answer the JenaModelSpec subproperty of JenaModelSpec.maker that describes the relationship 
        between this specification and its ModelMaker.
        
        @return a sub-property of JenaModelSpec.maker
    */
    public abstract Property getMakerProperty();
   
    /**
     	Answer a Model, as per the specification of ModelSpec; appeal to 
        the sibling Maker.
    */
    public Model openModel( String name )
        { return loadFiles( maker.openModel( name ) ); }
    
    public Model openModel()
        {
        Statement s = root.getProperty( JenaModelSpec.modelName );
        return loadFiles( s == null ? maker.openModel() : maker.openModel( s.getString(), true ) );
        }
    
    /**
        Answer the model hidden in the sibling maker, if it has one, and
        null otherwise.
    */
    public Model openModelIfPresent( String name )
        { return maker.hasModel( name ) ? loadFiles( maker.openModel( name ) ) : null; }
        
    public static Resource getMaker( Resource root, Model desc )
        {
        StmtIterator it = desc.listStatements( root, JenaModelSpec.maker, (RDFNode) null );
        if (it.hasNext())
        	return it.nextStatement().getResource();
        else 
            {
            Resource r = desc.createResource();
            desc.add( root, JenaModelSpec.maker, r );
            return r;
            }
        }
        
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
        desc.add( root, JenaModelSpec.maker, makerRoot );
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
        { 
        Model m = ModelSpecImpl.withSpecSchema( description );
        StmtIterator it = m.listStatements( null, RDF.type, type );
        if (!it.hasNext()) throw new BadDescriptionNoRootException( m, type );
        Resource root = it.nextStatement().getSubject();
        if (it.hasNext()) throw new BadDescriptionMultipleRootsException( m, type );
        return root; 
        }
        
    public static ModelMaker createMaker( Resource root, Model d )
        { return createMakerByRoot( root, ModelSpecImpl.withSpecSchema( d ) ); }
        
    public static ModelMaker createMakerByRoot( Resource root, Model fullDesc )
        { Resource type = findSpecificType( (Resource) root.inModel( fullDesc ), JenaModelSpec.MakerSpec );
        ModelMakerCreator mmc = ModelMakerCreatorRegistry.findCreator( type );
        if (mmc == null) throw new RuntimeException( "no maker type" );  
        return mmc.create( fullDesc, root ); }

    public static Model withSpecSchema( Model m )
        {
        return withSchema( m, JenaModelSpec.getSchema() );
        }

    /**
        answer a limited RDFS closure of <code>m union schema</code>.
    */
    public static Model withSchema( Model m, Model schema )
        {
        Model result = ModelFactory.createDefaultModel();
        result.add( m );
        addJMSSubclassesFrom( result, schema );        
        addDomainTypes( result, m, schema );
        addSupertypesFrom( result, schema );
        addSupertypesFrom( result, m );
        return result;
        }

    protected Model loadFiles( Model m )
        {
        StmtIterator it = description.listStatements( root, JenaModelSpec.loadWith, (RDFNode) null );
        while (it.hasNext()) loadFile( m, it.nextStatement().getResource() );
        return m;
        }

    protected Model loadFile( Model m, Resource file )
        { FileManager.get().readModel( m, file.getURI() ); 
        return m; }
    
    /**
        @deprecated 
        @see com.hp.hpl.jena.rdf.model.ModelSource#getModel()
    */
    public Model getModel()
        { return createDefaultModel(); }
    
    /**
        @deprecated 
        @see com.hp.hpl.jena.rdf.model.ModelSource#createModel()
     */
    public Model createModel()
        { return createFreshModel(); }
    
    public Model getModel( String URL )
        { return null; }
    
    public Model getModel( String URL, ModelReader loadIfAbsent )
        { throw new CannotCreateException( URL ); }

    /**
        Answer the "most specific" type of root in desc which is an instance of type.
        We assume a single inheritance thread starting with that type. The model
        should contain the subclass closure (ie either be complete, or an inference
        model which will generate completeness).
        
        @param root the subject whose type is to be found
        @param type the base type for the search
        @return T such that (root type T) and if (root type T') then (T' subclassof T)
    */
    public static Resource findSpecificType( Resource root, Resource type )
        { StmtIterator it = root.listProperties( RDF.type );
        Model desc = root.getModel();
        while (it.hasNext())
            { Resource candidate = it.nextStatement().getResource();
            if (desc.contains( candidate, RDFS.subClassOf, type )) type = candidate; }
        return type; }

    protected static boolean notRDF( Resource resource )
        {
        if (resource.isAnon()) return true;
        if (resource.getNameSpace().equals( RDF.getURI() )) return false;
        if (resource.getNameSpace().equals( RDFS.getURI() )) return false;
        return true;
        }

    protected static void addJMSSubclassesFrom( Model result, Model schema )
        {
        for (StmtIterator it = schema.listStatements( null, RDFS.subClassOf, nullObject ); it.hasNext();)
            { 
            Statement s = it.nextStatement();
            if (ModelSpecImpl.notRDF( s.getSubject() ) && ModelSpecImpl.notRDF( s.getResource() )) result.add( s ); 
            }
        }

    protected static void addDomainTypes( Model result, Model m, Model schema )
        {
        for (StmtIterator it = schema.listStatements( null, RDFS.domain, nullObject ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            Property property = (Property) s.getSubject().as( Property.class );
            for (StmtIterator x = m.listStatements( null, property, nullObject ); x.hasNext();)
                {
                Statement t = x.nextStatement();
                // System.err.println( ">> adding domain type: subject " + t.getSubject() + ", type " + s.getObject() + " because of property " + property );
                result.add( t.getSubject(), RDF.type, s.getObject() );
                }
            }
        }

    protected static void addSupertypesFrom( Model result, Model source )
        {
        Model temp = ModelFactory.createDefaultModel();
        for (StmtIterator it = result.listStatements( null, RDF.type, nullObject ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            for (StmtIterator subclasses = source.listStatements( s.getResource(), RDFS.subClassOf, nullObject ); subclasses.hasNext();)
                {
                RDFNode type = subclasses.nextStatement().getObject();
                // System.err.println( ">> adding super type: subject " + s.getSubject() + ", type " + type );
                temp.add( s.getSubject(), RDF.type, type );
                }
            }
        result.add( temp );
        }
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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