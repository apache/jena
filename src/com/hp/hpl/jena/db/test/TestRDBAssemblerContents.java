/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestRDBAssemblerContents.java,v 1.1 2008-01-03 15:19:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;

public class TestRDBAssemblerContents extends AssemblerTestBase
    {
    public TestRDBAssemblerContents( String name )
        { super( name ); }

    private static final String url = ModelFactoryBase.guessDBURL();
    private static final String user = ModelFactoryBase.guessDBUser();
    private static final String password = ModelFactoryBase.guessDBPassword();
    private static final String type = ModelFactoryBase.guessDBType();
    private static final String driver = ModelFactoryBase.guessDBDriver();
    
    static
        {
        try { Class.forName( driver ); } catch (Exception e) { throw new RuntimeException( e ); }
        }
    
    public void testCreatesEmptyModel()
        {
        ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
        Resource root = resourceInModel( "db rdf:type ja:RDBModel; db ja:connection C; db ja:modelName 'CreatesEmptyModel'" );
        Assembler ca = new NamedObjectAssembler( resource( "C" ), cd );
        Model assembled = (Model) Assembler.rdbModel.open( ca, root, Mode.ANY );
        assertIsoModels( modelWithStatements( "" ), assembled );
        assembled.close();
        }
    
    public void testCreatesInitialisedModel()
        {
        ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
        Resource root = resourceInModel( "db rdf:type ja:RDBModel; db ja:connection C; db ja:modelName 'CreatesInitialisedModel'; db ja:quotedContent X; X rdf:type T" );
        Assembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
        Model assembled = (Model) Assembler.rdbModel.open( ca, root, Mode.ANY );
        assertIsoModels( modelWithStatements( "X rdf:type T" ), assembled );
        assembled.close();
        }
    
    public void testOpensAndInitialisesModel()
        {
        ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
        Resource root = resourceInModel( "db rdf:type ja:RDBModel; db ja:connection C; db ja:modelName 'OpensAndInitialisesModel'; db ja:quotedContent X; X rdf:type T" );
        Assembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
        Model assembled = (Model) Assembler.rdbModel.open( ca, root, Mode.ANY );
        assertIsoModels( modelWithStatements( "X rdf:type T" ), assembled );
        assembled.close();
        }
    
    public void testCreatesAndInitialisesModel()
        {
        ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
        ensureAbsent( cd, "CreatesAndInitialisesModel" );
        Resource root = resourceInModel( "db rdf:type ja:RDBModel; db ja:connection C; db ja:modelName 'CreatesAndInitialisesModel'; db ja:initialContent Q; Q ja:quotedContent X; X rdf:type T" );
        Assembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
        Model assembled = (Model) Assembler.rdbModel.open( ca, root, Mode.ANY );
        assertIsoModels( modelWithStatements( "X rdf:type T" ), assembled );
        assembled.close();
        }
    
    public void testOpensAndDoesNotInitialiseModel()
        {
        ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
        ensurePresent( cd, "OpensAndDoesNotInitialiseModel" );
        Resource root = resourceInModel( "db rdf:type ja:RDBModel; db ja:connection C; db ja:modelName 'OpensAndDoesNotInitialiseModel'; db ja:initialContent Q; Q ja:quotedContent X; X rdf:type T" );
        Assembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
        Model assembled = (Model) Assembler.rdbModel.open( ca, root, Mode.ANY );
        assertIsoModels( modelWithStatements( "" ), assembled );
        assembled.close();
        }

    private void ensurePresent( ConnectionDescription cd, String modelName )
        {
        IDBConnection ic = cd.getConnection();
        if (!ic.containsModel( modelName )) ModelRDB.createModel( ic, modelName ).close();
        }

    private void ensureAbsent( ConnectionDescription cd, String modelName )
        {
        IDBConnection ic = cd.getConnection();
        if (ic.containsModel( modelName )) ModelRDB.open( ic, modelName ).remove();
        }
    
    static class AssistantAssembler extends AssemblerBase
        {
        protected final Assembler general;
        protected final Map map = new HashMap();
        
        public AssistantAssembler( Assembler general )
            { this.general = general; }
        
        public AssistantAssembler with( Resource name, Object value )
            {
            map.put( name, value );
            return this;
            }
        
        public Object open( Assembler a, Resource root, Mode mode )
            {
            Object fromMap = map.get( root );
            return fromMap == null ? general.open( a, root, mode ) : fromMap;
            }
    
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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