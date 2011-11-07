/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        
        @param subject the URI of the subject node from which this description is created
        @param dbURL the URL of the database to connect to
        @param dbUser the name of the user authorising the connection
        @param dbPassword the password of that user
        @param dbType the type of the database
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
    @Override
    public String toString()
        { 
        return
            "UrlConnection ("
            + "subject=" + subject
            + " url=" + dbURL
            + " type=" + dbType
            + " user=" + dbUser
            + " password=" + dbPassword
            + (connection == null ? " unopened" : " opened")
            + ")";
        }

    }
