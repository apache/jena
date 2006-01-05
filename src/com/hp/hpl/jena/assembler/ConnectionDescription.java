/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ConnectionDescription.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ConnectionDescription
    {
    public final String dbURL;
    public final String dbUser;
    public final String dbPassword;
    public final String dbType;
    
    protected IDBConnection connection;
    
    public ConnectionDescription( String dbURL, String dbUser, String dbPassword, String dbType )
        {
        this.dbURL = dbURL;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbType = dbType;
        }

    public IDBConnection getConnection()
        {
        if (connection == null) 
            connection = ModelFactory.createSimpleRDBConnection
                ( dbURL, dbUser, dbPassword, dbType );
        return connection;
        }
    
    public static ConnectionDescription create( String dbURL, String dbUser, String dbPassword, String dbType )
        { return new ConnectionDescription( dbURL, dbUser, dbPassword, dbType ); }
    
    public String toString()
        { 
        return
            "UrConnection("
            + " url=" + dbURL
            + " type=" + dbType
            + " user=" + dbUser
            + " password=" + dbPassword
            + (connection == null ? " unopened" : " opened")
            + ")";
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