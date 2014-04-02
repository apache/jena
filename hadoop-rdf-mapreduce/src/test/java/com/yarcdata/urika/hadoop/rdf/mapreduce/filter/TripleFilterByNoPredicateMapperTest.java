/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.yarcdata.urika.hadoop.rdf.mapreduce.filter.positional.TripleFilterByPredicateUriMapper;

/**
 * Tests for the {@link TripleFilterByPredicateUriMapper} where there are no
 * predicates and thus all data must be invalid
 * 
 * @author rvesse
 * 
 */
public class TripleFilterByNoPredicateMapperTest extends TripleFilterByPredicateMapperTest {

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

}
