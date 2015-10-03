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

package arq.examples.riot;

import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;

/** Other writer examples */
public class ExRIOT_out2
{
    public static void main(String[] args)
    {
        Model model = RDFDataMgr.loadModel("D.ttl") ;
        System.out.println() ;
        System.out.println("#### ---- Write as TriG") ;
        System.out.println() ;
        // This wil be the default graph of the dataset written.
        RDFDataMgr.write(System.out, model, Lang.TRIG) ;
        
        // Loading Turtle as Trig reads into the default graph.
        Dataset dataset = RDFDataMgr.loadDataset("D.ttl") ;
        System.out.println() ;
        System.out.println("#### ---- Write as NQuads") ;
        System.out.println() ;
        RDFDataMgr.write(System.out, dataset, Lang.NQUADS) ;
    }

}

