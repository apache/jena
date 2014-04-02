/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;

/**
 * Abstract line based input format that reuses the machinery from
 * {@link NLineInputFormat} to calculate the splits
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Value type
 */
public abstract class AbstractNLineFileInputFormat<TKey, TValue> extends FileInputFormat<TKey, TValue> {

    /**
     * Logically splits the set of input files for the job, splits N lines of
     * the input as one split.
     * 
     * @see FileInputFormat#getSplits(JobContext)
     */
    public final List<InputSplit> getSplits(JobContext job) throws IOException {
        List<InputSplit> splits = new ArrayList<InputSplit>();
        int numLinesPerSplit = NLineInputFormat.getNumLinesPerSplit(job);
        for (FileStatus status : listStatus(job)) {
            splits.addAll(NLineInputFormat.getSplitsForFile(status, job.getConfiguration(), numLinesPerSplit));
        }
        return splits;
    }
}
