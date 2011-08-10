/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.out;

import java.io.Writer ;

import com.hp.hpl.jena.graph.Node ;

public interface NodeFormatter
{
    public void format(Writer w, Node n) ;

    /** Node is guaranteed to be a URI node */
    public void formatURI(Writer w, Node n) ;
    public void formatURI(Writer w, String uriStr) ;
    
    public void formatVar(Writer w, Node n) ;
    public void formatVar(Writer w, String name) ;
    
    /** Node is guaranteed to be a blank node */
    public void formatBNode(Writer w, Node n) ;
    public void formatBNode(Writer w, String label) ;
    
    /** Node is guaranteed to be a literal */
    public void formatLiteral(Writer w, Node n) ;
    
    /** Plain string / xsd:string (RDF 1.1) */
    public void formatLitString(Writer w, String lex) ;
    
    /** String with language tag */
    public void formatLitLang(Writer w, String lex, String langTag) ;

    /** Literal with datatype, not a simple literal, not an xsd:string (RDF 1.1), no language tag. */
    public void formatLitDT(Writer w, String lex, String datatypeURI) ;
    
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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