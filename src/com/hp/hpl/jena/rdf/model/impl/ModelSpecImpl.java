/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelSpecImpl.java,v 1.8 2003-08-24 16:23:33 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.ontology.*;

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
        Initialise this ModelSpec with the supplied ModeMaker; if it is null, fabricate a
        MemModelMaker anyway.
        
        @param maker the ModelMaker to use, or null to create a fresh one
    */
    public ModelSpecImpl( ModelMaker maker )
        { this.maker = this.maker = maker == null ? ModelFactory.createMemModelMaker(): maker; }
        
    /**
        Initialise this ModelSpec from the supplied description, which is used to construct
        a ModelMaker. 
        
        @description an RDF description including that of the necessary ModelMaker
    */
    public ModelSpecImpl( Model description )
        { this( createMaker( description ) ); }

    /**
        Answer a Model created according to this ModelSpec; left abstract for subclasses
        to implement.
    */
    public abstract Model createModel();
    
    /**
        Answer the JMS subproperty of JMS.maker that describes the relationship 
        between this specification and its ModelMaker.
        
        @return a sub-property of JMS.maker
    */
    public abstract Property getMakerProperty();
    
    /**
        Answer a new ModelSpec created according to the supplied RDF description.
        <i>in progress</i>.
    */
    public static ModelSpec create( Model desc )
        {
        Model d = ModelFactory.createRDFSModel( JMS.schema, desc );
        Resource r = findRootByType( d, JMS.MakerSpec );
        if (d.listStatements( null, RDF.type, JMS.OntModelSpec ).hasNext())
            {
            return new OntModelSpec( desc );
            }
        if (d.listStatements( null, RDF.type, JMS.InfModelSpec).hasNext())
            return new InfModelSpec( desc );
        if (d.listStatements( null, RDF.type, JMS.MemMakerSpec).hasNext())
            return new PlainModelSpec( desc );
        throw new BadDescriptionException( "neither ont nor inf nor mem", desc );
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
        desc.add( root, getMakerProperty(), makerRoot );
        maker.addDescription( desc, makerRoot );
        return desc;
        }

    /**
        Answer a new bnode Resource associated with the given value. The mapping from
        bnode to value is held in a single static table, and is not intended to hold many
        objects; there is no provision for garbage-collecting them [this might eventually be
        regarded as a bug].
        
        @param value a Java value to be remembered 
        @answer a fresh bnode bound to <code>value</code>
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
        @exception SomeException[s] if there's not exactly one subject
    */        
    public static Resource findRootByType( Model description, Resource r )
        { 
        Model d = ModelFactory.createRDFSModel( JMS.schema, description );
        ResIterator rs  = d.listSubjectsWithProperty( RDF.type, r );
        if (rs.hasNext()) return rs.nextResource();
        throw new BadDescriptionException( "no " + r + " thing found", description );
        }
    
    /**
        Answer a ModelMaker that conforms to the supplied description.
        <i>work in progress</i>.
    */
    public static ModelMaker createMaker( Model d )
        {
        Model description = ModelFactory.createRDFSModel( JMS.schema, d );
        Resource root = findRootByType( description, JMS.MakerSpec );
        Reifier.Style style = Reifier.Standard;
        Statement st = description.getProperty( root, JMS.reificationMode );
        if (st != null) style = JMS.findStyle( st.getObject() );
        if (description.listStatements( null, RDF.type, JMS.RDBMakerSpec ).hasNext())
            return ModelFactory.createModelRDBMaker( createConnection( description ) );
        if (description.listStatements( null, RDF.type, JMS.FileMakerSpec ).hasNext())
            {
            Statement fb = description.getProperty( root, JMS.fileBase );
            String fileBase = fb == null ? "/tmp" : fb.getString();
            return ModelFactory.createFileModelMaker( fileBase, style );
            }
        if (description.listStatements( null, RDF.type, JMS.MemMakerSpec ).hasNext())
            return ModelFactory.createMemModelMaker( style );
        throw new RuntimeException( "no maker type" );    
        }
    
    public static IDBConnection createConnection( Model description )
        {
        Resource root = findRootByType( description, JMS.RDBMakerSpec );
        String url = getString( description, root, JMS.dbURL );
        String user = getString( description, root, JMS.dbUser );
        String password = getString( description, root , JMS.dbPassword );
        String className = getClassName( description, root );
        String dbType = getString( description, root, JMS.dbType );
        loadDrivers( dbType, className );
        return ModelFactory.createSimpleRDBConnection( url, user, password, dbType );    
        }
    
    public static void loadDrivers( String dbType, String className )
        {
        try
            {   
            Class.forName( "com.hp.hpl.jena.db.impl.Driver_" + dbType );
            if (className != null) Class.forName( className );
            }
        catch (ClassNotFoundException c)
            { throw new JenaException( c ); }
        }   
                 
    public static String getClassName( Model description, Resource root )
        {
        Statement cnStatement = description.getProperty( root, JMS.dbClass );
        return cnStatement == null ? null : cnStatement.getString();
        }                            
        
    public static String getString( Model description, Resource root, Property p )
        {
        return description.getRequiredProperty( root, p ).getString();  
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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