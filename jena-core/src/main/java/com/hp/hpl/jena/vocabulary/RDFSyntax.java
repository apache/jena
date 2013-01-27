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
