/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestReaderEvents.java,v 1.1 2004-06-29 14:42:03 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import java.io.StringReader;
import java.util.ArrayList;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.RecordingListener;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
 	TestReaderEvents - test that reader events are issued
 	@author kers
*/
public class TestReaderEvents extends ModelTestBase
	{
    public TestReaderEvents( String name )
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestReaderEvents.class ); }
    
    public void testReaderEvent()
        {
        Model m = ModelFactory.createDefaultModel();
        Graph g = m.getGraph();
        RecordingListener L = new RecordingListener();
        g.getEventManager().register( L );
        RDFReader r = m.getReader( "RDF/XML" );
        StringReader stringReader = new StringReader( "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'></rdf:RDF>" );
        r.read( m, stringReader, "" );
        L.assertHas( new Object[] {"someEvent", g, GraphEvents.startRead, "addList", g, new ArrayList(), "someEvent", g, GraphEvents.finishRead } );
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