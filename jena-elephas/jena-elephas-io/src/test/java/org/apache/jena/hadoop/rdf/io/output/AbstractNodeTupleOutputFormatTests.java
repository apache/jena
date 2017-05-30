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

package org.apache.jena.hadoop.rdf.io.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.task.JobContextImpl;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.StreamRDFLib;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract node tuple output format tests
 * 
 * 
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 * 
 */
public abstract class AbstractNodeTupleOutputFormatTests<TValue, T extends AbstractNodeTupleWritable<TValue>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeTupleOutputFormatTests.class);

    protected static final int EMPTY_SIZE = 0, SMALL_SIZE = 100, LARGE_SIZE = 10000, VERY_LARGE_SIZE = 100000;

    /**
     * Temporary folder for the tests
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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
     * Gets the extra file extension to add to the filenames
     * 
     * @return File extension
     */
    protected abstract String getFileExtension();

    /**
     * Generates tuples to be output for testing
     * 
     * @param num
     *            Number of tuples to generate
     * @return Iterator of tuples
     */
    protected abstract Iterator<T> generateTuples(int num);

    /**
     * Counts tuples in the output file
     * 
     * @param f
     *            Output file
     * @return Tuple count
     */
    protected final long countTuples(File f) {
        StreamRDFCounting counter = StreamRDFLib.count();
        RDFDataMgr.parse(counter, f.getAbsolutePath(), this.getRdfLanguage());
        return counter.count();
    }

    /**
     * Checks that tuples are as expected
     * 
     * @param f
     *            File
     * @param expected
     *            Expected number of tuples
     */
    protected void checkTuples(File f, long expected) {
        Assert.assertEquals(expected, this.countTuples(f));
    }

    /**
     * Gets the RDF language of the produced output which is used to parse back
     * in the output to validate the correct amount of output was produced
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();

    /**
     * Gets the output format to test
     * 
     * @return Output format
     */
    protected abstract OutputFormat<NullWritable, T> getOutputFormat();

    /**
     * Adds an output path to the job configuration
     * 
     * @param f
     *            File
     * @param config
     *            Configuration
     * @param job
     *            Job
     * @throws IOException
     */
    protected void addOutputPath(File f, Configuration config, Job job) throws IOException {
        FileSystem fs = FileSystem.getLocal(config);
        Path outputPath = fs.makeQualified(new Path(f.getAbsolutePath()));
        FileOutputFormat.setOutputPath(job, outputPath);
    }

    protected File findOutputFile(File dir, JobContext context) throws FileNotFoundException, IOException {
        Path outputPath = FileOutputFormat.getOutputPath(context);
        RemoteIterator<LocatedFileStatus> files = outputPath.getFileSystem(context.getConfiguration()).listFiles(
                outputPath, true);
        while (files.hasNext()) {
            LocatedFileStatus status = files.next();
            if (status.isFile() && !status.getPath().getName().startsWith("_")) {
                return new File(status.getPath().toUri());
            }
        }
        return null;
    }

    /**
     * Tests output
     * 
     * @param f
     *            File to output to
     * @param num
     *            Number of tuples to output
     * @throws IOException
     * @throws InterruptedException
     */
    protected final void testOutput(File f, int num) throws IOException, InterruptedException {
        // Prepare configuration
        Configuration config = this.prepareConfiguration();

        // Set up fake job
        OutputFormat<NullWritable, T> outputFormat = this.getOutputFormat();
        Job job = Job.getInstance(config);
        job.setOutputFormatClass(outputFormat.getClass());
        this.addOutputPath(f, job.getConfiguration(), job);
        JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());
        Assert.assertNotNull(FileOutputFormat.getOutputPath(context));

        // Output the data
        TaskAttemptID id = new TaskAttemptID("outputTest", 1, TaskType.MAP, 1, 1);
        TaskAttemptContext taskContext = new TaskAttemptContextImpl(job.getConfiguration(), id);
        RecordWriter<NullWritable, T> writer = outputFormat.getRecordWriter(taskContext);
        Iterator<T> tuples = this.generateTuples(num);
        while (tuples.hasNext()) {
            writer.write(NullWritable.get(), tuples.next());
        }
        writer.close(taskContext);

        // Check output
        File outputFile = this.findOutputFile(this.folder.getRoot(), context);
        Assert.assertNotNull(outputFile);
        this.checkTuples(outputFile, num);
    }

    /**
     * Basic output tests
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void output_01() throws IOException, InterruptedException {
        this.testOutput(this.folder.getRoot(), EMPTY_SIZE);
    }

    /**
     * Basic output tests
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void output_02() throws IOException, InterruptedException {
        this.testOutput(this.folder.getRoot(), SMALL_SIZE);
    }

    /**
     * Basic output tests
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void output_03() throws IOException, InterruptedException {
        this.testOutput(this.folder.getRoot(), LARGE_SIZE);
    }

    /**
     * Basic output tests
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void output_04() throws IOException, InterruptedException {
        this.testOutput(this.folder.getRoot(), VERY_LARGE_SIZE);
    }
}
