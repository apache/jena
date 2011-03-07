/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.pipeline.normalize;

import java.util.HashMap ;
import java.util.Map ;

import org.openjena.riot.LangTag ;


import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

public class CanonicalizeLiteral implements NodeTransform    
{
    private static final CanonicalizeLiteral singleton = new CanonicalizeLiteral(); 

    public static CanonicalizeLiteral get() { return singleton ; }

    private CanonicalizeLiteral() {}
    
    public Node convert(Node node)
    {
        if ( ! node.isLiteral() )
            return node ;
            
        RDFDatatype dt = node.getLiteralDatatype() ;
        Node n2 ;
        if ( dt == null )
        {
            if ( node.getLiteralLanguage().equals("") )
                return node ;
            n2 = canonicalLangtag(node, node.getLiteralLexicalForm(), node.getLiteralLanguage()) ; 
        }
        else
        {
            // Valid?  Yes - assumes checking has been done.
            // May integrate later
            DatatypeHandler handler = dispatch.get(dt) ;
            if ( handler == null )
                return node ;
    
            n2 = handler.handle(node, node.getLiteralLexicalForm(), dt) ;
        }
        if ( n2 == null )
            return node ;
        return n2 ;
    }
    
    private static Node canonicalLangtag(Node node, String lexicalForm, String langTag)
    {
        String langTag2 = LangTag.canonical(langTag) ;
        if ( langTag2.equals(langTag) )
            return null ;
        return Node.createLiteral(lexicalForm, langTag2, null) ;
    }
    
    private final static Map<RDFDatatype, DatatypeHandler> dispatch = new HashMap<RDFDatatype, DatatypeHandler>() ;

    // MUST be after the handler definitions as these assign to statics, so it's code lexcial order.
    // or use static class to force touching that, initializing and then getting the values. 
    static {
        dispatch.put(XSDDatatype.XSDinteger,                NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDdecimal,                NormalizeValue.dtDecimal) ;

        // Subtypes. Changes the datatype.
        dispatch.put(XSDDatatype.XSDint,                    NormalizeValue.dtInteger) ;        
        dispatch.put(XSDDatatype.XSDlong,                   NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDshort,                  NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDbyte,                   NormalizeValue.dtInteger) ;

        dispatch.put(XSDDatatype.XSDunsignedInt,            NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDunsignedLong,           NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDunsignedShort,          NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDunsignedByte,           NormalizeValue.dtInteger) ;

        dispatch.put(XSDDatatype.XSDnonPositiveInteger,     NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDnonNegativeInteger,     NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDpositiveInteger,        NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDnegativeInteger,        NormalizeValue.dtInteger) ;


        dispatch.put(XSDDatatype.XSDfloat,      NormalizeValue.dtFloat ) ;
        dispatch.put(XSDDatatype.XSDdouble,     NormalizeValue.dtDouble ) ;

        // Only fractional seconds part can vary for the same value. 
        dispatch.put(XSDDatatype.XSDdateTime,   NormalizeValue.dtDateTime) ;
        
        // These are fixed format 
//        dispatch.put(XSDDatatype.XSDdate,       null) ;
//        dispatch.put(XSDDatatype.XSDtime,       null) ;
//        dispatch.put(XSDDatatype.XSDgYear,      null) ;
//        dispatch.put(XSDDatatype.XSDgYearMonth, null) ;
//        dispatch.put(XSDDatatype.XSDgMonth,     null) ;
//        dispatch.put(XSDDatatype.XSDgMonthDay,  null) ;
//        dispatch.put(XSDDatatype.XSDgDay,       null) ;

        dispatch.put(XSDDatatype.XSDduration,   null) ;
        dispatch.put(XSDDatatype.XSDboolean,    NormalizeValue.dtBoolean) ;
    }
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