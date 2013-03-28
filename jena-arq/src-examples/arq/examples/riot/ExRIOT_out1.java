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

import org.apache.jena.riot.* ;

import com.hp.hpl.jena.rdf.model.Model ;

/** Example writing a model with RIOT */
public class ExRIOT_out1
{
    public static void main(String[] args)
    {
        Model model = RDFDataMgr.loadModel("D.ttl") ;
        
        System.out.println() ;
        System.out.println("#### ---- Write as Turtle") ;
        System.out.println() ;
        RDFDataMgr.write(System.out, model, Lang.TURTLE) ;
        
        System.out.println() ;
        System.out.println("#### ---- Write as Turtle (streaming)") ;
        System.out.println() ;
        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_BLOCKS) ;
        
        System.out.println() ;
        System.out.println("#### ---- Write as Turtle via model.write") ;
        System.out.println() ;
        model.write(System.out, "TTL") ;
    }

}

