/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ConnectionDescription.java,v 1.3 2006-11-16 14:44:45 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

/**
    A ConnectionDescription holds the information required to construct an
    IDBConnection, and can construct that connection on demand.

    @author kers
*/
public class ConnectionDescription
    {
    public final String subject;
    public final String dbURL;
    public final String dbUser;
    public final String dbPassword;
    public final String dbType;
    
    protected IDBConnection connection;
    
    /**
        Initialise a description from the given arguments.
        
        @param dbURL the URL of the database to connect to
        @param dbUser the name of the user authorising the connection
        @param dbPassword the password of that user
        @param dbType the type of the database
     * @param string 
    */
    public ConnectionDescription( String subject, String dbURL, String dbUser, String dbPassword, String dbType )
        {
        this.subject = subject;
        this.dbURL = dbURL;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbType = dbType;
        }

    /**
        Answer the connection specified by the description. Once created, that same
        connection is returned for each call to getConnection.
    */
    public IDBConnection getConnection()
        {
        if (connection == null) 
            {
            if (dbURL == null || dbType == null) 
                throw new JenaException( "this connection " + this + " cannot be opened because no dbURL or dbType was specified" );
            connection = ModelFactory.createSimpleRDBConnection
                ( dbURL, dbUser, dbPassword, dbType );
            }
        return connection;
        }
    
    /**
        Answer a description containing the specified components.
    */
    public static ConnectionDescription create( String subject, String dbURL, String dbUser, String dbPassword, String dbType )
        { return new ConnectionDescription( subject, dbURL, dbUser, dbPassword, dbType ); }
    
    /**
        Answer a descriptive string for this connection object, including whether or
        not it has already had its connection opened. 
        @see java.lang.Object#toString()
    */
    public String toString()
        { 
        return
            "UrlConnection("
            + " subject=" + subject
            + " url=" + dbURL
            + " type=" + dbType
            + " user=" + dbUser
            + " password=" + dbPassword
            + (connection == null ? " unopened" : " opened")
            + ")";
        }

    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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