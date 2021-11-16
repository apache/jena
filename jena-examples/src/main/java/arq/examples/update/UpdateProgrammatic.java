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

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.modify.request.Target ;
import org.apache.jena.sparql.modify.request.UpdateCreate ;
import org.apache.jena.sparql.modify.request.UpdateDrop ;
import org.apache.jena.sparql.modify.request.UpdateLoad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.update.* ;

/** Build an update request up out of individual Update objects, not by parsing.
 *  This is quite low-level.
 *  See UpdateExecuteOperations for ways to build the request up from strings. 
 *  These two approaches can be mixed.
 */

public class UpdateProgrammatic
{
    static { LogCtl.setLogging(); }
    public static void main(String []args)
    {
        Dataset dataset = DatasetFactory.createTxnMem() ;
        
        UpdateRequest request = UpdateFactory.create() ;
        
        request.add(new UpdateDrop(Target.ALL)) ;
        request.add(new UpdateCreate("http://example/g2")) ;
        request.add(new UpdateLoad("file:etc/update-data.ttl", "http://example/g2")) ;
        UpdateAction.execute(request, dataset) ;
        
        System.out.println("# Debug format");
        SSE.write(dataset) ;
        
        System.out.println();
        
        System.out.println("# N-Quads: S P O G") ;
        RDFDataMgr.write(System.out, dataset, Lang.NQUADS) ;
    }
}
