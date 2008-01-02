/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestVocabRDFS.java,v 1.10 2008-01-02 12:08:10 andy_seaborne Exp $
*/

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

/**
 	@author kers
*/
public class TestVocabRDFS extends ModelTestBase
    {
    public TestVocabRDFS(String name)
        {  super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestVocabRDFS.class ); }

    public void testVocabRDFS()
        {
        String ns = "http://www.w3.org/2000/01/rdf-schema#";
        assertEquals( ns, RDFS.getURI() );
        assertEquals( ns + "Class", RDFS.Class.getURI() );
        assertEquals( ns + "Datatype", RDFS.Datatype.getURI() );
        assertEquals( ns + "Container", RDFS.Container.getURI() );
        assertEquals( ns + "ContainerMembershipProperty", RDFS.ContainerMembershipProperty.getURI() );
        assertEquals( ns + "Literal", RDFS.Literal.getURI() );
        assertEquals( ns + "Resource", RDFS.Resource.getURI() );
        assertEquals( ns + "comment", RDFS.comment.getURI() );
        assertEquals( ns + "domain", RDFS.domain.getURI() );
        assertEquals( ns + "label", RDFS.label.getURI() );
        assertEquals( ns + "isDefinedBy", RDFS.isDefinedBy.getURI() );
        assertEquals( ns + "range", RDFS.range.getURI() );
        assertEquals( ns + "seeAlso", RDFS.seeAlso.getURI() );
        assertEquals( ns + "subClassOf", RDFS.subClassOf.getURI() );
        assertEquals( ns + "subPropertyOf", RDFS.subPropertyOf.getURI() );
        assertEquals( ns + "member", RDFS.member.getURI() );
        }

	public void testNodes()
		{
        assertEquals( RDFS.Class.asNode(), RDFS.Nodes.Class );
        assertEquals( RDFS.Datatype.asNode(), RDFS.Nodes.Datatype );
        assertEquals( RDFS.Container.asNode(), RDFS.Nodes.Container );
        assertEquals( RDFS.ContainerMembershipProperty.asNode(), RDFS.Nodes.ContainerMembershipProperty );
        assertEquals( RDFS.Literal.asNode(), RDFS.Nodes.Literal );
        assertEquals( RDFS.Resource.asNode(), RDFS.Nodes.Resource );
        assertEquals( RDFS.comment.asNode(), RDFS.Nodes.comment );
        assertEquals( RDFS.domain.asNode(), RDFS.Nodes.domain );
        assertEquals( RDFS.label.asNode(), RDFS.Nodes.label );
        assertEquals( RDFS.isDefinedBy.asNode(), RDFS.Nodes.isDefinedBy );
        assertEquals( RDFS.range.asNode(), RDFS.Nodes.range );
        assertEquals( RDFS.seeAlso.asNode(), RDFS.Nodes.seeAlso );
        assertEquals( RDFS.subClassOf.asNode(), RDFS.Nodes.subClassOf );
        assertEquals( RDFS.subPropertyOf.asNode(), RDFS.Nodes.subPropertyOf );
        assertEquals( RDFS.member.asNode(), RDFS.Nodes.member );
		}
    }


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
