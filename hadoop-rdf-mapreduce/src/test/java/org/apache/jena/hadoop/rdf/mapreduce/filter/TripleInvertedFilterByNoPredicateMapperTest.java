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

package org.apache.jena.hadoop.rdf.mapreduce.filter;

import org.apache.jena.hadoop.rdf.mapreduce.filter.positional.TripleFilterByPredicateUriMapper;

/**
 * Tests for the {@link TripleFilterByPredicateUriMapper} where there are no
 * predicates and thus all data must be invalid
 * 
 * 
 * 
 */
public class TripleInvertedFilterByNoPredicateMapperTest extends TripleInvertedFilterByPredicateMapperTest {

    private static final String[] EMPTY_PREDICATE_POOL = new String[0];

    /**
     * Gets the pool of predicates considered valid
     * 
     * @return Predicate pool
     */
    @Override
    protected String[] getPredicatePool() {
        return EMPTY_PREDICATE_POOL;
    }

    @Override
    protected boolean noValidInputs() {
        return true;
    }
    
    @Override
    protected boolean isInverted() {
        return true;
    }

}
