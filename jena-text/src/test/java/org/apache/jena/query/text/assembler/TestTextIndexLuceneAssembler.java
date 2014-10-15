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

import org.apache.jena.query.text.TextIndexLucene;
import org.junit.Test;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestTextIndexLuceneAssembler extends AbstractTestTextAssembler {
	
	@Test public void testIndexHasEntityMap() {
		TextIndexLucene indexLucene = (TextIndexLucene) Assembler.general.open(SIMPLE_INDEX_SPEC);
		assertEquals(RDFS.label.asNode(), indexLucene.getDocDef().getPrimaryPredicate());		
	}
	
	static {
		TextAssembler.init();
	}

}
