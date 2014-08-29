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

/*
 * ALiteral.java
 *
 * Created on June 26, 2001, 9:27 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.ANode ;

/**
 * A string literal property value from an RDF/XML file. 
 */
public interface ALiteral extends ANode {

/** True if this literal was formed from a rdf:parseType="Literal" construction.
 * @return true for rdf:parseType="Literal" or any other unrecognised parseType.
 */    
    public boolean isWellFormedXML();
    
    // Usually null, maybe "Literal" or something else.
/** When <CODE>isWellFormedXML()</CODE> is true, this returns the value of the <I>rdf:parseType</I> attribute, usually "Literal".
 * Otherwise <B>null</B> is returned.
 * @return The <I>parseType</I> for well formed XML, or <B>null</B> for normal literals.
 */    
    public String getParseType();
    
    /**
     * The datatype URI of a typed literal, or null 
     * for an untyped literal.
     * @return the URI as a String, or null
     */
    public String getDatatypeURI();
    
/** The string value of the literal.
 * @return The string.
 */    
    @Override
    public String toString();
    
    // never null - maybe ""
/** The value of xml:lang for this literal, often the empty string.
 * @return xml:lang.
 */    
    public String getLang();
}
