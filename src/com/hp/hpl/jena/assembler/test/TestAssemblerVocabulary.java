/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestAssemblerVocabulary.java,v 1.4 2006-10-03 10:27:20 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.rdf.model.*;

public class TestAssemblerVocabulary extends AssemblerTestBase
    {
    public TestAssemblerVocabulary( String name )
        { super( name ); }

    protected Class getAssemblerClass()
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
        assertLocalname( "RDBModel", JA.RDBModel );
        assertLocalname( "FileModel", JA.FileModel );
        assertLocalname( "OntModel", JA.OntModel );
        assertLocalname( "OntModelSpec", JA.OntModelSpec );
    //
        assertLocalname( "Connection", JA.Connection );
        assertLocalname( "PrefixMapping", JA.PrefixMapping );
        assertLocalname( "ReasonerFactory", JA.ReasonerFactory );
        assertLocalname( "Content", JA.Content );
        assertLocalname( "LiteralContent", JA.LiteralContent );
        assertLocalname( "ExternalContent", JA.ExternalContent );
    //
        assertLocalname( "rules", JA.rules );
        assertLocalname( "reasoner", JA.reasoner );
        assertLocalname( "reasonerURL", JA.reasonerURL );
        assertLocalname( "baseModel", JA.baseModel );
        assertLocalname( "literalContent", JA.literalContent );
        assertLocalname( "externalContent", JA.externalContent );
        assertLocalname( "ontModelSpec", JA.ontModelSpec );
        assertLocalname( "assembler", JA.assembler );
        assertLocalname( "prefix", JA.prefix );
        assertLocalname( "namespace", JA.namespace );
        assertLocalname( "includes", JA.includes );
        assertLocalname( "directory", JA.directory );
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
        assertSubclassOf( JA.Content, JA.Object );
        assertSubclassOf( JA.Connection, JA.Object );
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
        assertSubclassOf( JA.RDBModel, JA.NamedModel );
        assertSubclassOf( JA.FileModel, JA.NamedModel );
        // assertSubclassOf( JA.OntModelSpec, JA.ReasonerFactory );
        assertSubclassOf( JA.ModelSource, JA.Connectable );
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


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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