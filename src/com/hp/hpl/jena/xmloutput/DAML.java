/*
 *  (c) Copyright Hewlett-Packard Company 2001 
 *  All rights reserved.
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
 *
 */

package com.hp.hpl.jena.xmloutput;

import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFException;

/**
 * A half-baked DAML vocabulary class, with
 * only the values I needed.
 * A fuller DAML implementation would belong in
 * com.hp.hpl.vocabulary
 * @author jjc
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:21:52 $'
 */
class DAML {

    protected static final String uri = "http://www.daml.org/2001/03/daml+oil#";

    /** The URI used as the base DAML URI: <em>http://www.daml.org/2001/03/daml+oil#</em>
     */
    public static String getURI() {
        return uri;
    }

    static final String nList = "List";
    public static Resource List = null;

    static final String nfirst = "first";
    public static Property first = null;
    static final String nrest = "rest";
    public static Property rest = null;

    static final String nnil = "nil";
    public static Resource nil = null;

    static final String nClass = "Class";
    public static Resource Class = null;
    static final String nDatatype = "Datatype";
    public static Resource Datatype = null;
    static final String nDatatypeProperty = "DatatypeProperty";
    public static Resource DatatypeProperty = null;
    static final String nObjectProperty = "ObjectProperty";
    public static Resource ObjectProperty = null;
    static final String nOntology = "Ontology";
    public static Resource Ontology = null;
    static final String nProperty = "Property";
    public static Resource Property = null;
    static final String nTransitiveProperty = "TransitiveProperty";
    public static Resource TransitiveProperty = null;
    static final String nUnambigousProperty = "UnambigousProperty";
    public static Resource UnambigousProperty = null;
    static final String nUniqueProperty = "UniqueProperty";
    public static Resource UniqueProperty = null;

    static {
        try {
            List = new ResourceImpl(uri + nList);
            nil = new ResourceImpl(uri + nnil);

            first = new PropertyImpl(uri, nfirst);
            rest = new PropertyImpl(uri, nrest);
            Class = new ResourceImpl(uri + nClass);
            Datatype = new ResourceImpl(uri + nDatatype);
            DatatypeProperty = new ResourceImpl(uri + nDatatypeProperty);
            ObjectProperty = new ResourceImpl(uri + nObjectProperty);
            Ontology = new ResourceImpl(uri + nOntology);
            Property = new ResourceImpl(uri + nProperty);
            TransitiveProperty = new ResourceImpl(uri + nTransitiveProperty);
            UnambigousProperty = new ResourceImpl(uri + nUnambigousProperty);
            UniqueProperty = new ResourceImpl(uri + nUniqueProperty);
        } catch (RDFException e) {
            ErrorHelper.logInternalError("prettywriter.DAML", 1, e);
        }
    }

}
