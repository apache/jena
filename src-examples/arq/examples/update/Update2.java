/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.update;

import com.hp.hpl.jena.sparql.modify.op.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.op.UpdateLoad;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;

/** Simple example of SPARQL/Update */ 
public class Update2
{
    public static void main(String []args)
    {
        final String graphName = "http://example/namedGraph" ;
        
        // Create an empty GraphStore (has an empty default graph and no named graphs) 
        GraphStore graphStore = GraphStoreFactory.create() ;
        
        // A sequence of operations
        UpdateRequest req = new UpdateRequest() ;
        
        // Create a named graph
        UpdateCreate c = new UpdateCreate(graphName) ;

        // Load a file into a named graph - NB order of arguments (both strings).
        UpdateLoad load = new UpdateLoad("etc/update-data.ttl", graphName) ;
        
        // Add the two operations and execute the request
        req.addUpdate(c) ;
        req.addUpdate(load) ;

        // Execute 
        UpdateAction.execute(req, graphStore) ;
        
        // Print it out (format is SSE <http://jena.hpl.hp.com/wiki/SSE>)
        // used to represent a dataset.
        // Note the empty default, unnamed graph
        SSE.write(graphStore) ;
    }
}


/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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