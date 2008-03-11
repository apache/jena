/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.update;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

/** Operations to create a GraphStore */
public class GraphStoreFactory
{
    /** Create an empty GraphStore with an empty default graph */
    public static GraphStore create() { return new GraphStoreBasic() ; }
    
    /** Create a GraphStore from a dataset */
    public static GraphStore create(Dataset ds) { return new GraphStoreBasic(ds) ; }
    
    private static class GraphStoreBasic extends DataSourceGraphImpl implements GraphStore
    {
        public GraphStoreBasic() { super.setDefaultGraph(GraphUtils.makeDefaultGraph()) ; }
        
        public GraphStoreBasic(Dataset ds) { super(ds) ; }
        
        public void execute(UpdateRequest request, Binding binding)
        { request.exec(this, binding) ; }

        public void execute(UpdateRequest request)
        { request.exec(this) ; }

        public void execute(Update graphUpdate, Binding binding)
        { execute(new UpdateRequest(graphUpdate), binding) ; }

        public void execute(Update graphUpdate)
        { execute(new UpdateRequest(graphUpdate)) ; }
        
        public Dataset toDataset()
        {
            // This is a shallow structure copy.
            return new DataSourceImpl(this) ;
        }

        public void close()
        {
            for ( Iterator iter = listGraphNodes() ; iter.hasNext() ; )
            {
                Node n = (Node)iter.next();
                getGraph(n).close();
            }
        }

        public void sync()
        {}
    }
}

 

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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