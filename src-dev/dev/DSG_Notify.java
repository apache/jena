/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import org.openjena.atlas.event.Event ;
import org.openjena.atlas.event.EventManager ;
import org.openjena.atlas.event.EventType ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** DatasetGraphWrapper that adds
 * events to change operations 
 * on a DatasetGraph. */

public class DSG_Notify extends DatasetGraphWrapper
{
    //Enum-ization?
    static final String URI                 = "http://openjena.org/TDB/dataset" ;
    static final EventType etAddQuad        = new EventType(URI+"addQuad") ;
    static final EventType etDeleteQuad     = new EventType(URI+"deleteQuad") ;
    static final EventType etDeleteAnyQuad  = new EventType(URI+"deleteAnyQuad") ;
    static final EventType etAddGraph       = new EventType(URI+"addGraph") ;
    static final EventType etRemoveQuad     = new EventType(URI+"removeGraph") ;
    static final EventType etClose          = new EventType(URI+"close") ;
    
    public DSG_Notify(DatasetGraph dsg)
    {
        super(dsg) ;
    }

    @Override
    public void add(Quad quad)
    {
        Event ev = new Event(etAddQuad, quad) ;
        EventManager.send(this, ev) ;
        super.add(quad) ;
    }

    @Override
    public void delete(Quad quad)
    {
        
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o)
    {}

    @Override
    public void setDefaultGraph(Graph g)
    {}

    @Override
    public void addGraph(Node graphName, Graph graph)
    {}

    @Override
    public void removeGraph(Node graphName)
    {}

    @Override
    public void close()
    {}
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