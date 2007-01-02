/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestCaseBasic.java,v 1.11 2007-01-02 11:49:29 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import java.lang.reflect.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
     @author  bwm, kers
*/
public class TestCaseBasic extends com.hp.hpl.jena.regression.TestCaseBasic 
	{
    private Class graphClass;

    public TestCaseBasic(String name, Class graphClass) 
    	{
        super(name);
        this.graphClass = graphClass;
    	}
    
    private Graph newGraph( Constructor cons ) throws Exception
    	{
    	return (Graph) cons.newInstance
            ( new Object [] { Factory.createGraphMem(), Factory.createGraphMem() } ); 
    	}
    	
    public void setUp() throws Exception
    	{
		Constructor constructor = graphClass.getConstructor
            (new Class [] { Graph.class, Graph.class } );
    	m1 = ModelFactory.createModelForGraph( newGraph( constructor ) );
    	m2 = ModelFactory.createModelForGraph( newGraph( constructor ) );
    	m3 = ModelFactory.createModelForGraph( newGraph( constructor ) );
    	m4 = ModelFactory.createModelForGraph( newGraph( constructor ) );
		}
	}

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
