/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ConnectionAssembler.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.CannotLoadClassException;
import com.hp.hpl.jena.rdf.model.*;

public class ConnectionAssembler extends AssemblerBase implements Assembler
    {
    public final String defaultURL;
    public final String defaultUser;
    public final String defaultPassword;
    public final String defaultType;
    
    protected static final Resource emptyRoot = ModelFactory.createDefaultModel().createResource();
    
    public ConnectionAssembler( Resource init )
        {
        defaultUser = get( init, "dbUser", null );
        defaultPassword = get( init, "dbPassword", null );
        defaultURL = get( init, "dbURL", null );
        defaultType = get( init, "dbType", null );
        }
    
    public ConnectionAssembler()
        { this( emptyRoot ); }

    public Object create( Assembler a, Resource root )
        {
        checkType( root, JA.Connection );
        String dbUser = getUser( root ), dbPassword = getPassword( root );
        String dbURL = getURL( root ), dbType = getType( root );
        loadClasses( root );
        return createConnection( dbURL, dbType, dbUser, dbPassword );
        }    
    
    private void loadClasses( Resource root )
        {
        for (StmtIterator it = root.listProperties( JA.dbClass ); it.hasNext();)
            {
            String className = it.nextStatement().getString();
            try { Class.forName( className ); }
            catch (ClassNotFoundException e)
                { throw new CannotLoadClassException( root, className, e ); }
            }
        }

    protected ConnectionDescription createConnection
        ( String dbURL, String dbType, String dbUser, String dbPassword )
        { return ConnectionDescription.create( dbURL, dbUser, dbPassword, dbType ); }
    
    public String getUser( Resource root )
        { return get( root, "dbUser", defaultUser ); }

    public String getPassword( Resource root )
        { return get( root, "dbPassword", defaultPassword ); }

    public String getURL( Resource root )
        { return get( root, "dbURL", defaultURL );  }

    public String getType( Resource root )
        { return get( root, "dbType", defaultType ); }    
    
    protected String get( Resource root, String label, String ifAbsent )
        {
        Property property = JA.property( label );
        RDFNode L = getUnique( root, property );
        return 
            L == null ? getIndirect( root, label, ifAbsent ) 
            : L.isLiteral() ? ((Literal) L).getLexicalForm()
            : ((Resource) L).getURI()
            ;
        }

    private String getIndirect( Resource root, String label, String ifAbsent )
        {
        Property property = JA.property( label + "Property" );
        Literal name = getUniqueLiteral( root, property );
        return name == null ? ifAbsent : System.getProperty( name.getLexicalForm() ); 
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/