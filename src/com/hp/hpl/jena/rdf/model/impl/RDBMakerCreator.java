/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: RDBMakerCreator.java,v 1.4 2005-02-21 12:14:48 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    An RDBMakerCreator makes an RDBModelMaker from its RDF description.
    This code probably belongs in the jena.db package.
     
 	@author hedgehog
 */

public class RDBMakerCreator implements ModelMakerCreator
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

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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