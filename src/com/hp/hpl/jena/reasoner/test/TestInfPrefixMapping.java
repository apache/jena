/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestInfPrefixMapping.java,v 1.2 2004-01-29 12:34:04 chris-dollin Exp $
*/

package com.hp.hpl.jena.reasoner.test;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.reasoner.InfGraph;

/**
     Needs extending; relys on knowing that the only InfGraph currently used is
     the Jena-provided base. Needs to be made into an abstract test and
     parametrised with the InfGraph being tested (hence getInfGraph).
 	@author hedgehog
*/
public class TestInfPrefixMapping extends ModelTestBase
    {
    public TestInfPrefixMapping( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestInfPrefixMapping.class ); }
    
    private InfGraph getInfGraph()
        {
        return (InfGraph) ModelFactory.createOntologyModel().getGraph();
        }
    
    public void testInfGraph()
        {
        InfGraph ig = getInfGraph();
        assertSame( ig.getPrefixMapping(), ig.getRawGraph().getPrefixMapping() );
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