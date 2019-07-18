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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

public class TestPropListsAssembler {

    private static final Resource spec0;
    private static final Resource spec1;
    private static final Resource spec2;
    private static final Resource spec3;
    
    @Test 
    public void PropsListsNull() {
        int before = Util.sizePropsList();
        PropListsAssembler.open(null, spec0);
        int after = Util.sizePropsList();
        assertEquals(before, after);
    }
    
    @Test 
    public void PropsListsOneWithNone() {
        int before = Util.sizePropsList();
        PropListsAssembler.open(null, spec1);
        int after = Util.sizePropsList();
        assertEquals(before+1, after);
        
        Resource listProp = ResourceFactory.createResource(SKOS.getURI()+"list1");
        List<Resource> pList = Util.getPropList(listProp);
        assertNotNull(pList);
        assertEquals(pList.size(), 0);
    }
    
    @Test 
    public void PropsListsOneWithThree() {
        int before = Util.sizePropsList();
        PropListsAssembler.open(null, spec2);
        int after = Util.sizePropsList();
        assertEquals(before+1, after);
        
        Resource listProp = ResourceFactory.createResource(SKOS.getURI()+"labels");
        List<Resource> pList = Util.getPropList(listProp);
        assertNotNull(pList);
        assertEquals(pList.size(), 3);
    }
    
    @Test 
    public void PropsListsTwoWith3and5() {
        int before = Util.sizePropsList();
        PropListsAssembler.open(null, spec3);
        int after = Util.sizePropsList();
        assertEquals(before+2, after);
        
        Resource listProp = ResourceFactory.createResource(SKOS.getURI()+"labels2");
        List<Resource> pList = Util.getPropList(listProp);
        assertNotNull(pList);
        assertEquals(pList.size(), 3);
        
        listProp = ResourceFactory.createResource(SKOS.getURI()+"stmts");
        pList = Util.getPropList(listProp);
        assertNotNull(pList);
        assertEquals(pList.size(), 5);
    }


    static {
        JenaSystem.init();
        TextAssembler.init();
        Model model = ModelFactory.createDefaultModel();

        // empty text:propLists        
        spec0 = model.createList( );

        // text:propList with 1 list of no props        
        spec1 = model.createList(
                new RDFNode[] {
                        model.createResource()
                        .addProperty(TextVocab.pPropListProp, 
                                model.createResource(SKOS.getURI()+"list1"))
                        .addProperty(TextVocab.pProps, 
                                model.createList( new RDFNode[] { } )
                         )
                }
                );

        // text:propList with 1 list of 3 props        
        spec2 = model.createList(
                new RDFNode[] {
                        model.createResource()
                        .addProperty(TextVocab.pPropListProp, 
                                model.createResource(SKOS.getURI()+"labels"))
                        .addProperty(TextVocab.pProps, 
                                model.createList( new RDFNode[] { 
                                        SKOS.prefLabel
                                       , SKOS.altLabel
                                       , RDFS.label
                                } )
                          )
                }
                );

        // text:propList with 1 list of 3 props        
        spec3 = model.createList(
                new RDFNode[] {
                        model.createResource()
                        .addProperty(TextVocab.pPropListProp, 
                                model.createResource(SKOS.getURI()+"labels2"))
                        .addProperty(TextVocab.pProps, 
                                model.createList( new RDFNode[] { 
                                        SKOS.prefLabel
                                       , SKOS.altLabel
                                       , RDFS.label
                                } )
                          ) ,
                        model.createResource()
                        .addProperty(TextVocab.pPropListProp, 
                                model.createResource(SKOS.getURI()+"stmts"))
                        .addProperty(TextVocab.pProps, 
                                model.createList( new RDFNode[] { 
                                        SKOS.editorialNote
                                       , SKOS.closeMatch
                                       , RDFS.member
                                       , RDFS.range
                                       , RDFS.seeAlso
                                } )
                          )
                }
                );
    }
}
