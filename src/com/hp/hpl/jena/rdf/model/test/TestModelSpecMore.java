/*
(c) Copyright 2003, Hewlett-Packard Development Company, LP
[See end of file]
$$
*/
package com.hp.hpl.jena.rdf.model.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.ModelLoader;
import com.hp.hpl.jena.vocabulary.*;

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
    
    public void xtestCreateFromResource() throws FileNotFoundException, IOException
	    {
	    File temp = FileUtils.tempFileName( "pre", ".rdf" );   
	    Model desc = TestModelSpec.createPlainModelDesc();
	    desc.write( System.out, "N3" );
	    TestModelSpec.writeModel( temp, desc );
	    ModelSpec ms = ModelSpecImpl.create( resource( "file:" + temp ) );
	    assertIsoModels( desc, ms.getDescription() );
	    }
    
    public void xtestCreateMemModelMaker()
        {
        Resource mem = JMS.MemMakerSpec;
        testCreateModelMaker( JMS.rsStandard, mem, SimpleGraphMaker.class );
        testCreateModelMaker( JMS.rsMinimal, mem, SimpleGraphMaker.class );
        testCreateModelMaker( JMS.rsConvenient, mem, SimpleGraphMaker.class );
        }

    protected static Resource resource()
        { return ResourceFactory.createResource(); }
    
    public void testCreateModelMaker( Resource style, Resource cl, Class required )
        {
        Resource me = resource();
        ReificationStyle wanted = JMS.findStyle( style );
        Model spec = modelWithStatements( "" )
            .add( me, RDF.type, cl )
            .add( me, JMS.reificationMode, style );
        ModelMaker maker = ModelSpecImpl.createMaker( spec );
        assertTrue( required.isInstance( maker.getGraphMaker() ) );
        assertEquals( wanted, maker.getGraphMaker().getReificationStyle() );
        }
    
    public void testCreatePlainMemModel()
        {
        Resource me = resource();
        Resource maker = resource();
        Model spec = TestModelSpec.createPlainModelDesc( me, maker );
        PlainModelSpec pms = new PlainModelSpec( maker, spec );
        ModelMaker mm = pms.getModelMaker();
        Model desc = mm.getDescription( me );
        spec.write( System.out, "N3-TRIPLES" );
        desc.write( System.out, "N3-TRIPLES" );
        assertTrue( desc.contains( me, RDF.type, JMS.MemMakerSpec ) );
        assertTrue( desc.contains( null, JMS.reificationMode, JMS.rsMinimal ) );
        assertTrue( mm.getGraphMaker() instanceof SimpleGraphMaker );
        assertEquals( ReificationStyle.Minimal , mm.getGraphMaker().getReificationStyle() );
        }
        

                    
    public void xtestA()
        {
        String url = "file:/tmp/some.rdf";
        Model wanted = ModelLoader.loadModel( url );
        Model spec = modelWithStatements( "_root rdf:type jms:PlainModelSpec; _root jms:maker jms:MemMaker; _junk jms:loadWith error; _root jms:loadWith " + url );
        ModelSpec ms = ModelFactory.createSpec( spec );
        Model m = ModelFactory.createModel( ms );
        assertIsoModels( wanted, m );
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
