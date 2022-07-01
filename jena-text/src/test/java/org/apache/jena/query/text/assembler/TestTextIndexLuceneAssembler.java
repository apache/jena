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

package org.apache.jena.query.text.assembler;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.query.text.TextIndexLucene ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.lucene.analysis.core.KeywordAnalyzer ;
import org.apache.lucene.store.ByteBuffersDirectory ;
import org.junit.Test ;
import static org.junit.Assert.* ;

public class TestTextIndexLuceneAssembler extends AbstractTestTextAssembler {
    
    @Test public void testIndexHasEntityMap() {
        TextIndexLucene index = (TextIndexLucene) Assembler.general.open(SIMPLE_INDEX_SPEC);
        try {
            assertEquals(RDFS.label.asNode(), index.getDocDef().getPrimaryPredicate());
        }
        finally {
            index.close();
        }
    }

    @Test public void testLiteralDirectory() {
        TextIndexLuceneAssembler assembler = new TextIndexLuceneAssembler();
        
        Resource root = SIMPLE_INDEX_SPEC_LITERAL_DIR;
        Assembler a = Assembler.general;
        // the open method is not supposed to throw exceptions when the directory is
        // a literal
        TextIndexLucene index = (TextIndexLucene)assembler.open(a, root, /*mode*/ null);
        try {
            assertNotNull(index);
        }
        finally {
            index.close();
        }
    }

    @Test public void testResourceDirectory() {
        TextIndexLuceneAssembler assembler = new TextIndexLuceneAssembler();

        Resource root = SIMPLE_INDEX_SPEC2;
        Assembler a = Assembler.general;
        // the open method is not supposed to throw exceptions when the directory is
        // a resource
        TextIndexLucene index = (TextIndexLucene) assembler.open(a, root, /*mode*/ null);
        try {
            assertFalse(index.getDirectory() instanceof ByteBuffersDirectory);
            assertNotNull(index.getQueryAnalyzer());
        }
        finally {
            index.close();
        }
    }

    @Test public void testMemDirectory() {
        TextIndexLuceneAssembler assembler = new TextIndexLuceneAssembler();

        Resource root = SIMPLE_INDEX_SPEC_MEM_DIR;
        Assembler a = Assembler.general;
        // the open method is not supposed to throw exceptions when the directory is
        // a iri resource
        TextIndexLucene index = (TextIndexLucene) assembler.open(a, root, /*mode*/ null);
        try {
            assertTrue(index.getDirectory() instanceof ByteBuffersDirectory);
        }
        finally {
            index.close();
        }
    }
    
    @Test public void testQueryAnalyzer() {
        TextIndexLucene index = (TextIndexLucene) Assembler.general.open(SIMPLE_INDEX_SPEC_QUERY_ANALYZER);
        try {
            assertTrue(index.getQueryAnalyzer() instanceof KeywordAnalyzer);
        }
        finally {
            index.close();
        }
    }

    static {
        JenaSystem.init();
        TextAssembler.init();
    }

}
