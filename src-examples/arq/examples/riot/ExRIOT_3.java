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

import org.openjena.riot.RiotLoader ;
import org.openjena.riot.SysRIOT ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** Example of using RIOT : reading dada into datasets.
 */
public class ExRIOT_3
{
    public static void main(String...argv)
    {
        // Ensure RIOT loaded.
        // This is only needed to be sure - touching any ARQ code will load RIOT.
        // This operation can be called several times.
        SysRIOT.wireIntoJena() ;
        DatasetGraph dsg = null ;
        
        // Read a TriG file into quad storage in-memory.
        dsg = RiotLoader.load("data.trig") ;
        
        // read some (more) data into a dataset graph.
        RiotLoader.read("data2.trig", dsg) ;
        
        // Create a daatset,
        Dataset ds = DatasetFactory.create() ;
        // read in data.
        RiotLoader.read("data2.trig", ds.asDatasetGraph()) ;

    }
}
