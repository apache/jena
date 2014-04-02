/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.riot.Lang;

import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for TriG input
 * @author rvesse
 *
 */
public class TriGInputTest extends AbstractWholeFileQuadInputFormatTests {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.TRIG;
    }

    @Override
    protected String getFileExtension() {
        return ".trig";
    }

    @Override
    protected InputFormat<LongWritable, QuadWritable> getInputFormat() {
        return new TriGInputFormat();
    }

}
