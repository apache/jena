/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

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
