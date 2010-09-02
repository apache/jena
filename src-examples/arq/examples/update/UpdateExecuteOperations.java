/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.update;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.RiotWriter ;

import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** Build an update request up out of indvidiual Updates specified as strings.
 *  See UpdatePorgrammatic for another way to build up a request. 
 *  These two approaches can be mixed.
 */

public class UpdateExecuteOperations
{
    public static void main(String []args)
    {
        // Create an empty GraphStore (has an empty default graph and no named graphs) 
        GraphStore graphStore = GraphStoreFactory.create() ;
        ex1(graphStore) ;
        ex2(graphStore) ;
        ex3(graphStore) ;
    }
    
    public static void ex1(GraphStore graphStore)
    {
        // Execute one operation.
        UpdateAction.parseExecute("LOAD <file:etc/update-data.ttl>", graphStore) ;
    }
    
    public static void ex2(GraphStore graphStore)
    {
        // Execute a series of operations at once.
        // See ex3 for a better way to build up a request
        // For maximum portability, multiple operations should be separated by a ";".
        // The "\n" imporves readability and parser error messages.
        String cmd = StrUtils.strjoin(" ;\n",
                                      "DROP ALL",
                                      "CREATE GRAPH <http://example/g2>",
                                      "LOAD <file:etc/update-data.ttl> INTO <http://example/g2>") ;
        // check string created
        System.out.println(cmd) ;
        UpdateAction.parseExecute(cmd, graphStore) ;
    }
    
    public static void ex3(GraphStore graphStore)
    {
        // Build up the request then execute it.
        // This is the preferred way for complex sequences of operations. 
        UpdateRequest request = UpdateFactory.create() ;
        request.add("DROP ALL")
               .add("CREATE GRAPH <http://example/g2>") ;
        // Different style.
        // Equivalent to request.add("...")
        UpdateFactory.parse(request, "LOAD <file:etc/update-data.ttl> INTO <http://example/g2>") ;
        
        // And perform the operations.
        UpdateAction.execute(request, graphStore) ;
        
        System.out.println("# Debug format");
        SSE.write(graphStore) ;
        
        System.out.println();
        
        System.out.println("# N-Quads: S P O G") ;
        RiotWriter.writeNQuads(System.out, graphStore) ;

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