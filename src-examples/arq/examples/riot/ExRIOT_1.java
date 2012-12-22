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

import org.apache.jena.riot.IO_Jena ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.util.FileManager ;

/** Example of using RIOT with Jena readers.
 */
public class ExRIOT_1
{
    public static void main(String...argv)
    {
        // Ensure RIOT loaded.
        // This is only needed to be sure - touching any ARQ code will load RIOT.
        // This operation can be called several times.
        IO_Jena.wireIntoJena() ;

        Model m = null ;
        
        // Load data, creating the model
        m = FileManager.get().loadModel("D.ttl") ;
        
        // Or read into an existing model.
        FileManager.get().readModel(m, "D2.ttl") ;
        
        // Or use Model.read
        m.read("D3.nt", "TTL") ;
        
        // Go back to using the old Jena readers.  
        IO_Jena.resetJenaReaders() ;
        m = FileManager.get().loadModel("D.nt") ;
    }
}
