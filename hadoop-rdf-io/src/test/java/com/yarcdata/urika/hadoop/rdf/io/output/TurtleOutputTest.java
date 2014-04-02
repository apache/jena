/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.util.Arrays;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.riot.Lang;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.yarcdata.urika.hadoop.rdf.io.RdfIOConstants;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for Turtle output
 * 
 * @author rvesse
 * 
 */
@RunWith(Parameterized.class)
public class TurtleOutputTest extends AbstractTripleOutputFormatTests {

    static long $bs1 = RdfIOConstants.DEFAULT_OUTPUT_BATCH_SIZE;
    static long $bs2 = 1000;
    static long $bs3 = 100;
    static long $bs4 = 1;

    /**
     * @return Test parameters
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { $bs1 }, { $bs2 }, { $bs3 }, { $bs4 } });
    }

    private final long batchSize;

    /**
     * Creates new tests
     * 
     * @param batchSize
     *            Batch size
     */
    public TurtleOutputTest(long batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    protected String getFileExtension() {
        return ".ttl";
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.TURTLE;
    }
    
    @Override
    protected Configuration prepareConfiguration() {
        Configuration config = super.prepareConfiguration();
        config.setLong(RdfIOConstants.OUTPUT_BATCH_SIZE, this.batchSize);
        return config;
    }

    @Override
    protected OutputFormat<NullWritable, TripleWritable> getOutputFormat() {
        return new TurtleOutputFormat<NullWritable>();
    }

}
