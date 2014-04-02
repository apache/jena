/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io;

import java.io.IOException;

/**
 * RDF IO related constants
 * 
 * @author rvesse
 * 
 */
public class RdfIOConstants {

    /**
     * Private constructor prevents instantiation
     */
    private RdfIOConstants() {
    }

    /**
     * Configuration key used to set whether bad tuples are ignored. This is the
     * default behaviour, when explicitly set to {@code false} bad tuples will
     * result in {@link IOException} being thrown by the relevant record
     * readers.
     */
    public static final String INPUT_IGNORE_BAD_TUPLES = "rdf.io.input.ignore-bad-tuples";

    /**
     * Configuration key used to set the batch size used for RDF output formats
     * that take a batched writing approach. Default value is given by the
     * constant {@link #DEFAULT_OUTPUT_BATCH_SIZE}.
     */
    public static final String OUTPUT_BATCH_SIZE = "rdf.io.output.batch-size";

    /**
     * Default batch size for batched output formats
     */
    public static final long DEFAULT_OUTPUT_BATCH_SIZE = 10000;
}
