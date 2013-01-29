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

package jena.examples.rdf ;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.io.PrintWriter;

/** Tutorial 11 - more on literals
 */
public class Tutorial11 extends Object {
    
      public static void main (String args[]) {
        // create an empty graph
        Model model = ModelFactory.createDefaultModel();

       // create the resource
       Resource r = model.createResource();                                     

      // add the property
      r.addProperty(RDFS.label, model.createLiteral("chat", "en"))
       .addProperty(RDFS.label, model.createLiteral("chat", "fr"))
       .addProperty(RDFS.label, model.createLiteral("<em>chat</em>", true));
      
      // write out the graph
      model.write(new PrintWriter(System.out));
      System.out.println();
      
      // create an empty graph
      model = ModelFactory.createDefaultModel();

       // create the resource
       r = model.createResource();                                     

      // add the property
      r.addProperty(RDFS.label, "11")
       .addLiteral(RDFS.label, 11);
      
      // write out the graph
      model.write( System.out, "N-TRIPLE");
      }
}
