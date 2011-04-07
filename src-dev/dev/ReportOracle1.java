/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.update.UpdateAction ;

public class ReportOracle1
{
    public static void main(String ...argv)
    {
        DataSource ds = DatasetFactory.create();

        //ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, szModelName);
        Model model = ModelFactory.createDefaultModel() ;
        model.getGraph().add(Triple.create(Node.createURI("http://example.org/bob"),
                                           Node.createURI("http://purl.org/dc/elements/1.1/publisher"),
                                           Node.createLiteral("Bob Hacker")));
        model.getGraph().add(Triple.create(Node.createURI("http://example.org/alice"),
                                           Node.createURI("http://purl.org/dc/elements/1.1/publisher"),
                                           Node.createLiteral("alice Hacker")));


        //ModelOracleSem model1 = ModelOracleSem.createOracleSemModel(oracle, szModelName+"1");
        Model model1 = ModelFactory.createDefaultModel() ;

        model1.getGraph().add(Triple.create(Node.createURI("urn:bob"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/name"),
                                            Node.createLiteral("Bob")
        ));
        model1.getGraph().add(Triple.create(Node.createURI("urn:bob"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/mbox"),
                                            Node.createURI("mailto:bob@example")
        ));

        //ModelOracleSem model2 = ModelOracleSem.createOracleSemModel(oracle, szModelName+"2");
        Model model2 = ModelFactory.createDefaultModel() ;
        model2.getGraph().add(Triple.create(Node.createURI("urn:alice"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/name"),
                                            Node.createLiteral("Alice")
        ));
        model2.getGraph().add(Triple.create(Node.createURI("urn:alice"),
                                            Node.createURI("http://xmlns.com/foaf/0.1/mbox"),
                                            Node.createURI("mailto:alice@example")
        ));

        ds.setDefaultModel(model);
        //ds.addNamedModel("<http://example.org/bob>",model1);
        ds.addNamedModel("http://example.org/bob",model1);
        // ds.addNamedModel("http://example.org/alice",model2);

        String insertString =
            "INSERT DATA <http://example.org/bob> {<urn:alice> <urn:loves> <urn:apples> } ";
        UpdateAction.parseExecute(insertString, ds); 
        System.out.println("DONE") ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */