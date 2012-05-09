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

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.rdf.model.*;

public class TestAssemblerVocabulary extends AssemblerTestBase
    {
    public TestAssemblerVocabulary( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return null; }

    public void testVocabulary()
        {
        assertEquals( "http://jena.hpl.hp.com/2005/11/Assembler#", JA.getURI() );
        assertEquals( "http://jena.hpl.hp.com/2005/11/Assembler#", JA.uri );
    //
        assertLocalname( "this", JA.This );
        assertLocalname( "Expanded", JA.Expanded );
    //
        assertLocalname( "Object", JA.Object );
    //
        assertLocalname( "Model", JA.Model );
        assertLocalname( "MemoryModel", JA.MemoryModel );
        assertLocalname( "DefaultModel", JA.DefaultModel );
        assertLocalname( "InfModel", JA.InfModel );
        assertLocalname( "OntModel", JA.OntModel );
        assertLocalname( "NamedModel", JA.NamedModel );
        assertLocalname( "FileModel", JA.FileModel );
        assertLocalname( "OntModel", JA.OntModel );
        assertLocalname( "OntModelSpec", JA.OntModelSpec );
    //
        assertLocalname( "PrefixMapping", JA.PrefixMapping );
        assertLocalname( "SinglePrefixMapping", JA.SinglePrefixMapping );
        assertLocalname( "ReasonerFactory", JA.ReasonerFactory );
        assertLocalname( "Content", JA.Content );
        assertLocalname( "LiteralContent", JA.LiteralContent );
        assertLocalname( "ExternalContent", JA.ExternalContent );
    //
        assertLocalname( "schema", JA.ja_schema );
        assertLocalname( "rules", JA.rules );
        assertLocalname( "reasoner", JA.reasoner );
        assertLocalname( "reasonerURL", JA.reasonerURL );
        assertLocalname( "baseModel", JA.baseModel );
        assertLocalname( "literalContent", JA.literalContent );
        assertLocalname( "externalContent", JA.externalContent );
        assertLocalname( "ontModelSpec", JA.ontModelSpec );
        assertLocalname( "assembler", JA.assembler );
        assertLocalname( "loadClass", JA.loadClass );
        assertLocalname( "prefix", JA.prefix );
        assertLocalname( "prefixMapping", JA.prefixMapping );
        assertLocalname( "namespace", JA.namespace );
        assertLocalname( "includes", JA.includes );
        assertLocalname( "directory", JA.directory );
        assertLocalname( "create", JA.create );
        assertLocalname( "strict", JA.strict );
        assertLocalname( "mapName", JA.mapName );
        assertLocalname( "documentManager", JA.documentManager );
        assertLocalname( "ontModelSpec", JA.ontModelSpec );
        assertLocalname( "ontLanguage", JA.ontLanguage );
        assertLocalname( "true", JA.True );
        assertLocalname( "false", JA.False );
        }
    
    protected void assertLocalname( String local, Resource resource )
        {
        assertEquals( JA.uri + local, resource.getURI() );
        }

    public void testObjectTypes()
        {
        assertSubclassOf( JA.Model, JA.Object );
        assertSubclassOf( JA.PrefixMapping, JA.Object );
        assertSubclassOf( JA.SinglePrefixMapping, JA.PrefixMapping );
        assertSubclassOf( JA.Content, JA.Object );
        assertSubclassOf( JA.OntModelSpec, JA.Object );
        assertSubclassOf( JA.ReasonerFactory, JA.Object );
        }
    
    public void testModelTypes()
        {
        assertSubclassOf( JA.MemoryModel, JA.Model );
        assertSubclassOf( JA.DefaultModel, JA.Model );
        assertSubclassOf( JA.InfModel, JA.Model );
        assertSubclassOf( JA.OntModel, JA.InfModel );
        assertSubclassOf( JA.NamedModel, JA.Model );
        assertSubclassOf( JA.FileModel, JA.NamedModel );
        // assertSubclassOf( JA.OntModelSpec, JA.ReasonerFactory );
        }
    
    public void testInfModelProperties()
        {
        assertDomain( JA.InfModel, JA.baseModel );
        assertDomain( JA.InfModel, JA.reasoner );
        }
    
    public void testOntModelProperties()
        {
        assertDomain( JA.OntModel, JA.ontModelSpec );
        // assertRange( JA.ReasonerFactory, JA.reasonerURL );
        }
    }
