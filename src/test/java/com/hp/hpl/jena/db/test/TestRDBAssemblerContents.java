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
        protected final Assembler assistentGeneral;
        protected final Map<Resource, Object> map = new HashMap<Resource, Object>();
        
        public AssistantAssembler( Assembler general )
            { this.assistentGeneral = general; }
        
        public AssistantAssembler with( Resource name, Object value )
            {
            map.put( name, value );
            return this;
            }
        
        @Override
        public Object open( Assembler a, Resource root, Mode mode )
            {
            Object fromMap = map.get( root );
            return fromMap == null ? assistentGeneral.open( a, root, mode ) : fromMap;
            }
    
        }
    }
