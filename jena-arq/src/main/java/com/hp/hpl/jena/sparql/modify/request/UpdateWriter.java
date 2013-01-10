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

package com.hp.hpl.jena.sparql.modify.request;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.riot.out.SinkQuadBracedOutput ;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataWriter.UpdateMode;
import com.hp.hpl.jena.sparql.serializer.FormatterElement;
import com.hp.hpl.jena.sparql.serializer.PrologueSerializer;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode ;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateWriter implements Closeable
{
    private final IndentedWriter out;
    private final SerializationContext sCxt;
    
    private UpdateDataWriter udw;
    private boolean firstOp = true;
    private boolean opened = false;
    
    public UpdateWriter(IndentedWriter out, SerializationContext sCxt)
    {
        if (out == null)
            throw new IllegalArgumentException("out may not be null") ;
        
        // To get legal syntax out, the serialization context 
        // has to be a bNode mapping that does ??N vars to bNodes
        if (sCxt == null)
            sCxt = new SerializationContext((Prologue)null, new NodeToLabelMapBNode());
        
        this.out = out;
        this.sCxt = sCxt;
    }
    
    public void open()
    {
        if (null != sCxt)
            prologue();
        opened = true;
    }
    
    private void checkOpen()
    {
        if (!opened)
            throw new IllegalStateException("UpdateStreamWriter is not opened.  Call open() first.");
    }
    
    private void prologue()
    {
        int row1 = out.getRow() ;
        PrologueSerializer.output(out, sCxt.getPrologue()) ;
        int row2 = out.getRow() ;
        if ( row1 != row2 )
            out.newline() ;
    }
    
    private void prepareForDataUpdate(UpdateMode mode)
    {
        if ((null != udw) && !udw.getMode().equals(mode))
        {
            udw.close();
            udw = null;
            firstOp = false;
        }
        
        if (null == udw)
        {
            if (!firstOp)
            {
                out.println(" ;");
            }
            udw = new UpdateDataWriter(mode, out, sCxt);
            udw.open();
            firstOp = false;
        }
    }
    
    public void insert(Quad quad)
    {
        insert(quad.getGraph(), quad.asTriple());
    }
    
    public void insert(Iterator<? extends Quad> it)
    {
        checkOpen();
        prepareForDataUpdate(UpdateMode.INSERT);
        while (it.hasNext())
        {
            udw.send(it.next());
        }
    }
    
    public void insert(Node graph, Triple triple)
    {
        checkOpen();
        prepareForDataUpdate(UpdateMode.INSERT);
        udw.send(graph, triple);
    }
    
    public void insert(Node graph, Iterator<? extends Triple> it)
    {
        checkOpen();
        prepareForDataUpdate(UpdateMode.INSERT);
        while (it.hasNext())
        {
            udw.send(graph, it.next());
        }
    }
    
    public void delete(Quad quad)
    {
        delete(quad.getGraph(), quad.asTriple());
    }
    
    public void delete(Iterator<? extends Quad> it)
    {
        checkOpen();
        prepareForDataUpdate(UpdateMode.DELETE);
        while (it.hasNext())
        {
            udw.send(it.next());
        }
    }
    
    public void delete(Node graph, Triple triple)
    {
        checkOpen();
        prepareForDataUpdate(UpdateMode.DELETE);
        udw.send(graph, triple);
    }
    
    public void delete(Node graph, Iterator<? extends Triple> it)
    {
        checkOpen();
        prepareForDataUpdate(UpdateMode.DELETE);
        while (it.hasNext())
        {
            udw.send(graph, it.next());
        }
    }
    
    public void update(Update update)
    {
        checkOpen();
        if (null != udw)
        {
            udw.close();
            udw = null;
        }
        
        if (!firstOp)
        {
            out.println(" ;");
        }
        Writer writer = new Writer(out, sCxt) ;
        update.visit(writer) ; 
        
        firstOp = false;
    }
    
    public void update(Iterable<? extends Update> updates)
    {
        update(updates.iterator());
    }
    
    public void update(Iterator<? extends Update> updateIter)
    {
        while (updateIter.hasNext())
        {
            update(updateIter.next());
        }
    }
    
    public void flush()
    {
        out.flush();
    }
    
    @Override
    public void close()
    {
        if (opened)
        {
            if (null != udw)
            {
                udw.close();
                udw = null;
            }
            
            // Update requests always end in newline.
            out.ensureStartOfLine();
            flush();
            opened = false;
        }
    }
    
    
    // -- Convenience static methods -----------------------
    
    public static void output(UpdateRequest request, IndentedWriter out)
    {
        output(request, out, null);
    }
    
    public static void output(UpdateRequest request, IndentedWriter out, SerializationContext sCxt)
    {
        UpdateWriter uw = new UpdateWriter(out, sCxt);
        uw.open();
        uw.update(request);
        uw.close();
    }
    
    public static void output(Update update, IndentedWriter out, SerializationContext sCxt)
    {
        UpdateWriter uw = new UpdateWriter(out, sCxt);
        uw.open();
        uw.update(update);
        uw.close();
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

        @Override
        public void visit(UpdateDrop update)
        { visitDropClear("DROP", update) ; }

        @Override
        public void visit(UpdateClear update)
        { visitDropClear("CLEAR", update) ; }

        @Override
        public void visit(UpdateCreate update)
        {
            out.ensureStartOfLine() ;
            out.print("CREATE") ;
            out.print(" ") ;
            if ( update.isSilent() )
                out.print("SILENT ") ;
            out.print("GRAPH") ;
            out.print(" ") ;
            String s = FmtUtils.stringForNode(update.getGraph(), sCxt) ;
            out.print(s) ;
        }

        @Override
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
        
        
        @Override
        public void visit(UpdateAdd update)
        { printUpdate2(update, "ADD") ; }

        @Override
        public void visit(UpdateCopy update)
        { printUpdate2(update, "COPY") ; }

        @Override
        public void visit(UpdateMove update)
        { printUpdate2(update, "MOVE") ; }

        @Override
        public void visit(UpdateDataInsert update)
        {
            UpdateDataWriter udw = new UpdateDataWriter(UpdateMode.INSERT, out, sCxt);
            udw.open();
            Iter.sendToSink(update.getQuads(), udw);  // udw.close() is called by Iter.sendToSink()
        }

        @Override
        public void visit(UpdateDataDelete update)
        {
            UpdateDataWriter udw = new UpdateDataWriter(UpdateMode.DELETE, out, sCxt);
            udw.open();
            Iter.sendToSink(update.getQuads(), udw);
        }

        // Prettier later.
        
        private void outputQuadsBraced(List<Quad> quads)
        {
            if ( quads.size() == 0 )
            {
                out.print("{ }") ;
                return ;
            }
            
            SinkQuadBracedOutput sink = new SinkQuadBracedOutput(out, sCxt);
            sink.open();
            Iter.sendToSink(quads, sink);
        }
        
        private void output(Node node)
        { 
            String $ = FmtUtils.stringForNode(node, sCxt) ;
            out.print($) ;
        }

        
        @Override
        public void visit(UpdateDeleteWhere update)
        {
            out.ensureStartOfLine() ;
            out.println("DELETE WHERE ") ;
            outputQuadsBraced(update.getQuads()) ;
        }

        @Override
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
