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

package org.apache.jena.sparql.graph;

import java.util.Objects;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.AbstractTestPrefixMapping;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;

/** Test a {@link PrefixMapping} backed by a {@link PrefixMap} */
public class TestPrefixMappingPrefixMap extends AbstractTestPrefixMapping {

    public TestPrefixMappingPrefixMap(String name) {
        super(name);
    }

    @Override
    protected PrefixMapping getMapping() {
        PrefixMap pmap = PrefixMapFactory.create();
        return new PrefixMappingAdapter(pmap);
    }
    
    // PrefixMaps only keep the prefix -> URI direction mapping.
    // Because they not have the reverse map, the outcome of getNsURIPrefix is "some match"
    // making the result non-deterministic.
    @Override
    public void testSecondPrefixReplacesReverseMap() {
        String testURI = "http://example/test";
        PrefixMapping A = getMapping();
        A.setNsPrefix( "a", testURI );
        A.setNsPrefix( "b", testURI );
        String prefix = A.getNsURIPrefix( testURI );
        assertTrue(Objects.equals(prefix, "a") || Objects.equals(prefix, "b") );
    }
    
    @Override
    public void testLock() {}
}
