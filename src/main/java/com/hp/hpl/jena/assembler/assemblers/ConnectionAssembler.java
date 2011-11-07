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

package com.hp.hpl.jena.assembler.assemblers;

import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    A ConnectionAssembler assembles a ConnectionDescription object which
    contains a database URL, user name, user password, and database type.
    Some of the components may have been specified in advance when the 
    Assembler was constructed. The ConnectionAssembler will also load any
    classes specified by dbClass[Property] statements of the root.
    
    @author kers
*/
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

    @Override
    public Object open( Assembler a, Resource root, Mode irrelevant )
        {
        checkType( root, JA.Connection );
        String dbUser = getUser( root ), dbPassword = getPassword( root );
        String dbURL = getURL( root ), dbType = getType( root );
        loadClasses( root );
        return createConnection( root.getURI(), dbURL, dbType, dbUser, dbPassword );
        }    
    
    /**
        Load all the classes that are named by the object of dbClass statements
        of <code>root</code>. Load all the classes named by the contents of
        system properties which are the objects of dbClassProperty statements
        of <code>root</code>.
    */
    private void loadClasses( Resource root )
        {
        for (StmtIterator it = root.listProperties( JA.dbClassProperty ); it.hasNext();)
            {
            String propertyName = getString( it.nextStatement() );
            String className = JenaRuntime.getSystemProperty( propertyName );
            loadClass( root, className );
            }
        for (StmtIterator it = root.listProperties( JA.dbClass ); it.hasNext();)
            {
            String className = getString( it.nextStatement() );
            loadClass( root, className );
            }
        }

    protected ConnectionDescription createConnection
        ( String subject, String dbURL, String dbType, String dbUser, String dbPassword )
        { return ConnectionDescription.create( subject, dbURL, dbUser, dbPassword, dbType ); }
    
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
        return name == null ? ifAbsent : JenaRuntime.getSystemProperty( name.getLexicalForm() ); 
        }
    }
