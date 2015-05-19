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

package arq.examples.update;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

/** Build an update request up out of indvidiual Updates specified as strings.
 *  See UpdatePorgrammatic for another way to build up a request. 
 *  These two approaches can be mixed.
 */

public class UpdateExecuteOperations
{
    public static void main(String []args)
    {
        // Create an empty DatasetGraph (has an empty default graph and no named graphs) 
        Dataset graphStore = DatasetFactory.createMem() ;
        
        ex1(graphStore) ;
        ex2(graphStore) ;
        ex3(graphStore) ;
    }
    
    public static void ex1(Dataset graphStore)
    {
        // Execute one operation.
        UpdateAction.parseExecute("LOAD <file:etc/update-data.ttl>", graphStore) ;
    }
    
    public static void ex2(Dataset graphStore)
    {
        // Execute a series of operations at once.
        // See ex3 for a better way to build up a request
        // For maximum portability, multiple operations should be separated by a ";".
        // The "\n" imporves readability and parser error messages.
        String cmd = StrUtils.strjoin(" ;\n",
                                      "DROP ALL",
                                      "CREATE GRAPH <http://example/g2>",
                                      "LOAD <file:etc/update-data.ttl> INTO GRAPH <http://example/g2>") ;
        // check string created
        System.out.println(cmd) ;
        UpdateAction.parseExecute(cmd, graphStore) ;
    }
    
    public static void ex3(Dataset graphStore)
    {
        // Build up the request then execute it.
        // This is the preferred way for complex sequences of operations. 
        UpdateRequest request = UpdateFactory.create() ;
        request.add("DROP ALL")
               .add("CREATE GRAPH <http://example/g2>") ;
        // Different style.
        // Equivalent to request.add("...")
        UpdateFactory.parse(request, "LOAD <file:etc/update-data.ttl> INTO GRAPH <http://example/g2>") ;
        
        // And perform the operations.
        UpdateAction.execute(request, graphStore) ;
        
        System.out.println("# Debug format");
        SSE.write(graphStore) ;
        
        System.out.println();
        
        System.out.println("# N-Quads: S P O G") ;
        RDFDataMgr.write(System.out, graphStore, Lang.NQUADS) ;
    }
}
