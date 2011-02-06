/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package reports.archive;


import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;


public class ReportUpdate1
{
    public static void main (String... argv)
    {
        GraphStore graphStore = GraphStoreFactory.create() ;

        String q = 
            "CREATE GRAPH <example:store> \n" +  // create a graph
            "INSERT INTO  <example:store> { _:foo a <example:Thing> . } \n" + // add some data
            "DELETE FROM  <example:store> { ?s ?p ?o } WHERE { ?s ?p ?o } \n"; // remove all data

        UpdateRequest req = UpdateFactory.create(q) ;
        
        UpdateRequest req1 = UpdateFactory.create("CREATE GRAPH <example:store>") ;
        UpdateRequest req2 = UpdateFactory.create("INSERT INTO  <example:store> { _:foo a <example:Thing> . }") ;
        UpdateRequest req3 = UpdateFactory.create("DELETE FROM  <example:store> { ?s ?p ?o } WHERE { ?s ?p ?o }") ;
        UpdateRequest req4 = UpdateFactory.create("DELETE FROM  <example:store> { ?s ?p ?o } WHERE { GRAPH <example:store> {?s ?p ?o } }") ;
        
        
        //System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        UpdateAction.execute(req1, graphStore);
        System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        UpdateAction.execute(req2, graphStore);
        System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        UpdateAction.execute(req4, graphStore);
        System.out.println("---------------------") ;
        SSE.write(graphStore) ;
        System.out.println("---------------------") ;
        
        //UpdateAction.execute(UpdateFactory.create(q), graphStore);
        SSE.write(graphStore) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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