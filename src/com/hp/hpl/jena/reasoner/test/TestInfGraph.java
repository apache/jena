/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestInfGraph.java,v 1.3 2004-11-29 16:39:01 der Exp $
*/

package com.hp.hpl.jena.reasoner.test;

import junit.framework.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.InfGraph;

/**
     Needs extending; relys on knowing that the only InfGraph currently used is
     the Jena-provided base. Needs to be made into an abstract test and
     parametrised with the InfGraph being tested (hence getInfGraph).
    @author hedgehog
*/

public class TestInfGraph extends AbstractTestGraph
    {
    public TestInfGraph( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestInfGraph.class ); }
    
    private InfGraph getInfGraph()
        {
        return (InfGraph) ModelFactory.createOntologyModel().getGraph();
        }
    
    public Graph getGraph()
        { return getInfGraph(); }
    
    public void testInfGraph()
        {
        InfGraph ig = getInfGraph();
        assertSame( ig.getPrefixMapping(), ig.getRawGraph().getPrefixMapping() );
        }
    
    public void testInfReification()
        {
        InfGraph ig = getInfGraph();
        assertSame( ig.getReifier(), ig.getRawGraph().getReifier() );
        }
    
    /**
         Placeholder. Will need revision later.
    */
    public void testInfCapabilities()
        {
        // The default Ontology inference model is RDFS which is safe
        assertTrue( getInfGraph().getCapabilities().findContractSafe() );
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