/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelMakerCreatorRegistry.java,v 1.1 2003-08-25 14:08:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecImpl;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.db.IDBConnection;

import java.util.*;

/**
 	@author hedgehog
*/
public class ModelMakerCreatorRegistry
    {
    /**
     	
    */
    public ModelMakerCreatorRegistry()
        {}

    private static Map creators = new HashMap();
    
    public static ModelMakerCreator findCreator( Resource type )
        { return (ModelMakerCreator) creators.get( type ); }
        
    public static void register( Resource type, ModelMakerCreator mmc )
        { creators.put( type, mmc ); }        
        
    static
        {
        register( JMS.FileMakerSpec, new FileMakerCreator() );    
        register( JMS.MemMakerSpec, new MemMakerCreator() );    
        register( JMS.RDBMakerSpec, new RDBMakerCreator() );    
        }
        
    static class MakerCreator
        {
        Reifier.Style style( Model desc, Resource root )
            {
            Reifier.Style style = Reifier.Standard;
            Statement st = desc.getProperty( root, JMS.reificationMode );
            if (st != null) style = JMS.findStyle( st.getObject() );  
            return style; 
            } 
        }
        
    static class FileMakerCreator extends MakerCreator implements ModelMakerCreator
        {
        public ModelMaker create( Model desc, Resource root ) 
            { 
            Statement fb = desc.getProperty( root, JMS.fileBase );
            String fileBase = fb == null ? "/tmp" : fb.getString();
            return ModelFactory.createFileModelMaker( fileBase, style( desc, root ) );
            }
        }
        
    static class MemMakerCreator extends MakerCreator implements ModelMakerCreator
        {
        public ModelMaker create( Model desc, Resource root ) 
            { return ModelFactory.createMemModelMaker( style( desc, root ) ); }
        }
                
    static class RDBMakerCreator implements ModelMakerCreator
        {                     
        public ModelMaker create( Model desc, Resource root ) 
            {
            return ModelFactory.createModelRDBMaker( createConnection( desc ) );
            }
            
        public static IDBConnection createConnection( Model description )
            {
            Resource root = ModelSpecImpl.findRootByType( description, JMS.RDBMakerSpec );
            String url = getString( description, root, JMS.dbURL );
            String user = getString( description, root, JMS.dbUser );
            String password = getString( description, root , JMS.dbPassword );
            String className = getClassName( description, root );
            String dbType = getString( description, root, JMS.dbType );
            loadDrivers( dbType, className );
            return ModelFactory.createSimpleRDBConnection( url, user, password, dbType );    
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