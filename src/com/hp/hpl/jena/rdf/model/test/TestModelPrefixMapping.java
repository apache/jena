/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelPrefixMapping.java,v 1.5 2004-07-02 11:39:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.test.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import junit.framework.*;

/**
    Test that a model is a prefix mapping.
 	@author kers
*/
public class TestModelPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestModelPrefixMapping( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelPrefixMapping.class ); }   

    protected PrefixMapping getMapping()
        { return ModelFactory.createDefaultModel(); }       
    
    protected static final String alphaPrefix = "alpha";
    protected static final String betaPrefix = "beta";
    protected static final String alphaURI = "http://testing.jena.hpl.hp.com/alpha#";
    protected static final String betaURI = "http://testing.jena.hpl.hp.com/beta#";
    
    protected PrefixMapping baseMap = PrefixMapping.Factory.create()
        .setNsPrefix( alphaPrefix, alphaURI )
        .setNsPrefix( betaPrefix, betaURI );
    
    private PrefixMapping prevMap;
    
    public void setPrefixes()
        {
        prevMap = ModelCom.setDefaultModelPrefixes( baseMap );
        }
    
    public void restorePrefixes()
        {
        ModelCom.setDefaultModelPrefixes( prevMap );
        }
    
    /**
        Test that a freshly-created Model has the prefixes established by the
        default in ModelCom.
    */
    public void testDefaultPrefixes()
        {
        setPrefixes();
        Model m = ModelFactory.createDefaultModel();
        assertEquals( baseMap.getNsPrefixMap(), m.getNsPrefixMap() );
        restorePrefixes();
        }
    
    public void testOnlyFreshPrefixes()
        {
        setPrefixes();
        try { doOnlyFreshPrefixes(); } finally { restorePrefixes(); }
        }
    
    /**
       Test that existing prefixes are not over-ridden by the default ones.
    */
    private void doOnlyFreshPrefixes()
        { 
        String newURI = "abc:def/";
        Graph g = Factory.createDefaultGraph();
        PrefixMapping pm = g.getPrefixMapping();
        pm.setNsPrefix( alphaPrefix, newURI );
        Model m = ModelFactory.createModelForGraph( g );
        assertEquals( newURI, m.getNsPrefixURI( alphaPrefix ) );
        assertEquals( betaURI, m.getNsPrefixURI( betaPrefix ) ); }
    
    public void testGetDefault()
        { setPrefixes();
        try { assertSame( baseMap, ModelCom.getDefaultModelPrefixes() ); } 
        finally { restorePrefixes(); } }
    }


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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