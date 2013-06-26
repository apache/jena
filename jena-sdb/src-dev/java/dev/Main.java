/**
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

package dev;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sdb.SDBFactory ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class Main
{

    public static void main(String[] args)
    {
        Store store = SDBFactory.connectStore("Store/sdb-hsqldb-mem.ttl") ;
        store.getTableFormatter().format() ;
        store.getTableFormatter().addIndexes() ;
        
        Dataset ds = SDBFactory.connectDataset(store) ;
        DatasetGraph dsg = ds.asDatasetGraph() ;
        Quad quad = SSE.parseQuad("(<g> <s> <p> <o>)") ;
        dsg.add(quad) ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }
    
    public static void main2(String[] args)
    {
        sdb.sdbquery.main("--sdb=sdb.ttl", "--set", "sdb:unionDefaultGraph=true", "SELECT * { ?s ?p ?o }") ;
        //sdbquery.main("--sdb=sdb.ttl", "--set", "http://jena.hpl.hp.com/SDB/symbol#unionDefaultGraph=true", "SELECT * { ?s ?p ?o }") ;
    }

}



