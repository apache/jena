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
   $Id: ALiteral.java,v 1.1.1.1 2002-12-19 19:15:56 bwm Exp $
   AUTHOR:  Jeremy J. Carroll
*/
 /*
 * ALiteral.java
 *
 * Created on June 26, 2001, 9:27 AM
 */

package com.hp.hpl.jena.rdf.arp;

/**
 * A string literal property value from an RDF/XML file. 
 * @author  jjc
 */
public interface ALiteral {
/** Was this formed from a rdf:parseType="Literal" construction.
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
     * @return String
     */
    public String getDatatypeURI();
/** The string value of the literal.
 * @return The string.
 */    
    public String toString();
    // never null - maybe ""
/** The value of xml:lang for this literal, often the empty string.
 * @return xml:lang.
 */    
    public String getLang();
}

