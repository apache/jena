/*
 *  (c)     Copyright 2003 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: RDFSyntax.java,v 1.2 2003-08-27 13:08:11 andy_seaborne Exp $
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;


import com.hp.hpl.jena.rdf.model.Resource;
/**
 * This class includes fragment IDs from the
 * <a href="http://www.w3.org/TR/rdf-syntax-grammar">
 * RDF Syntax WD</a>, to identify grammar rules etc.
 * using in the Jena I/O routines.
 * These URIs can be used in some options (specifically
 * the configuration of the RDF/XML-ABBREV writer) to indicate
 * behaviour concerning these rules.
 * @author jjc
 */
public class RDFSyntax {
protected static final String uri =
    "http://www.w3.org/TR/rdf-syntax-grammar#";
    static public String getURI() {
        return uri;
    }
static final public Resource coreSyntaxTerms = new ResourceImpl(uri+"coreSyntaxTerms");
static final public Resource syntaxTerms = new ResourceImpl(uri+"syntaxTerms");
static final public Resource oldTerms = new ResourceImpl(uri+"oldTerms");
static final public Resource nodeElementURIs = new ResourceImpl(uri+"nodeElementURIs");
static final public Resource propertyElementURIs = new ResourceImpl(uri+"propertyElementURIs");
static final public Resource propertyAttributeURIs = new ResourceImpl(uri+"propertyAttributeURIs");
static final public Resource doc = new ResourceImpl(uri+"doc");
static final public Resource RDF = new ResourceImpl(uri+"RDF");
static final public Resource nodeElementList = new ResourceImpl(uri+"nodeElementList");
static final public Resource nodeElement = new ResourceImpl(uri+"nodeElement");
static final public Resource ws = new ResourceImpl(uri+"ws");
static final public Resource propertyEltList = new ResourceImpl(uri+"propertyEltList");
static final public Resource propertyElt = new ResourceImpl(uri+"propertyElt");
static final public Resource resourcePropertyElt = new ResourceImpl(uri+"resourcePropertyElt");
static final public Resource literalPropertyElt = new ResourceImpl(uri+"literalPropertyElt");
static final public Resource parseTypeLiteralPropertyElt = new ResourceImpl(uri+"parseTypeLiteralPropertyElt");
static final public Resource parseTypeResourcePropertyElt = new ResourceImpl(uri+"parseTypeResourcePropertyElt");
static final public Resource parseTypeCollectionPropertyElt = new ResourceImpl(uri+"parseTypeCollectionPropertyElt");
static final public Resource parseTypeOtherPropertyElt = new ResourceImpl(uri+"parseTypeOtherPropertyElt");
static final public Resource emptyPropertyElt = new ResourceImpl(uri+"emptyPropertyElt");
static final public Resource idAttr = new ResourceImpl(uri+"idAttr");
static final public Resource nodeIdAttr = new ResourceImpl(uri+"nodeIdAttr");
static final public Resource aboutAttr = new ResourceImpl(uri+"aboutAttr");
static final public Resource bagIdAttr = new ResourceImpl(uri+"bagIdAttr");
static final public Resource propertyAttr = new ResourceImpl(uri+"propertyAttr");
static final public Resource resourceAttr = new ResourceImpl(uri+"resourceAttr");
static final public Resource datatypeAttr = new ResourceImpl(uri+"datatypeAttr");
static final public Resource parseLiteral = new ResourceImpl(uri+"parseLiteral");
static final public Resource parseResource = new ResourceImpl(uri+"parseResource");
static final public Resource parseCollection = new ResourceImpl(uri+"parseCollection");
static final public Resource parseOther = new ResourceImpl(uri+"parseOther");
static final public Resource URIReference = new ResourceImpl(uri+"URI-reference");
static final public Resource literal = new ResourceImpl(uri+"literal");
static final public Resource rdfId = new ResourceImpl(uri+"rdf-id");
    static final public Resource sectionReification = new ResourceImpl(uri+"section-Reification");
    static final public Resource sectionListExpand = new ResourceImpl(uri+"section-List-Expand");

}

/*
 *  (c)   Copyright 2000, 2001, 2002 Hewlett-Packard Development Company, LP
 *   All rights reserved.
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
 *
 * RDF.java
 *
 * Created on 28 July 2000, 18:12
 */
