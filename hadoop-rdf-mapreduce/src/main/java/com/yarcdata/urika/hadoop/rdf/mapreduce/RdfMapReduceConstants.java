/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce;

/**
 * RDF Map/Reduce related constants
 * 
 * @author rvesse
 * 
 */
public class RdfMapReduceConstants {

    /**
     * Private constructor prevents instantiation
     */
    private RdfMapReduceConstants() {

    }

    /**
     * Configuration key used to set whether the behaviour of the filter mappers
     * is inverted. When enabled the filter mappers will invert their selection
     * i.e. tuples that would normally be accepted will be rejected and vice
     * versa.
     */
    public static final String FILTER_INVERT = "rdf.mapreduce.filter.invert";

    /**
     * Configuration key used to set a command separated list of predicate URIs
     * to filter upon
     */
    public static final String FILTER_PREDICATE_URIS = "rdf.mapreduce.filter.predicate.uris";

    /**
     * Configuration key used to set a command separated list of subject URIs to
     * filter upon
     */
    public static final String FILTER_SUBJECT_URIS = "rdf.mapreduce.filter.subject.uris";

    /**
     * Configuration key used to set a command separated list of object URIs to
     * filter upon
     */
    public static final String FILTER_OBJECT_URIS = "rdf.mapreduce.filter.object.uris";

    /**
     * Configuration key used to set a command separated list of graph URIs to
     * filter upon
     */
    public static final String FILTER_GRAPH_URIS = "rdf.mapreduce.filter.graph.uris";
}
