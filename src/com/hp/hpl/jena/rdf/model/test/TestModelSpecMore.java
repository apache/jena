/*
(c) Copyright 2003, Hewlett-Packard Development Company, LP
[See end of file]
$$
*/
package com.hp.hpl.jena.rdf.model.test;

import java.io.*;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.util.FileUtils;

import junit.framework.TestSuite;

/**
    A second bunch of tests for ModelSpecs [because the first bunch is too busy]
*/
public class TestModelSpecMore extends ModelTestBase
    {

    public TestModelSpecMore( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestModelSpecMore.class ); }    
    
    public void testLoadWorks() throws Exception
        {
        String url = makeModel( "a bb c" );
        Model wanted = FileUtils.loadModel( url );
        Model spec = modelWithStatements( "_root rdf:type jms:PlainModelSpec; _root jms:maker jms:MemMaker; _root jms:loadWith " + url );
        ModelSpec ms = ModelFactory.createSpec( spec );
        Model m = ModelFactory.createModel( ms );
        assertIsoModels( wanted, m );
        }
    
    public void testLoadMultiWorks() throws Exception
	    {
        String url1 = makeModel( "dogs may bark" ), url2 = makeModel( "pigs might fly" );
	    Model wanted = FileUtils.loadModels( new String[] {url1, url2} );
	    Model spec = modelWithStatements( "_root rdf:type jms:PlainModelSpec; _root jms:maker jms:MemMaker" );
	    modelAdd( spec, "_root jms:loadWith " + url1 );
	    modelAdd( spec, "_root jms:loadWith " + url2 );
	    ModelSpec ms = ModelFactory.createSpec( spec );
	    Model m = ModelFactory.createModel( ms );
	    assertIsoModels( wanted, m );
	    }
    
    protected String makeModel( String statements ) throws FileNotFoundException, IOException
        {
	    String name = FileUtils.tempFileName( "test-load-with-", ".rdf" ).getAbsolutePath();
        Model m = modelWithStatements( statements );
        FileOutputStream fos = new FileOutputStream( name );
        m.write( fos, FileUtils.guessLang( name ) ); 
        fos.close();
	    return "file://" + name;
        }
    
    public void testOpenModel()
        {
        Model s = modelWithStatements( "_root jms:maker jms:MemMaker" );
        assertTrue( ModelFactory.createSpec( s ).openModel( "nosuch" ) instanceof Model );
        }
    }

/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
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
