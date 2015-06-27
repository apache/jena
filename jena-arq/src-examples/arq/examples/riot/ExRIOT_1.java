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

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;

/** Example of using RIOT with Jena readers.
 * An application can use model.read or RDFDataMgr.
 */
public class ExRIOT_1
{
    public static void main(String...argv)
    {
        Model m = ModelFactory.createDefaultModel() ;
        // read into the model.
        m.read("data.ttl") ;
        
        // Alternatively, use the RDFDataMgr, which reads from the web,
        // with content negotiation.  Plain names are assumed to be 
        // local files where file extension indicates the syntax.  
        
        Model m2 = RDFDataMgr.loadModel("data.ttl") ;
        
        // read in more data, the remote server serves up the data
        // with the right MIME type.
        RDFDataMgr.read(m2, "http://host/some-published-data") ;
        
        
        // Read some data but also give a hint for the synatx if it is not
        // discovered by inspectying the file or by HTTP content negotiation.  
        RDFDataMgr.read(m2, "some-more-data.out", RDFLanguages.TURTLE) ;
    }
}
