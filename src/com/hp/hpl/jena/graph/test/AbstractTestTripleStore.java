/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: AbstractTestTripleStore.java,v 1.1 2004-09-10 11:09:41 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.impl.TripleStore;

import junit.framework.TestSuite;

/**
     AbstractTestTripleStore
     @author kers
*/
public abstract class AbstractTestTripleStore extends GraphTestBase
    {
    public AbstractTestTripleStore( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( AbstractTestTripleStore.class ); }
    
    /**
         Subclasses must over-ride to return a new empty TripleStore. 
    */
    public abstract TripleStore getTripleStore();
    
    protected TripleStore store;
    
    public void setUp()
        {
        store = getTripleStore();
        }
    
    public void testEmpty()
        { 
        assertEquals( true, store.isEmpty() );
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