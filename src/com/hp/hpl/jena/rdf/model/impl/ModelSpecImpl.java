/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelSpecImpl.java,v 1.3 2003-08-20 13:02:12 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/**
 	@author kers
*/
public abstract class ModelSpecImpl implements ModelSpec
    {
    public ModelSpecImpl()
        {}

    public abstract Model createModel();
    
    public Model getDescription() 
        { return getDescription( ResourceFactory.createResource() ); }
        
    public Model getDescription( Resource root ) 
        { return addDescription( ModelFactory.createDefaultModel(), root ); }

    public abstract Model addDescription( Model desc, Resource root );
            
    private static Map values = new HashMap();
    
    public static Resource createValue( Object value )
        {
        Resource it = ResourceFactory.createResource();
        values.put( it, value );
        return it;    
        }
        
    public static Object getValue( RDFNode it )
        { return values.get( it ); }
        
    public static Reifier.Style findStyle( RDFNode style )
        {
        if (style.equals(JMS.rsStandard )) return Reifier.Standard;    
        if (style.equals(JMS.rsMinimal)) return Reifier.Minimal;    
        if (style.equals( JMS.rsConvenient)) return Reifier.Convenient;
        return null;
        }

    protected static Resource subjectTyped( Model m, Resource type )
        { return m.listSubjectsWithProperty( RDF.type, type ).nextResource(); }
        
    static final public Model schema = ModelFactory.createDefaultModel()
        .add( JMS.MemMakerClass, RDFS.subClassOf, JMS.MakerClass )
        .add( JMS.FileMakerClass, RDFS.subClassOf, JMS.MakerClass )
        .add( JMS.ontLanguage, RDFS.domain, JMS.OntMakerClass )
        ;
        
    public static Resource findRootByType( Model description, Resource r )
        { 
        Model d = ModelFactory.createRDFSModel( schema, description );
        ResIterator rs  = d.listSubjectsWithProperty( RDF.type, r );
        if (rs.hasNext()) return rs.nextResource();
        throw new JenaException( "no " + r + " thing found" );
        }
    
    public static ModelMaker createMaker( Model d )
        {
        Model description = ModelFactory.createRDFSModel( schema, d );
        Resource root = subjectTyped( description, JMS.MakerClass );
        Reifier.Style style = Reifier.Standard;
        Statement st = description.getProperty( root, JMS.reificationMode );
        if (st != null) style = findStyle( st.getObject() );
        if (description.listStatements( null, RDF.type, JMS.FileMakerClass ).hasNext())
            {
            Statement fb = description.getProperty( root, JMS.fileBase );
            String fileBase = fb == null ? "/tmp" : fb.getString();
            return ModelFactory.createFileModelMaker( fileBase, style );
            }
        if (description.listStatements( null, RDF.type, JMS.MemMakerClass ).hasNext())
            return ModelFactory.createMemModelMaker( style );
        throw new RuntimeException( "no maker type" );    
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