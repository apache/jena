/**
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

package com.hp.hpl.jena.sparql.serializer;

import java.io.OutputStream ;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.Syntax ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode ;


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
            s = Syntax.defaultQuerySyntax ;
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
            s = Syntax.defaultQuerySyntax ;
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
            outSyntax = Syntax.defaultQuerySyntax ;
        
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
        
        Log.warn(Serializer.class, "Unknown syntax: "+outSyntax) ;
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
