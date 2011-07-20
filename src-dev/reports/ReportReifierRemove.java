/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package reports;

import org.junit.Test ;
import static org.junit.Assert.* ;

import com.hp.hpl.jena.graph.Reifier ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sparql.graph.Reifier2 ;

public class ReportReifierRemove
{
    @Test
    public void testOverenthusiasticDeletion_1() {
        
        
        Model model = ModelFactory.createDefaultModel() ;
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
        model.setNsPrefix("rex", "http://jug.basistech.com/2011/01/rex-entity#") ;
        model.setNsPrefix("g", "urn:jug:global#") ;

        Resource per1 = model.createResource("urn:jug:global#per1");
        Resource per2 = model.createResource("urn:jug:global#per2");
        Property pred1 = model.createProperty("http://jug.basistech.com/2011/01/rex-entity#coOccurInSentence");
        Property pred2 = model.createProperty("http://jug.basistech.com/2011/01/rex-entity#hasSibling");
        Statement s1 = model.createStatement(per1, pred1, per2);
        Statement s2 = model.createStatement(per2, pred2, per2);
        
        s1.createReifiedStatement();
        s2.createReifiedStatement();
        
        model.write(System.out, "TTL") ;
        System.out.println() ;
        System.out.println() ;
        System.out.println() ;
        System.out.println() ;
        assertEquals(2, model.listReifiedStatements().toList().size());
        
        Reifier r = new Reifier2(model.getGraph()) ;
        
        //r = model.getGraph().getReifier() ;
        
        r.remove(s2.asTriple()) ;
        //s2.removeReification();
        model.write(System.out, "TTL") ;
        assertEquals(1, model.listReifiedStatements().toList().size());
    }
    
}
