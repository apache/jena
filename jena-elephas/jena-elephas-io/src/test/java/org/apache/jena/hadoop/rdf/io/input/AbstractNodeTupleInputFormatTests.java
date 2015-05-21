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

package org.apache.jena.hadoop.rdf.io.input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.task.JobContextImpl;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.jena.hadoop.rdf.io.HadoopIOConstants;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract node tuple input format tests
 * 
 * 
 * 
 * @param <TValue>
 * @param <T>
 */
public abstract class AbstractNodeTupleInputFormatTests<TValue, T extends AbstractNodeTupleWritable<TValue>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeTupleInputFormatTests.class);

    protected static final int EMPTY_SIZE = 0, SMALL_SIZE = 100, LARGE_SIZE = 10000, BAD_SIZE = 100, MIXED_SIZE = 100;
    protected static final String EMPTY = "empty";
    protected static final String SMALL = "small";
    protected static final String LARGE = "large";
    protected static final String BAD = "bad";
    protected static final String MIXED = "mixed";

    /**
     * Temporary folder for the tests
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    protected File empty, small, large, bad, mixed;

    /**
     * Prepares the inputs for the tests
     * 
     * @throws IOException
     */
    @Before
    public void beforeTest() throws IOException {
        this.prepareInputs();
    }

    /**
     * Cleans up the inputs after each test
     */
    @After
    public void afterTest() {
        // Should be unnecessary since JUnit will clean up the temporary folder
        // anyway but best to do this regardless
        if (empty != null)
            empty.delete();
        if (small != null)
            small.delete();
        if (large != null)
            large.delete();
        if (bad != null)
            bad.delete();
        if (mixed != null)
            mixed.delete();
    }

    /**
     * Prepares a fresh configuration
     * 
     * @return Configuration
     */
    protected Configuration prepareConfiguration() {
        Configuration config = new Configuration(true);
        // Nothing else to do
        return config;
    }

    /**
     * Prepares the inputs
     * 
     * @throws IOException
     */
    protected void prepareInputs() throws IOException {
        String ext = this.getFileExtension();
        empty = folder.newFile(EMPTY + ext);
        this.generateTuples(empty, EMPTY_SIZE);
        small = folder.newFile(SMALL + ext);
        this.generateTuples(small, SMALL_SIZE);
        large = folder.newFile(LARGE + ext);
        this.generateTuples(large, LARGE_SIZE);
        bad = folder.newFile(BAD + ext);
        this.generateBadTuples(bad, BAD_SIZE);
        mixed = folder.newFile(MIXED + ext);
        this.generateMixedTuples(mixed, MIXED_SIZE);
    }

    /**
     * Gets the extra file extension to add to the filenames
     * 
     * @return File extension
     */
    protected abstract String getFileExtension();

    /**
     * Generates tuples used for tests
     * 
     * @param f
     *            File
     * @param num
     *            Number of tuples to generate
     * @throws IOException
     */
    protected final void generateTuples(File f, int num) throws IOException {
        this.generateTuples(this.getOutputStream(f), num);
    }

    /**
     * Gets the output stream to use for generating tuples
     * 
     * @param f
     *            File
     * @return Output Stream
     * @throws IOException
     */
    protected OutputStream getOutputStream(File f) throws IOException {
        return new FileOutputStream(f, false);
    }

    /**
     * Generates tuples used for tests
     * 
     * @param output
     *            Output Stream to write to
     * @param num
     *            Number of tuples to generate
     * @throws IOException
     */
    protected abstract void generateTuples(OutputStream output, int num) throws IOException;

    /**
     * Generates bad tuples used for tests
     * 
     * @param f
     *            File
     * @param num
     *            Number of bad tuples to generate
     * @throws IOException
     */
    protected final void generateBadTuples(File f, int num) throws IOException {
        this.generateBadTuples(this.getOutputStream(f), num);
    }

    /**
     * Generates bad tuples used for tests
     * 
     * @param output
     *            Output Stream to write to
     * @param num
     *            Number of bad tuples to generate
     * @throws IOException
     */
    protected abstract void generateBadTuples(OutputStream output, int num) throws IOException;

    /**
     * Generates a mixture of good and bad tuples used for tests
     * 
     * @param f
     *            File
     * @param num
     *            Number of tuples to generate, they should be a 50/50 mix of
     *            good and bad tuples
     * @throws IOException
     */
    protected final void generateMixedTuples(File f, int num) throws IOException {
        this.generateMixedTuples(this.getOutputStream(f), num);
    }

    /**
     * Generates a mixture of good and bad tuples used for tests
     * 
     * @param output
     *            Output Stream to write to
     * @param num
     *            Number of tuples to generate, they should be a 50/50 mix of
     *            good and bad tuples
     * @throws IOException
     */
    protected abstract void generateMixedTuples(OutputStream output, int num) throws IOException;

    /**
     * Adds an input path to the job configuration
     * 
     * @param f
     *            File
     * @param config
     *            Configuration
     * @param job
     *            Job
     * @throws IOException
     */
    protected void addInputPath(File f, Configuration config, Job job) throws IOException {
        FileSystem fs = FileSystem.getLocal(config);
        Path inputPath = fs.makeQualified(new Path(f.getAbsolutePath()));
        FileInputFormat.addInputPath(job, inputPath);
    }

    protected final int countTuples(RecordReader<LongWritable, T> reader) throws IOException, InterruptedException {
        int count = 0;

        // Check initial progress
        LOG.info(String.format("Initial Reported Progress %f", reader.getProgress()));
        float progress = reader.getProgress();
        if (Float.compare(0.0f, progress) == 0) {
            Assert.assertEquals(0.0d, reader.getProgress(), 0.0d);
        } else if (Float.compare(1.0f, progress) == 0) {
            // If reader is reported 1.0 straight away then we expect there to
            // be no key values
            Assert.assertEquals(1.0d, reader.getProgress(), 0.0d);
            Assert.assertFalse(reader.nextKeyValue());
        } else {
            Assert.fail(String.format(
                    "Expected progress of 0.0 or 1.0 before reader has been accessed for first time but got %f",
                    progress));
        }

        // Count tuples
        boolean debug = LOG.isDebugEnabled();
        while (reader.nextKeyValue()) {
            count++;
            progress = reader.getProgress();
            if (debug)
                LOG.debug(String.format("Current Reported Progress %f", progress));
            Assert.assertTrue(String.format("Progress should be in the range 0.0 < p <= 1.0 but got %f", progress),
                    progress > 0.0f && progress <= 1.0f);
        }
        reader.close();
        LOG.info(String.format("Got %d tuples from this record reader", count));

        // Check final progress
        LOG.info(String.format("Final Reported Progress %f", reader.getProgress()));
        Assert.assertEquals(1.0d, reader.getProgress(), 0.0d);

        return count;
    }

    protected final void checkTuples(RecordReader<LongWritable, T> reader, int expected) throws IOException,
            InterruptedException {
        Assert.assertEquals(expected, this.countTuples(reader));
    }

    /**
     * Runs a test with a single input
     * 
     * @param input
     *            Input
     * @param expectedTuples
     *            Expected tuples
     * @throws IOException
     * @throws InterruptedException
     */
    protected final void testSingleInput(File input, int expectedSplits, int expectedTuples) throws IOException,
            InterruptedException {
        // Prepare configuration
        Configuration config = this.prepareConfiguration();
        this.testSingleInput(config, input, expectedSplits, expectedTuples);
    }

    /**
     * Runs a test with a single input
     * 
     * @param config
     *            Configuration
     * @param input
     *            Input
     * @param expectedTuples
     *            Expected tuples
     * @throws IOException
     * @throws InterruptedException
     */
    protected final void testSingleInput(Configuration config, File input, int expectedSplits, int expectedTuples)
            throws IOException, InterruptedException {
        // Set up fake job
        InputFormat<LongWritable, T> inputFormat = this.getInputFormat();
        Job job = Job.getInstance(config);
        job.setInputFormatClass(inputFormat.getClass());
        this.addInputPath(input, job.getConfiguration(), job);
        JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());
        Assert.assertEquals(1, FileInputFormat.getInputPaths(context).length);
        NLineInputFormat.setNumLinesPerSplit(job, LARGE_SIZE);

        // Check splits
        List<InputSplit> splits = inputFormat.getSplits(context);
        Assert.assertEquals(expectedSplits, splits.size());

        // Check tuples
        for (InputSplit split : splits) {
            TaskAttemptContext taskContext = new TaskAttemptContextImpl(job.getConfiguration(), new TaskAttemptID());
            RecordReader<LongWritable, T> reader = inputFormat.createRecordReader(split, taskContext);
            reader.initialize(split, taskContext);
            this.checkTuples(reader, expectedTuples);
        }
    }

    protected abstract InputFormat<LongWritable, T> getInputFormat();

    /**
     * Basic tuples input test
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public final void single_input_01() throws IOException, InterruptedException {
        testSingleInput(empty, this.canSplitInputs() ? 0 : 1, EMPTY_SIZE);
    }

    /**
     * Basic tuples input test
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Test
    public final void single_input_02() throws IOException, InterruptedException {
        testSingleInput(small, 1, SMALL_SIZE);
    }

    /**
     * Basic tuples input test
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Test
    public final void single_input_03() throws IOException, InterruptedException {
        testSingleInput(large, 1, LARGE_SIZE);
    }

    /**
     * Basic tuples input test
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Test
    public final void single_input_04() throws IOException, InterruptedException {
        testSingleInput(bad, 1, 0);
    }

    /**
     * Basic tuples input test
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Test
    public final void single_input_05() throws IOException, InterruptedException {
        testSingleInput(mixed, 1, MIXED_SIZE / 2);
    }

    /**
     * Tests behaviour when ignoring bad tuples is disabled
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Test(expected = IOException.class)
    public final void fail_on_bad_input_01() throws IOException, InterruptedException {
        Configuration config = this.prepareConfiguration();
        config.setBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, false);
        Assert.assertFalse(config.getBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, true));
        testSingleInput(config, bad, 1, 0);
    }

    /**
     * Tests behaviour when ignoring bad tuples is disabled
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Test(expected = IOException.class)
    public final void fail_on_bad_input_02() throws IOException, InterruptedException {
        Configuration config = this.prepareConfiguration();
        config.setBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, false);
        Assert.assertFalse(config.getBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, true));
        testSingleInput(config, mixed, 1, MIXED_SIZE / 2);
    }

    /**
     * Runs a multiple input test
     * 
     * @param inputs
     *            Inputs
     * @param expectedSplits
     *            Number of splits expected
     * @param expectedTuples
     *            Number of tuples expected
     * @throws IOException
     * @throws InterruptedException
     */
    protected final void testMultipleInputs(File[] inputs, int expectedSplits, int expectedTuples) throws IOException,
            InterruptedException {
        // Prepare configuration and inputs
        Configuration config = this.prepareConfiguration();

        // Set up fake job
        InputFormat<LongWritable, T> inputFormat = this.getInputFormat();
        Job job = Job.getInstance(config);
        job.setInputFormatClass(inputFormat.getClass());
        for (File input : inputs) {
            this.addInputPath(input, job.getConfiguration(), job);
        }
        JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());
        Assert.assertEquals(inputs.length, FileInputFormat.getInputPaths(context).length);
        NLineInputFormat.setNumLinesPerSplit(job, expectedTuples);

        // Check splits
        List<InputSplit> splits = inputFormat.getSplits(context);
        Assert.assertEquals(expectedSplits, splits.size());

        // Check tuples
        int count = 0;
        for (InputSplit split : splits) {
            TaskAttemptContext taskContext = new TaskAttemptContextImpl(job.getConfiguration(), new TaskAttemptID());
            RecordReader<LongWritable, T> reader = inputFormat.createRecordReader(split, taskContext);
            reader.initialize(split, taskContext);
            count += this.countTuples(reader);
        }
        Assert.assertEquals(expectedTuples, count);
    }

    /**
     * tuples test with multiple inputs
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public final void multiple_inputs_01() throws IOException, InterruptedException {
        testMultipleInputs(new File[] { empty, small, large }, this.canSplitInputs() ? 2 : 3, EMPTY_SIZE + SMALL_SIZE
                + LARGE_SIZE);
    }

    /**
     * tuples test with multiple inputs
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Test
    public final void multiple_inputs_02() throws IOException, InterruptedException {
        testMultipleInputs(new File[] { folder.getRoot() }, this.canSplitInputs() ? 4 : 5, EMPTY_SIZE + SMALL_SIZE
                + LARGE_SIZE + (MIXED_SIZE / 2));
    }

    protected final void testSplitInputs(Configuration config, File[] inputs, int expectedSplits, int expectedTuples)
            throws IOException, InterruptedException {
        // Set up fake job
        InputFormat<LongWritable, T> inputFormat = this.getInputFormat();
        Job job = Job.getInstance(config);
        job.setInputFormatClass(inputFormat.getClass());
        for (File input : inputs) {
            this.addInputPath(input, job.getConfiguration(), job);
        }
        JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());
        Assert.assertEquals(inputs.length, FileInputFormat.getInputPaths(context).length);

        // Check splits
        List<InputSplit> splits = inputFormat.getSplits(context);
        Assert.assertEquals(expectedSplits, splits.size());

        // Check tuples
        int count = 0;
        for (InputSplit split : splits) {
            // Validate split
            Assert.assertTrue(this.isValidSplit(split, config));

            // Read split
            TaskAttemptContext taskContext = new TaskAttemptContextImpl(job.getConfiguration(), new TaskAttemptID());
            RecordReader<LongWritable, T> reader = inputFormat.createRecordReader(split, taskContext);
            reader.initialize(split, taskContext);
            count += this.countTuples(reader);
        }
        Assert.assertEquals(expectedTuples, count);
    }

    /**
     * Determines whether an input split is valid
     * 
     * @param split
     *            Input split
     * @return True if a valid split, false otherwise
     */
    protected boolean isValidSplit(InputSplit split, Configuration config) {
        return split instanceof FileSplit;
    }

    /**
     * Indicates whether inputs can be split, defaults to true
     * 
     * @return Whether inputs can be split
     */
    protected boolean canSplitInputs() {
        return true;
    }

    /**
     * Tests for input splitting
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    @Test
    public final void split_input_01() throws IOException, InterruptedException {
        Assume.assumeTrue(this.canSplitInputs());

        Configuration config = this.prepareConfiguration();
        config.setBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, false);
        Assert.assertEquals(Integer.MAX_VALUE, config.getInt(HadoopIOConstants.MAX_LINE_LENGTH, Integer.MAX_VALUE));
        this.testSplitInputs(config, new File[] { small }, 100, SMALL_SIZE);
    }

    /**
     * Tests for input splitting
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    @Test
    public final void split_input_02() throws IOException, InterruptedException {
        Assume.assumeTrue(this.canSplitInputs());

        Configuration config = this.prepareConfiguration();
        config.setBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, false);
        config.setLong(NLineInputFormat.LINES_PER_MAP, 10);
        Assert.assertEquals(Integer.MAX_VALUE, config.getInt(HadoopIOConstants.MAX_LINE_LENGTH, Integer.MAX_VALUE));
        this.testSplitInputs(config, new File[] { small }, 10, SMALL_SIZE);
    }

    /**
     * Tests for input splitting
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    @Test
    public final void split_input_03() throws IOException, InterruptedException {
        Assume.assumeTrue(this.canSplitInputs());

        Configuration config = this.prepareConfiguration();
        config.setBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, false);
        config.setLong(NLineInputFormat.LINES_PER_MAP, 100);
        Assert.assertEquals(Integer.MAX_VALUE, config.getInt(HadoopIOConstants.MAX_LINE_LENGTH, Integer.MAX_VALUE));
        this.testSplitInputs(config, new File[] { large }, 100, LARGE_SIZE);
    }
}