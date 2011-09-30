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

package com.hp.hpl.jena.sparql.modify.request;

import java.util.List ;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.serializer.FormatterElement ;
import com.hp.hpl.jena.sparql.serializer.PrologueSerializer ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateWriter
{
    public static void output(UpdateRequest request, IndentedWriter out)
    {
        output(request, out, new SerializationContext(request)) ;
    }
    
    public static void output(UpdateRequest request, IndentedWriter out, SerializationContext sCxt)
    {
        if ( sCxt == null )
            sCxt = new SerializationContext(request) ;
        prologue(out, sCxt.getPrologue()) ;
        boolean addSeparator = (request.getOperations().size() > 1) ;
        boolean first = true ;
        
        for ( Update update : request.getOperations() )
        {
            out.ensureStartOfLine() ;
            if ( ! first )
                out.println() ;
            first = false ;
            outputUpdate(update, out, sCxt) ;
            if ( addSeparator )
                out.print(" ;") ;
        }
        
        // Update requests always end in newline. 
        out.ensureStartOfLine() ;
        out.flush() ;
    }
    
    public static void output(Update update, IndentedWriter out, SerializationContext sCxt)
    {
        if ( sCxt == null )
            sCxt = new SerializationContext() ;
        prologue(out, sCxt.getPrologue()) ;
        outputUpdate(update, out, sCxt) ;
        // Update operations do not end in newline. 
        out.flush() ;
    }
    
    
    private static void outputUpdate(Update update, IndentedWriter out, SerializationContext sCxt)
    {
        Writer writer = new Writer(out, sCxt) ;
        update.visit(writer) ; 
    }

    private static void prologue(IndentedWriter out, Prologue prologue)
    {
        int row1 = out.getRow() ;
        PrologueSerializer.output(out, prologue) ;
        int row2 = out.getRow() ;
        if ( row1 != row2 )
            out.newline() ;
    }


    // newline policy - don't add until needed.
    private static class Writer implements UpdateVisitor
    {
        private static final int BLOCK_INDENT = 2 ;
        private final IndentedWriter out ;
        private final SerializationContext sCxt ;

        public Writer(IndentedWriter out, SerializationContext sCxt)
        {
            this.out = out ;
            this.sCxt = sCxt ;
        }

        private void visitDropClear(String name, UpdateDropClear update)
        {
            out.ensureStartOfLine() ;
            out.print(name) ;
            out.print(" ") ;
            if ( update.isSilent() )
                out.print("SILENT ") ;
            
            printTarget(update.getTarget()) ;
            
        }
    
        private void printTarget(Target target)
        {
            if ( target.isAll() )               { out.print("ALL") ; }
            else if ( target.isAllNamed() )     { out.print("NAMED") ; }
            else if ( target.isDefault() )      { out.print("DEFAULT") ; }
            else if ( target.isOneNamedGraph() )
            { 
                out.print("GRAPH ") ;
                String s = FmtUtils.stringForNode(target.getGraph(), sCxt) ;
                out.print(s) ;
            }
            else
            {
                out.print("Target BROKEN") ;
                throw new ARQException("Malformed Target") ;
            }
        }

        public void visit(UpdateDrop update)
        { visitDropClear("DROP", update) ; }

        public void visit(UpdateClear update)
        { visitDropClear("CLEAR", update) ; }

        public void visit(UpdateCreate update)
        {
            out.ensureStartOfLine() ;
            out.print("CREATE") ;
            out.print(" ") ;
            if ( update.isSilent() )
                out.print("SILENT ") ;
            
            String s = FmtUtils.stringForNode(update.getGraph(), sCxt) ;
            out.print(s) ;
        }

        public void visit(UpdateLoad update)
        {
            out.ensureStartOfLine() ;
            out.print("LOAD") ;
            out.print(" ") ;
            if ( update.getSilent() )
                out.print("SILENT ") ;
            
            outputStringAsURI(update.getSource()) ;
            
            if ( update.getDest() != null )
            {
                out.print(" INTO GRAPH ") ;
                output(update.getDest()) ;
            }
        }

        private void outputStringAsURI(String uriStr)
        {
            String x = FmtUtils.stringForURI(uriStr, sCxt) ;
            out.print(x) ;
        }
        
        private void printTargetUpdate2(Target target)
        {
            if ( target.isDefault() )      { out.print("DEFAULT") ; }
            else if ( target.isOneNamedGraph() )
            { 
                //out.print("GRAPH ") ;
                String s = FmtUtils.stringForNode(target.getGraph(), sCxt) ;
                out.print(s) ;
            }
            else
            {
                out.print("Target BROKEN / Update2") ;
                throw new ARQException("Malformed Target / Update2") ;
            }
        }
        
        private void printUpdate2(UpdateBinaryOp update, String name)
        {
            out.print(name) ;
            if ( update.getSilent() )
                out.print(" SILENT") ;
            out.print(" ") ;
            printTargetUpdate2(update.getSrc()) ;
            out.print(" TO ") ;
            printTargetUpdate2(update.getDest()) ;
        }
        
        
        public void visit(UpdateAdd update)
        { printUpdate2(update, "ADD") ; }

        public void visit(UpdateCopy update)
        { printUpdate2(update, "COPY") ; }

        public void visit(UpdateMove update)
        { printUpdate2(update, "MOVE") ; }

        public void visit(UpdateDataInsert update)
        {
            out.ensureStartOfLine() ;
            out.print("INSERT DATA ") ;
            outputQuadsBraced(update.getQuads()) ;
        }

        public void visit(UpdateDataDelete update)
        {
            out.ensureStartOfLine() ;
            out.print("DELETE DATA ") ;
            outputQuadsBraced(update.getQuads()) ;
        }

        // Prettier later.
        
        private void outputQuadsBraced(List<Quad> quads)
        {
            if ( quads.size() == 0 )
            {
                out.print("{ }") ;
                return ;
            }
            out.println("{") ;
            outputQuads(quads) ;
            out.print("}") ;
        }
        
        private void outputQuads(List<Quad> quads)
        {
            out.incIndent(BLOCK_INDENT) ;
            Node g = Quad.tripleInQuad ;
            boolean inBlock = false ;
            for ( Quad q : quads )
            {
                if ( q.getGraph() != g )
                {
                    // New graph (default or named)
                    if ( inBlock )
                    {
                        // In named - end it.
                        out.decIndent(BLOCK_INDENT) ;
                        out.println("}") ;
                        inBlock = false ;
                    }
                    
                    g = q.getGraph() ;
                    
                    // Start new block.
                    // Two cases for no braces: 
                    // Quad.tripleInQuad and Quad.defaultGraphNodeGenerated ;
                    if ( ! q.isTriple() && ! q.isDefaultGraphGenerated() )
                    {
                        out.print("GRAPH ") ;
                        output(g) ;
                        out.println(" {") ;
                        out.incIndent(BLOCK_INDENT) ;
                        inBlock = true ;
                    }
                }
                    
                outputTripleOfQuad(q) ;
                out.println(" .") ;
            }
            
            if ( inBlock )
            {
                out.decIndent(BLOCK_INDENT) ;
                out.println("}") ;
                inBlock = false ;
            }
            out.decIndent(BLOCK_INDENT) ;
        }
        
        private void output(Node node)
        { 
            String $ = FmtUtils.stringForNode(node, sCxt) ;
            out.print($) ;
        }

        private void outputQuad(Quad quad)
        {
            String qs = FmtUtils.stringForQuad(quad, sCxt.getPrefixMapping()) ;
            
            if ( quad.getGraph() != null )
            {
                String g = FmtUtils.stringForNode(quad.getGraph(), sCxt) ;
                out.print(g) ;
                out.print(" ") ;    
            }
            outputTripleOfQuad(quad) ;
            out.println(" .") ;
        }

        private void outputTripleOfQuad(Quad quad)
        {
            String s = FmtUtils.stringForNode(quad.getSubject(), sCxt) ;
            String p = FmtUtils.stringForNode(quad.getPredicate(), sCxt) ;
            String o = FmtUtils.stringForNode(quad.getObject(), sCxt) ;
            
            out.print(s) ;
            out.print(" ") ;
            out.print(p) ;
            out.print(" ") ;
            out.print(o) ;
        }
        

        
        public void visit(UpdateDeleteWhere update)
        {
            out.ensureStartOfLine() ;
            out.println("DELETE WHERE ") ;
            outputQuadsBraced(update.getQuads()) ;
        }

        public void visit(UpdateModify update)
        {
            out.ensureStartOfLine() ;
            if ( update.getWithIRI() != null )
            {
                //out.ensureStartOfLine() ;
                out.print("WITH ") ;
                output(update.getWithIRI()) ;
            }
            
            
            if ( update.hasDeleteClause() )
            {
                List<Quad> deleteQuads = update.getDeleteQuads() ;
                out.ensureStartOfLine() ;
                out.print("DELETE ") ;
                outputQuadsBraced(deleteQuads) ;
            }
            
            
            if ( update.hasInsertClause() )
            {
                List<Quad> insertQuads = update.getInsertQuads() ;
                out.ensureStartOfLine() ;
                out.print("INSERT ") ;
                outputQuadsBraced(insertQuads) ;
            }
            
            if ( ! update.hasInsertClause() && ! update.hasDeleteClause() )
            {
                // Fake a clause to make it legal syntax.
                out.ensureStartOfLine() ;
                out.println("INSERT { }") ;
            }
            
            for ( Node x : update.getUsing() )
            {
                out.ensureStartOfLine() ;
                out.print("USING ") ;
                output(x) ;
            }
            
            for ( Node x : update.getUsingNamed() )
            {
                out.ensureStartOfLine() ;
                out.print("USING NAMED ") ;
                output(x) ;
            }
             
            Element el = update.getWherePattern() ;
            out.ensureStartOfLine() ;
            out.print("WHERE") ;
            out.incIndent(BLOCK_INDENT) ;
            out.newline() ;

            if ( el != null )
            {
                FormatterElement fmtElement = new FormatterElement(out, sCxt) ;
                fmtElement.visitAsGroup(el) ;
            }
            else
                out.print("{}") ;
            out.decIndent(BLOCK_INDENT) ;
        }
    }
}
