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

package dev.reports;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.ReifiedStatement ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sdb.SDBFactory ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class Report_ReifiedStatements
{
    public static void main(String...argv)
    {
        // Add stmt to in memory model and reify it:
        Model model = ModelFactory.createDefaultModel();
        
        Store store = SDBFactory.connectStore("sdb.ttl") ;
        store.getTableFormatter().create() ;
        
        Model sdbModel = SDBFactory.connectDefaultModel(store) ;
        
        Statement stmt = model.createStatement(ResourceFactory
                                               .createResource("urn:test:t1"), RDFS.label, "foo1");
        
        model.add(stmt);
        ReifiedStatement rs = stmt.createReifiedStatement();
        rs.addProperty(ResourceFactory.createProperty("urn:property:foo"),"Foo");

        System.out.println("Model:");
        model.write(System.out, "N-TRIPLES");
        System.out.println();

        // now add in memory model to SDB model
        sdbModel.add(model); // <-------  CannotReifyException THROWN HERE!!!!!!!!!!!!!!!

        System.out.println("SDB Model:");
        sdbModel.write(System.out, "N-TRIPLES");
        System.out.println();

        
        System.out.println("DONE");
    }
}
