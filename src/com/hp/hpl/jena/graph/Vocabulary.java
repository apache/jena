/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Vocabulary.java,v 1.1.1.1 2002-12-19 19:13:36 bwm Exp $
*/

package com.hp.hpl.jena.graph;

/**
	@author kers
*/
public interface Vocabulary
    {
    public static final String prefixRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String prefixRDFS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String prefixQuery = "http://www.jena.hp.com/2002/01/query-predicates#";
    
    public static final Node rdfType = GraphTestBase.node( "rdf:type" );
    public static final Node rdfProperty = GraphTestBase.node( "rdf:Property" );
    public static final Node rdfSubject = GraphTestBase.node( "rdf:subject" );
    public static final Node rdfPredicate = GraphTestBase.node( "rdf:predicate" );
    public static final Node rdfObject = GraphTestBase.node( "rdf:object" );
    public static final Node rdfValue = GraphTestBase.node( "rdf:value" );
    public static final Node rdfStatement = GraphTestBase.node( "rdf:Statement" );
    public static final Node rdfBag = GraphTestBase.node( "rdf:Bag" );
    public static final Node rdfSeq = GraphTestBase.node( "rdf:Seq" );
    public static final Node rdfAlt = GraphTestBase.node( "rdf:Alt" );
    
    public static final Node rdfsResource = GraphTestBase.node( "rdfs:Resource" );
    public static final Node rdfsClass = GraphTestBase.node( "rdfs:Class" );
    public static final Node rdfsLiteral = GraphTestBase.node( "rdfs:Literal" );
    public static final Node rdfsContainer = GraphTestBase.node( "rdfs:Container" );
    public static final Node rdfsIsDefinedBy = GraphTestBase.node( "rdfs:isDefinedBy" );
    public static final Node rdfsMember = GraphTestBase.node( "rdfs:Member" );
    public static final Node rdfsSubClassOf = GraphTestBase.node( "rdfs:subClassOf" );
    public static final Node rdfsSubPropertyOf = GraphTestBase.node( "rdfs:subPropertyOf" );
    public static final Node rdfsDomain = GraphTestBase.node( "rdfs:domain" );
    public static final Node rdfsRange = GraphTestBase.node( "rdfs:range" );
    public static final Node rdfsSeeAlso = GraphTestBase.node( "rdfs:seeAlso" );
    public static final Node rdfsComment = GraphTestBase.node( "rdfs:comment" );    
    
    public static final Node rdfsContainerMembershipProperty = 
        GraphTestBase.node( "rdfs:ContainerMembershipProperty" );


    public static final Graph rdfsAxioms = GraphTestBase.graphWith
        ( 
        "rdf:type rdf:type rdf:Property" 
        + "; rdfs:Resource rdf:type rdfs:Class"
        + "; rdfs:Literal rdf:type rdfs:Class"
        + "; rdfs:Class rdf:type rdfs:Class"
        + "; rdf:Property rdf:type rdfs:Class"
        + "; rdf:Seq rdf:type rdfs:Class"
        + "; rdf:Bag rdf:type rdfs:Class"
        + "; rdf:Alt rdf:type rdfs:Class"
        + "; rdf:Statement rdf:type rdfs:Class"
        + "; rdf:type rdf:type rdf:Property"
        + "; rdf:type rdfs:domain rdfs:Resource"
        + "; rdf:type rdfs:range rdfs:Class"
        + "; rdfs:domain rdf:type rdf:Property"
        + "; rdfs:domain rdfs:domain rdf:Property"
        + "; rdfs:domain rdfs:range rdfs:Class"
        + "; rdfs:range rdf:type rdf:Property"
        + "; rdfs:range rdfs:domain rdf:Property"
        + "; rdfs:range rdfs:range rdfs:Class"
        + "; rdfs:subPropertyOf rdf:type rdf:Property"
        + "; rdfs:subPropertyOf rdfs:domain rdf:Property"
        + "; rdfs:subPropertyOf rdfs:range rdf:Property"
        + "; rdfs:subClassOf rdf:type rdf:Property"
        + "; rdfs:subClassOf rdfs:domain rdfs:Class"
        + "; rdfs:subClassOf rdfs:range rdfs:Class"
        + "; rdf:subject rdf:type rdf:Property"
        + "; rdf:subject rdfs:domain rdf:Statement"
        + "; rdf:predicate rdf:type rdf:Property"
        + "; rdf:predicate rdfs:domain rdf:Statement"
        + "; rdf:object rdf:type rdf:Property"
        + "; rdf:object rdfs:domain rdf:Statement"  
        );
    
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
