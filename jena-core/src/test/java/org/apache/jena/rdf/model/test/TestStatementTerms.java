/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rdf.model.test;

import org.junit.Assert;

import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.test.helpers.ModelHelper;
import org.apache.jena.rdf.model.test.helpers.TestingModelFactory;
import org.apache.jena.vocabulary.RDF;

public class TestStatementTerms extends AbstractModelTestBase {
    public TestStatementTerms(TestingModelFactory modelFactory, String name) {
        super(modelFactory, name);
    }

    public void testStatementTerms() {
        String fakeURI = "fake:URI";
        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);

        Statement stmt = model.createStatement(S, P, O);
        Assert.assertTrue(model.isEmpty());

        StatementTerm stmtTerm = model.createStatementTerm(stmt);
        Assert.assertTrue(model.isEmpty());

        Assert.assertEquals(S, stmtTerm.getStatement().getSubject());
        Assert.assertEquals(P, stmtTerm.getStatement().getPredicate());
        Assert.assertEquals(O, stmtTerm.getStatement().getObject());
    }

    private static StatementTerm create(Model model) {
        String fakeURI = "fake:URI";
        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);

        Statement stmt = model.createStatement(S, P, O);
        StatementTerm stmtTerm = model.createStatementTerm(stmt);
        return stmtTerm;
    }

    public void testStatementReifierAnon() {
        String fakeURI = "fake:URI";
        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);
        Statement stmt = model.createStatement(S, P, O);

        Resource r = model.createReifier(stmt);
        Assert.assertFalse(model.isEmpty());
        Assert.assertEquals(1, model.size());

        Statement s = model.listStatements().next();

        RDFNode x = s.getObject();
        Assert.assertTrue(s.getSubject().isAnon());
        Assert.assertTrue(s.getPredicate().equals(RDF.reifies));
        Assert.assertTrue(s.getObject().isStatementTerm());

        StatementTerm st = s.getObject().asStatementTerm();
        Assert.assertTrue(st != null);
        Assert.assertEquals(st.getStatement(), stmt);
    }

    public void testStatementReifierResource() {
        String fakeURI = "fake:URI";
        String reifURI = "reifier:URI";

        Resource reifier = model.createResource(reifURI);

        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);
        Statement stmt = model.createStatement(S, P, O);

        Resource r = model.createReifier(reifier, stmt);
        Assert.assertEquals(reifURI, r.getURI());

        Assert.assertFalse(model.isEmpty());
        Assert.assertEquals(1, model.size());

        StatementTerm st = r.getProperty(RDF.reifies).getObject().asStatementTerm();
        Assert.assertTrue(st != null);
        Assert.assertEquals(st.getStatement(), stmt);
    }
}
