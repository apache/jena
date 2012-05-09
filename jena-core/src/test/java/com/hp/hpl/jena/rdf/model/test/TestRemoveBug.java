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

package com.hp.hpl.jena.rdf.model.test;

import java.io.StringReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $
 */

public class TestRemoveBug extends TestCase {
    
    public TestRemoveBug(String name) {
        super(name);
    }
    
    public static TestSuite suite() {
        return new TestSuite( TestRemoveBug.class );
    }

    /**
     * Test a bug case, intermittent only (about 1 in 50!)
     */
    public void testBug1() {
        String src="@prefix foaf:    <http://xmlns.com/foaf/0.1/> .\n" +
        "<http://www.hp.com/people/Ian_Dickinson> foaf:mbox_sha1sum '896dfb5980f37c47ada8c2a2538888d0c39e582d' .\n" +
//        "[a foaf:Person ; foaf:name 'Ian Dickinson'  ; foaf:title 'Mr'  ;" +
//        " foaf:givenname 'Ian'  ; foaf:family_name 'Dickinson' ;\n" +
//        " foaf:mbox_sha1sum '896dfb5980f37c47ada8c2a2538888d0c39e582d'  ;" +
//        " foaf:homepage <http://www.iandickinson.me.uk>;\n" +
//        " foaf:phone <tel:+44-(117)-312-8796> ; " +
//        " foaf:depiction <http://www.iandickinson.me.uk/images/me2005.jpg>;" +
//        " foaf:workInfoHomepage <http://www.hpl.hp.com/semweb>" +
        "[] foaf:name 'Ian Dickinson'  ;\n" +
        " foaf:p1 'v1'; \n" +
        " foaf:p1 'v2'; \n" +
        " foaf:p1 'v3'; \n" +
        " foaf:p1 'v4'; \n" +
        " foaf:p1 'v5'; \n" +
        " foaf:p1 'v6'; \n" +
        " foaf:p1 'v7'; \n" +
        " foaf:p1 'v8'; \n" +
        " foaf:p1 'v9'; \n" +
        "." ;
        
        for (int count = 0; count < 1000; count++) {
//            System.out.println("Test " + count);
            Model incoming = ModelFactory.createDefaultModel();
            incoming.read( new StringReader(src), null, "N3");

            // Find the bNode that will be rewritten
            Property name = incoming.createProperty("http://xmlns.com/foaf/0.1/", "name");
            ResIterator ri = incoming.listSubjectsWithProperty(name, "Ian Dickinson");
            Resource bNode = ri.nextResource();
            ri.close();
            
            // Rewrite it to ground form
            int originalCount = bNode.listProperties().toList().size();
            Resource newR = incoming.createResource("http://www.hp.com/people/Ian_Dickinson");
            int runningCount = 0;
            StmtIterator si = incoming.listStatements(bNode, null, (RDFNode)null);
            Model additions = ModelFactory.createDefaultModel();
            while (si.hasNext()) {
                Statement s = si.nextStatement();
                runningCount += 1;
                si.remove();
//                System.out.println("Rewrite " + s + " base on " + newR);
                additions.add(additions.createStatement(newR, s.getPredicate(), s.getObject()));
            }
            assertEquals( "on iteration " + count + " with " + bNode.asNode().getBlankNodeLabel(), originalCount, runningCount );
            incoming.add(additions);
            Resource ian = incoming.getResource("http://www.hp.com/people/Ian_Dickinson");
            assertTrue("Smush failed on iteration " + count, ian.hasProperty(name));
        }
    }
}
