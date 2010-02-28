/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import java.io.OutputStream;

import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;


public class Serializer
{
    static final int BLOCK_INDENT = 2 ;
    /** Output the query
     * 
     * @param query  The query
     * @param out    OutputStream
     */
    static public void serialize(Query query, OutputStream out)
    {
        serialize(query, out, null) ;
    }
    
    /** Output the query
     * 
     * @param query  The query
     * @param out     OutputStream
     * @param syntax  Syntax URI
     */
    
    static public void serialize(Query query, OutputStream out, Syntax syntax)
    {
        IndentedWriter writer = new IndentedWriter(out) ;
        serialize(query, writer, syntax) ;
        writer.flush() ;
        try { out.flush() ; } catch (Exception ex) { }
    }
    
    /** Format the query into the buffer
     * @param query  The query
     * @param buff    IndentedLineBuffer
     */
    
    static public void serialize(Query query, IndentedLineBuffer buff)
    {
        Syntax s = query.getSyntax() ;
        if ( s == null )
            s = Syntax.syntaxSPARQL ;
        serialize(query, buff, s) ;
    }
    
    /** Format the query
     * 
     * @param query      The query
     * @param buff       IndentedLineBuffer in which to place the unparsed query
     * @param outSyntax  Syntax URI
     */
    
    static public void serialize(Query query, IndentedLineBuffer buff, Syntax outSyntax)
    {
        _serialize(query, buff, outSyntax) ;
    }
    
    /** Format the query
     * @param query   The query
     * @param writer  IndentedWriter
     */
    
    static public void serialize(Query query, IndentedWriter writer)
    {
        Syntax s = query.getSyntax() ;
        if ( s == null )
            s = Syntax.syntaxSPARQL ;
        serialize(query, writer, s) ;
    }
    
    /** Format the query
     * 
     * @param writer     IndentedWriter
     * @param outSyntax  Syntax URI
     */
    
    static public void serialize(Query query, IndentedWriter writer, Syntax outSyntax)
    {
        _serialize(query, writer, outSyntax) ;
    }
    
    static private void _serialize(Query query, IndentedWriter writer, Syntax outSyntax)
    {
        if ( outSyntax == null )
            outSyntax = Syntax.syntaxSPARQL ;
        
        if ( outSyntax.equals(Syntax.syntaxARQ) )
        {
            serializeARQ(query, writer) ;
            writer.flush() ;
            return ;
        }
        
        if (outSyntax.equals(Syntax.syntaxSPARQL_10))
        {
            serializeSPARQL_10(query, writer) ;
            writer.flush() ;
            return ;
        }

        if (outSyntax.equals(Syntax.syntaxSPARQL_11))
        {
            serializeSPARQL_11(query, writer) ;
            writer.flush() ;
            return ;
        }
        
//        if (outSyntax.equals(Syntax.syntaxSPARQL_X))
//        {
//            serializeSPARQL_X(query, writer) ;
//            writer.flush() ;
//            return ;
//        }
        
        ALog.warn(Serializer.class, "Unknown syntax: "+outSyntax) ;
    }
     
    static public void serializeARQ(Query query, IndentedWriter writer)
    {
        // For the query pattern
        SerializationContext cxt1 = new SerializationContext(query, new NodeToLabelMapBNode("b", false) ) ;
        // For the construct pattern
        SerializationContext cxt2 = new SerializationContext(query, new NodeToLabelMapBNode("c", false)  ) ;
        
        serializeARQ(query, writer, 
                     new FormatterElement(writer, cxt1),
                     new FmtExpr(writer, cxt1),
                     new FmtTemplate(writer, cxt2)) ;
    }
    
    static private void serializeARQ(Query query, 
                                     IndentedWriter writer, 
                                     FormatterElement eltFmt,
                                     FmtExpr    exprFmt,
                                     FormatterTemplate templateFmt)
    {
        QuerySerializer serilizer = new QuerySerializer(writer, eltFmt, exprFmt, templateFmt) ;
        query.visit(serilizer) ;
    }

    static public void serializeSPARQL_10(Query query, IndentedWriter writer)
    {
        // ARQ is a superset of SPARQL.
        serializeARQ(query, writer) ;
    }

    static public void serializeSPARQL_11(Query query, IndentedWriter writer)
    {
        // ARQ is a superset of SPARQL.
        serializeARQ(query, writer) ;
    }


    //    static public void serializeSPARQL_X(Query query, IndentedWriter writer)
//    {
//        SerializationContext cxt = new SerializationContext(query, null ) ;
//        QuerySerializerXML serilizer = new QuerySerializerXML(writer, cxt) ;
//        query.visit(serilizer) ;
//    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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