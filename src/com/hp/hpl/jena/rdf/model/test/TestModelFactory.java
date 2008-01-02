/*
  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelFactory.java,v 1.36 2008-01-02 12:04:41 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.graph.compose.Union;

import junit.framework.*;

/**
    Tests the ModelFactory code. Very skeletal at the moment. It's really
    testing that the methods actually exists, but it doesn't check much in
    the way of behaviour.
    
    @author kers
*/

public class TestModelFactory extends ModelTestBase
    {
    public static final Resource DAMLLangResource = resource( ProfileRegistry.DAML_LANG );
    
    public TestModelFactory(String name)
        { super(name); }
        
    public static TestSuite suite()
        { return new TestSuite( TestModelFactory.class ); }   
        
    /**
        Test that ModelFactory.createDefaultModel() exists. [Should check that the Model
        is truly a "default" model.]
     */
    public void testCreateDefaultModel()
        { ModelFactory.createDefaultModel().close(); }    

    public void testGetDefaultPrefixMapping()
        {
        assertSame( ModelCom.getDefaultModelPrefixes(), ModelFactory.getDefaultModelPrefixes() );
        }
    
    public void testSetDefaultPrefixMapping()
        {
        PrefixMapping original = ModelCom.getDefaultModelPrefixes();
        PrefixMapping pm = PrefixMapping.Factory.create();
        ModelFactory.setDefaultModelPrefixes( pm );
        assertSame( pm, ModelCom.getDefaultModelPrefixes() );
        assertSame( pm, ModelFactory.getDefaultModelPrefixes() );
        ModelCom.setDefaultModelPrefixes( original );
        }
    
    public void testCreateInfModel() 
        {
        String rule = "-> (eg:r eg:p eg:v).";
        Reasoner r = new GenericRuleReasoner( Rule.parseRules(rule) );
        InfGraph ig = r.bind( ModelFactory.createDefaultModel().getGraph() );
        InfModel im = ModelFactory.createInfModel(ig);
        assertInstanceOf( InfModel.class, im );
        assertEquals( 1, im.size() );
        }
    
    /**
         test that a union model is a model over the union of the two underlying
         graphs. (We don't check that Union works - that's done in the Union
         tests, we hope.)
    */
    public void testCreateUnion()
        {
        Model m1 = ModelFactory.createDefaultModel();
        Model m2 = ModelFactory.createDefaultModel();
        Model m = ModelFactory.createUnion( m1, m2 );
        assertInstanceOf( Union.class, m.getGraph() );
        assertSame( m1.getGraph(), ((Union) m.getGraph()).getL() );
        assertSame( m2.getGraph(), ((Union) m.getGraph()).getR() );
        }
    
    public void testAssembleModelFromModel()
        {
        // TODO Model ModelFactory.assembleModelFrom( Model singleRoot )
        }
    
    public void testFindAssemblerRoots()
        {
        // TODO Set ModelFactory.findAssemblerRoots( Model m )
        }

    public void testAssmbleModelFromRoot()
        {
        // TODO Model assembleModelFrom( Resource root )
        }
    }

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
