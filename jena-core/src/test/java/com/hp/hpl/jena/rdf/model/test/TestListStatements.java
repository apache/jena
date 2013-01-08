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

package com.hp.hpl.jena.rdf.model.test;

import static org.junit.Assert.assertTrue ;

import java.util.List ;

import junit.framework.JUnit4TestAdapter ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.rdf.model.* ;

public class TestListStatements
{
    private static Model m;
    private static Resource s;
    private static Property p;

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestListStatements.class) ;
    }
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        m = ModelFactory.createDefaultModel();
        Resource s = m.createResource("http://www.a.com/s");
        Property p = m.createProperty("http://www.a.com/p");

        m.add(s,p,m.createResource("http://www.a.com/o"));
        m.add(s,p,"texte","fr");
        m.add(s,p,"text","en");
        m.add(s,p,"text");
        m.add(m.createLiteralStatement(s,p,1789));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        m = null;
        s = null;
        p = null;
    }

    @Test
    public final void thereAre4Literals() {     
        StmtIterator it = m.listStatements(s, p, null, null);
        assertTrue(it.toList().size() == 4);    
    }

    @Test
    public final void thereIsOneFrench() {      
        StmtIterator it = m.listStatements(s, p, null, "fr");
        List<Statement> lis = it.toList();
        assertTrue(lis.size() == 1);    
        assertTrue(lis.get(0).getObject().toString().equals("texte@fr"));   
    }

    @Test
    public final void thereAre2LitsWoLang() {       
        StmtIterator it = m.listStatements(s, p, null, "");
        assertTrue(it.toList().size() == 2);    
    }

    @Test
    public final void theresOneTextEN() {       
        StmtIterator it = m.listStatements(s, p, "text", "en");
        List<Statement> lis = it.toList();
        assertTrue(lis.size() == 1);    
        assertTrue(lis.get(0).getObject().toString().equals("text@en"));    
    }

    @Test
    public final void theresOneTextWoLang() {       
        StmtIterator it = m.listStatements(s, p, "text", "");
        List<Statement> lis = it.toList();
        assertTrue(lis.size() == 1);    
    }

    @Test
    public final void theresAreTwoText() {      
        StmtIterator it = m.listStatements(s, p, "text", null);
        List<Statement> lis = it.toList();
        assertTrue(lis.size() == 2);    
    }
}

