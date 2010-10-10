/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.client;

import org.openjena.fuseki.DatasetUpdater ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

/** Adapter between Dataset/Model level and DatasetGraph/Graph level */ 
public class DatasetUpdaterAdapter implements DatasetUpdater
{
    private final DatasetGraphUpdater updater ;

    public DatasetUpdaterAdapter(DatasetGraphUpdater updater) { this.updater = updater ; }
    
    /** Get the default model of a Dataset */
    @Override
    public Model getModel()
    {
        Graph g = updater.httpGet() ;
        return ModelFactory.createModelForGraph(g) ;
    }

    /** Get a named model of a Dataset */
    @Override
    public Model getModel(String graphUri)
    {
        Graph g = updater.httpGet(Node.createURI(graphUri)) ;
        return ModelFactory.createModelForGraph(g) ;
    }

    /** Put (replace) the default model of a Dataset */
    @Override
    public void putModel(Model data)
    {
        updater.httpPut(data.getGraph()) ;
    }

    /** Put (create/replace) a named model of a Dataset */
    @Override
    public void putModel(String graphUri, Model data)
    {
        updater.httpPut(Node.createURI(graphUri), data.getGraph()) ;
    }

    /** Delete (which means clear) the default model of a Dataset */
    @Override
    public void deleteDefault()
    {
        updater.httpDelete() ;
    }

    /** Delete a named model of a Dataset */
    @Override
    public void deleteModel(String graphUri)
    {
        updater.httpDelete(Node.createURI(graphUri)) ;
    }

    /** Add statements to the default model of a Dataset */
    @Override
    public void add(Model data)
    {
        updater.httpPost(data.getGraph()) ;   
    }
    
    /** Add statements to a named model of a Dataset */
    @Override
    public void add(String graphUri, Model data)
    {
        updater.httpPost(Node.createURI(graphUri), data.getGraph()) ;
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