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

package arq.examples.riot;

import static org.apache.jena.riot.RDFLanguages.TRIG ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RIOT ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;

/** Example of using RIOT : reading data into datasets. */
public class ExRIOT_3
{
    public static void main(String...argv)
    {
        // Ensure RIOT loaded.
        // This is only needed to be sure - touching any ARQ code will load RIOT.
        // This operation can be called several times.
        RIOT.init() ;
        Dataset ds = null ;
        
        // Read a TriG file into quad storage in-memory.
        ds = RDFDataMgr.loadDataset("data.trig") ;
        
        // read some (more) data into a dataset graph.
        RDFDataMgr.read(ds, "data2.trig") ;
        
        // Create a dataset,
        Dataset ds2 = DatasetFactory.createMem() ;
        // read in data, indicating the syntax in case the remote end does not
        // correctly provide the HTTP content type.
        RDFDataMgr.read(ds2, "http://host/data2.unknown", TRIG) ;
    }
}
