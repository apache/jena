/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelSpecCreatorRegistry.java,v 1.9 2005-02-16 13:39:08 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
    The registry of creators appropriate for different ModelSpec types; it maps RDF types
    (subtypes of jms:ModelSpec) to ModelSpecCreators which, when requested, will
    deliver ModelSpecs.
    
 	@author hedgehog
*/
public class ModelSpecCreatorRegistry
    {
    protected Map creators = new HashMap();     
    
    public static final ModelSpecCreatorRegistry instance = 
        new ModelSpecCreatorRegistry( "etc/modelspec-config.n3", true );
    
    public ModelSpecCreatorRegistry()
        {}
    
    public ModelSpecCreatorRegistry( String name )
        { this( name, false ); }
    
    public ModelSpecCreatorRegistry( String name, boolean ignoreMissingModel )
        { this();
        addFromModelNamed( name, ignoreMissingModel ); }

    /**
        Answer a registry with a single entry, mapping <code>type</code>
        to <code>c</code>.
    */
    public static ModelSpecCreatorRegistry registryWith( Resource type, ModelSpecCreator c )
        {
        ModelSpecCreatorRegistry result = new ModelSpecCreatorRegistry();
        result.registerCreator( type, c );
        return result;
        }
    
    public ModelSpecCreator getCreator( Resource type )
        { return (ModelSpecCreator) creators.get( type ); }
    
    public static ModelSpecCreator findCreator( Resource type )    
        { return instance.getCreator( type ); }
    
    public void registerCreator( Resource type, ModelSpecCreator c )
        { creators.put( type, c ); }
    
    public static void register( Resource type, ModelSpecCreator c )
        { instance.registerCreator( type, c ); }
        
    /**
        Add registry entries (R, L) from those statements of the model
        located by <code>name</code> which are (R, jms:typeCreatedBy, L).
        
        <p>if ignoreMissingModel is true, a missing model is ignored,
        rather than raising an exception.
    */
    protected void addFromModelNamed( String name, boolean ignoreMissingModel )
        { 
        try
            {
            Model m = FileManager.get().loadModel( name ); 
            StmtIterator it = m.listStatements( null, JMS.typeCreatedBy, (RDFNode) null ); 
            while (it.hasNext()) addFromStatement( it.nextStatement() );
            }
        catch (NotFoundException e)
            { if (!ignoreMissingModel) throw e; }
        }

    /**
     	Add a registery entry whose key is the resource R and whose creator
        is the class named by L from the statement s=(R, jms.typecreatedBy, L).
    */
    protected void addFromStatement( Statement s )
        {
        registerCreator( s.getSubject(), new ModelSpecCreatorByClassname( s.getString() ) );
        }
    
    static class InfSpecCreator implements ModelSpecCreator
        {
        public ModelSpec create( Resource root, Model desc ) 
            { return new InfModelSpec( root, desc ); }     
        }
        
    static class PlainSpecCreator implements ModelSpecCreator
        {
        public ModelSpec create( Resource root, Model desc ) 
            { return new PlainModelSpec( root, desc ); }      
        }
            
    static class OntSpecCreator implements ModelSpecCreator
        {
        public ModelSpec create( Resource root, Model desc ) 
            { return new OntModelSpec( root, desc ); }     
        }
                        
    static
        {
        register( JMS.InfModelSpec, new InfSpecCreator() );  
        register( JMS.OntModelSpec, new OntSpecCreator() );  
        register( JMS.PlainModelSpec, new PlainSpecCreator() );   
        
        register( JMS.DefaultModelSpec, new PlainSpecCreator() );   
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