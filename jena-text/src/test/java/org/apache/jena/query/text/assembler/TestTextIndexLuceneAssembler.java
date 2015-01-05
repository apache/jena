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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextIndexLucene;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestTextIndexLuceneAssembler extends AbstractTestTextAssembler {
    
    @Test public void testIndexHasEntityMap() {
        TextIndexLucene indexLucene = (TextIndexLucene) Assembler.general.open(SIMPLE_INDEX_SPEC);
        assertEquals(RDFS.label.asNode(), indexLucene.getDocDef().getPrimaryPredicate());        
    }

    @Test public void testLiteralDirectory() {
        TextIndexLuceneAssembler assembler = new TextIndexLuceneAssembler();
        
        Resource root = SIMPLE_INDEX_SPEC_LITERAL_DIR;
        Assembler a = Assembler.general;
        // the open method is not supposed to throw exceptions when the directory is
        // a literal
        TextIndex index = assembler.open(a, root, /*mode*/ null);
        assertNotNull(index);
    }

    @Test public void testResourceDirectory() {
        TextIndexLuceneAssembler assembler = new TextIndexLuceneAssembler();

        Resource root = SIMPLE_INDEX_SPEC;
        Assembler a = Assembler.general;
        // the open method is not supposed to throw exceptions when the directory is
        // a resource
        TextIndexLucene index = (TextIndexLucene) assembler.open(a, root, /*mode*/ null);
        assertFalse(index.getDirectory() instanceof RAMDirectory);
    }

    @Test public void testMemDirectory() {
        TextIndexLuceneAssembler assembler = new TextIndexLuceneAssembler();

        Resource root = SIMPLE_INDEX_SPEC_MEM_DIR;
        Assembler a = Assembler.general;
        // the open method is not supposed to throw exceptions when the directory is
        // a iri resource
        TextIndexLucene index = (TextIndexLucene) assembler.open(a, root, /*mode*/ null);
        assertTrue(index.getDirectory() instanceof RAMDirectory);
    }

    static {
        TextAssembler.init();
    }

}
