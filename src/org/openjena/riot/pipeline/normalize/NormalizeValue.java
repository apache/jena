/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.pipeline.normalize;

import java.math.BigDecimal ;
import java.math.BigInteger ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;

class NormalizeValue
{
    // Auxillary class of datatype handers, placed here to avoid static initialization
    // ordering problems (if in CanonicalizeLiteral, all this low-level machinary would
    // need to be in the file before the external API, which I consider bad style).  It
    // is a source of obscure bugs.

    // See Normalizevalue2 for "faster" versions (less parsing overhead). 
    
    static DatatypeHandler dtFloat = null ;
    static DatatypeHandler dtBoolean = null ;
    static DatatypeHandler dtDatetime = null ;

    // DateTimeStruct
    // Years may be 4 or more chars
    
    static DatatypeHandler dtInteger = new DatatypeHandler() {
        public Node handle(Node node, String lexicalForm, RDFDatatype datatype)
        {
            char[] chars = lexicalForm.toCharArray() ;
            if ( chars.length == 0 )
                // Illegal lexical form.
                return node ;
            
            // If valid and one char, it must be legal.
            // If valid, and two chars and not leading 0, it must be valid.
            String lex2 = lexicalForm ;
            
            if ( lex2.startsWith("+") )
                lex2 = lex2.substring(1) ;
            
            if ( lex2.length() > 8 )
                // Maybe large than an int so do carefully.
                lex2 = new BigInteger(lexicalForm).toString() ;
            else
            {
                // Avoid object churn.
                int x = Integer.parseInt(lex2) ;
                lex2 = Integer.toString(x) ;
            }
            if ( lex2.equals(lexicalForm) )
                return node ;
            return Node.createLiteral(lex2, null, datatype) ;
        }
    } ;

    static DatatypeHandler dtDecimal = new DatatypeHandler() {
        public Node handle(Node node, String lexicalForm, RDFDatatype datatype)
        {
            BigDecimal bd = new BigDecimal(lexicalForm).stripTrailingZeros() ;
            String lex2 = bd.toPlainString() ;
            
            // Ensure there is a "."
            //if ( bd.scale() <= 0 )
            if ( lex2.indexOf('.') == -1 )
                // Must contain .0
                lex2 = lex2+".0" ;
            if ( lex2.equals(lexicalForm) )
                return node ;
            return Node.createLiteral(lex2, null, datatype) ;
        }
    } ;
    
    static DatatypeHandler dtDouble = new DatatypeHandler() {
        public Node handle(Node node, String lexicalForm, RDFDatatype datatype)
        {
            double d = Double.parseDouble(lexicalForm) ;
            String lex2 = Double.toString(d) ;
            if ( lex2.indexOf('e') == -1 )
                lex2 = lex2+"e0" ;
            if ( lex2.equals(lexicalForm) )
                return node ;
            return Node.createLiteral(lex2, null, datatype) ;
        }
    } ;

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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