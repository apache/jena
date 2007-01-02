/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionAltMethods.java,v 1.4 2007-01-02 11:49:22 andy_seaborne Exp $
*/

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionAltMethods extends NewRegressionContainerMethods
    {
    public NewRegressionAltMethods( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionAltMethods.class ); }

    protected Container createContainer()
        { return m.createAlt(); }

    protected Resource getContainerType()
        { return RDF.Alt; }
    
    public void testDefaults()
        {
        Alt a = m.createAlt();
        Literal tvLiteral = m.createLiteral( "test 12 string 2" );
        Resource tvResObj = m.createResource( new ResTestObjF() );
        Bag tvBag = m.createBag();
        Alt tvAlt = m.createAlt();
        Seq tvSeq = m.createSeq();
    //
        Resource tvResource = m.createResource();
        assertEquals( tvLiteral, a.setDefault( tvLiteral ).getDefault() );
        assertEquals( tvLiteral, a.getDefaultLiteral() );
        assertEquals( tvResource, a.setDefault( tvResource ).getDefaultResource() );
        assertEquals( tvByte, a.setDefault( tvByte ).getDefaultByte() );
        assertEquals( tvShort, a.setDefault( tvShort ).getDefaultShort() );
        assertEquals( tvInt, a.setDefault( tvInt ).getDefaultInt() );
        assertEquals( tvLong, a.setDefault( tvLong ).getDefaultLong() );
        assertEquals( tvFloat, a.setDefault( tvFloat ).getDefaultFloat(), fDelta );
        assertEquals( tvDouble, a.setDefault( tvDouble ).getDefaultDouble(), dDelta );
        assertEquals( tvChar, a.setDefault( tvChar ).getDefaultChar() );
        assertEquals( tvString, a.setDefault( tvString ).getDefaultString() );
        assertEquals( tvResObj, a.setDefault( tvResObj ).getDefaultResource() );
        assertEquals( tvLitObj, a.setDefault( tvLitObj ).getDefaultObject( new LitTestObjF() ) );
        assertEquals( tvAlt, a.setDefault( tvAlt ).getDefaultAlt() );
        assertEquals( tvBag, a.setDefault( tvBag ).getDefaultBag() );
        assertEquals( tvSeq, a.setDefault( tvSeq ).getDefaultSeq() );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/