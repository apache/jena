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

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.system.IRIResolver ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataWriter.UpdateMode ;
import com.hp.hpl.jena.sparql.serializer.PrologueSerializer ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateWriter implements UpdateSerializer
{
    protected final IndentedWriter out;
    protected final SerializationContext sCxt;
    
    protected UpdateDataWriter udw;
    protected boolean firstOp = true;
    protected boolean opened = false;
    
    /** Create a UpdateWriter for output of a single UpdateRequest.
     *  @param out
     *  @param sCxt SerializationContext - pass null for one that will produce legal output.
     */
    public UpdateWriter(IndentedWriter out, SerializationContext sCxt)
    {
        if (out == null)
            throw new NullPointerException("out") ;
        
        // To get legal syntax out, the serialization context 
        // has to be a bNode mapping that does ??N vars to bNodes
        if (sCxt == null)
            sCxt = new SerializationContext((Prologue)null, new NodeToLabelMapBNode());
        
        this.out = out;
        this.sCxt = sCxt;
    }
    
    /* (non-Javadoc)
     * @see com.hp.hpl.jena.sparql.modify.request.UpdateSerializer#open()
     */
    @Override
    public void open()
    {
        if (null != sCxt)
            prologue();
        opened = true;
    }
    
    protected void checkOpen()
    {
        if (!opened)
            throw new IllegalStateException("UpdateStreamWriter is not opened.  Call open() first.");
    }
    
    protected void prologue()
    {
        int row1 = out.getRow() ;
        PrologueSerializer.output(out, sCxt.getPrologue()) ;
        int row2 = out.getRow() ;
        if ( row1 != row2 )
            out.newline() ;
    }
    
    protected void prepareForDataUpdate(UpdateMode mode)
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
    
    /* (non-Javadoc)
     * @see com.hp.hpl.jena.sparql.modify.request.UpdateSerializer#update(com.hp.hpl.jena.update.Update)
     */
    @Override
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
        UpdateVisitor writer = prepareWriterVisitor() ;
        update.visit(writer) ; 
        
        firstOp = false;
    }

    /**
     * Prepares a visitor which is used to visit the actual updates that make up the update request and write them out
     * @return Update visitor
     */
    protected UpdateVisitor prepareWriterVisitor() {
        return new UpdateWriterVisitor(out, sCxt);
    }
    
    /* (non-Javadoc)
     * @see com.hp.hpl.jena.sparql.modify.request.UpdateSerializer#update(java.lang.Iterable)
     */
    @Override
    public void update(Iterable<? extends Update> updates)
    {
        update(updates.iterator());
    }
    
    /* (non-Javadoc)
     * @see com.hp.hpl.jena.sparql.modify.request.UpdateSerializer#update(java.util.Iterator)
     */
    @Override
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
    
    /* (non-Javadoc)
     * @see com.hp.hpl.jena.sparql.modify.request.UpdateSerializer#close()
     */
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
        Prologue prologue = request ;
        if ( ! request.explicitlySetBaseURI() )
            prologue = new Prologue(request.getPrefixMapping(), (IRIResolver)null) ;
        
        SerializationContext sCxt = new SerializationContext(prologue, new NodeToLabelMapBNode()) ;
        output(request, out, sCxt);
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
}
