/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io;

/**
 * Hadoop IO related constants
 * 
 * @author rvesse
 * 
 */
public class HadoopIOConstants {

    /**
     * Private constructor prevents instantiation
     */
    private HadoopIOConstants() {
    }

    /**
     * Map Reduce configuration setting for max line length
     */
    public static final String MAX_LINE_LENGTH = "mapreduce.input.linerecordreader.line.maxlength";

    /**
     * Run ID
     */
    public static final String RUN_ID = "runId";
    
    /**
     * Compression codecs to use
     */
    public static final String IO_COMPRESSION_CODECS = "io.compression.codecs";
}
