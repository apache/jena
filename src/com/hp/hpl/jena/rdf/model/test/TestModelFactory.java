/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestModelFactory.java,v 1.10 2003-08-20 15:12:56 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
    Tests the ModelFactory code. Very skeletal at the moment. It's really
    testing that the methods actually exists, but it doesn't check much in
    the way of behaviour.
    
    @author kers
*/

public class TestModelFactory extends ModelTestBase
    {
    public static TestSuite suite()
        { return new TestSuite( TestModelFactory.class ); }   
        
    public TestModelFactory(String name)
        { super(name); }

    public void testCreateDefaultModel()
        {
        Model m = ModelFactory.createDefaultModel();
        m.close();
        }    
        
    public void testCreateSpec()
        {
        Model desc = ModelFactory.createDefaultModel();
        ModelSpec spec = ModelFactory.createSpec( desc ); 
        assertIsoModels( desc, spec.getDescription() );
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
