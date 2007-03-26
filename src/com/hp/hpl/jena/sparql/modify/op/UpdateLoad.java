/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.op;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.modify.UpdateVisitor;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.util.FileManager;

public class UpdateLoad extends GraphUpdate1
{
    List loadData = new ArrayList() ;
    
    public UpdateLoad() {}
    
    /** Load the default graph with the contents of web resource iri */
    public UpdateLoad(String iri) { super() ; addLoadIRI(iri) ; }
    /** Load the contents of web resource iri into graph graphName */
    public UpdateLoad(String iri, String graphName) { super(graphName) ; addLoadIRI(iri) ; }
    
    public void addLoadIRI(String iri) { loadData.add(iri) ; }
    
    //@Override
    protected void exec(GraphStore graphStore, Graph graph)
    {
        Model model = ModelFactory.createModelForGraph(graph) ;
        for ( Iterator iter = loadData.iterator() ; iter.hasNext() ; )
        {
            String s = (String)iter.next() ;
            FileManager.get().readModel(model, s) ;
        }
    }

    //@Override
    public void visit(UpdateVisitor visitor) { visitor.visit(this) ; }

    public List getLoadIRIs()
    { return loadData ; }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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
