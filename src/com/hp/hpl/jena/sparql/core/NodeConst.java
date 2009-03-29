/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class NodeConst
{
    public static final Node nodeTrue       = Node.createLiteral("true", null,  XSDDatatype.XSDboolean) ; 
    public static final Node nodeFalse      = Node.createLiteral("false", null,  XSDDatatype.XSDboolean) ; 
    public static final Node nodeZero       = Node.createLiteral("0", null,  XSDDatatype.XSDinteger) ;
    public static final Node nodeOne        = Node.createLiteral("1", null,  XSDDatatype.XSDinteger) ;
    public static final Node nodeTwo        = Node.createLiteral("2", null,  XSDDatatype.XSDinteger) ;
    public static final Node nodeMinusOne   = Node.createLiteral("-1", null,  XSDDatatype.XSDinteger) ;
    
    public static final Node nodeRDFType    = RDF.Nodes.type ;
    public static final Node nodeFirst      = RDF.Nodes.first ;
    public static final Node nodeRest       = RDF.Nodes.rest ;
    public static final Node nodeNil        = RDF.Nodes.nil ;
    
    public static final Node nodeOwlSameAs  = OWL.sameAs.asNode() ;
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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