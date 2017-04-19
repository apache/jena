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
package org.apache.jena.hadoop.rdf.io.input.bnodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.task.JobContextImpl;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test case that embodies the scenario described in JENA-820
 */
public abstract class AbstractBlankNodeTests<T, TValue extends AbstractNodeTupleWritable<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBlankNodeTests.class);

    @BeforeClass
    public static void setup() {
        // Enable if you need to diagnose test failures
        // Useful since it includes printing the file names of the temporary
        // files being used
        // BasicConfigurator.resetConfiguration();
        // BasicConfigurator.configure();
    }

    /**
     * Gets the extension for the initial input files
     */
    protected abstract String getInitialInputExtension();

    /**
     * Creates a tuple
     * 
     * @param s
     *            Subject
     * @param p
     *            Predicate
     * @param o
     *            Object
     * @return Tuple
     */
    protected abstract T createTuple(Node s, Node p, Node o);

    /**
     * Writes out the given tuples to the given file
     * 
     * @param f
     *            File
     * @param tuples
     *            Tuples
     * @throws FileNotFoundException
     */
    protected abstract void writeTuples(File f, List<T> tuples) throws FileNotFoundException;

    /**
     * Creates the input format for reading the initial inputs
     * 
     * @return Input format
     */
    protected abstract InputFormat<LongWritable, TValue> createInitialInputFormat();

    /**
     * Creates the output format for writing the intermediate output
     * 
     * @return Output format
     */
    protected abstract OutputFormat<LongWritable, TValue> createIntermediateOutputFormat();

    /**
     * Creates the input format for reading the intermediate outputs back in
     * 
     * @return Input format
     */
    protected abstract InputFormat<LongWritable, TValue> createIntermediateInputFormat();

    /**
     * Gets the subject of the tuple
     * 
     * @param value
     *            Tuple
     * @return Subject
     */
    protected abstract Node getSubject(T value);

    /**
     * Gets whether the format being tested respects the RIOT
     * {@link ParserProfile}
     * 
     * @return True if parser profile is respected, false otherwise
     */
    protected boolean respectsParserProfile() {
        return true;
    }

    /**
     * Gets whether the format being tested preserves blank node identity
     * 
     * @return True if identity is presereved, false otherwise
     */
    protected boolean preservesBlankNodeIdentity() {
        return false;
    }

    /**
     * Test that starts with two blank nodes with the same identity in a single
     * file, splits them over two files and checks that we can workaround
     * JENA-820 successfully by setting the
     * {@link RdfIOConstants#GLOBAL_BNODE_IDENTITY} flag for our subsequent job
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public final void blank_node_divergence_01() throws IOException, InterruptedException {
        Assume.assumeTrue("Requires ParserProfile be respected", this.respectsParserProfile());
        Assume.assumeFalse("Requires that Blank Node identity not be preserved", this.preservesBlankNodeIdentity());

        // Temporary files
        File a = File.createTempFile("bnode_divergence", getInitialInputExtension());
        File intermediateOutputDir = Files.createTempDirectory("bnode_divergence", new FileAttribute[0]).toFile();

        try {
            // Prepare the input data
            // Two mentions of the same blank node in the same file
            List<T> tuples = new ArrayList<>();
            Node bnode = NodeFactory.createBlankNode();
            Node pred = NodeFactory.createURI("http://example.org/predicate");
            tuples.add(createTuple(bnode, pred, NodeFactory.createLiteral("first")));
            tuples.add(createTuple(bnode, pred, NodeFactory.createLiteral("second")));
            writeTuples(a, tuples);

            // Set up fake job which will process the file as a single split
            Configuration config = new Configuration(true);
            InputFormat<LongWritable, TValue> inputFormat = createInitialInputFormat();
            Job job = Job.getInstance(config);
            job.setInputFormatClass(inputFormat.getClass());
            NLineInputFormat.setNumLinesPerSplit(job, 100);
            FileInputFormat.setInputPaths(job, new Path(a.getAbsolutePath()));
            FileOutputFormat.setOutputPath(job, new Path(intermediateOutputDir.getAbsolutePath()));
            JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            List<InputSplit> splits = inputFormat.getSplits(context);
            Assert.assertEquals(1, splits.size());

            for (InputSplit split : splits) {
                // Initialize the input reading
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        createAttemptID(1, 1, 1));
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                // Copy the input to the output - each triple goes to a separate
                // output file
                // This is how we force multiple files to be produced
                int taskID = 1;
                while (reader.nextKeyValue()) {
                    // Prepare the output writing
                    OutputFormat<LongWritable, TValue> outputFormat = createIntermediateOutputFormat();
                    TaskAttemptContext outputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                            createAttemptID(1, ++taskID, 1));
                    RecordWriter<LongWritable, TValue> writer = outputFormat.getRecordWriter(outputTaskContext);

                    writer.write(reader.getCurrentKey(), reader.getCurrentValue());
                    writer.close(outputTaskContext);
                }
            }

            // Promote outputs from temporary status
            promoteInputs(intermediateOutputDir);

            // Now we need to create a subsequent job that reads the
            // intermediate outputs
            // As described in JENA-820 at this point the blank nodes are
            // consistent, however when we read them from different files they
            // by default get treated as different nodes and so the blank nodes
            // diverge which is incorrect and undesirable behaviour in
            // multi-stage pipelines
            LOGGER.debug("Intermediate output directory is {}", intermediateOutputDir.getAbsolutePath());
            job = Job.getInstance(config);
            inputFormat = createIntermediateInputFormat();
            job.setInputFormatClass(inputFormat.getClass());
            FileInputFormat.setInputPaths(job, new Path(intermediateOutputDir.getAbsolutePath()));

            // Enabling this flag works around the JENA-820 issue
            job.getConfiguration().setBoolean(RdfIOConstants.GLOBAL_BNODE_IDENTITY, true);
            context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            splits = inputFormat.getSplits(context);
            Assert.assertEquals(2, splits.size());

            // Expect to end up with a single blank node
            Set<Node> nodes = new HashSet<Node>();
            for (InputSplit split : splits) {
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        new TaskAttemptID());
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                while (reader.nextKeyValue()) {
                    nodes.add(getSubject(reader.getCurrentValue().get()));
                }
            }
            // Nodes should not have diverged
            Assert.assertEquals(1, nodes.size());

        } finally {
            a.delete();
            deleteDirectory(intermediateOutputDir);
        }
    }

    /**
     * Test that starts with two blank nodes with the same identity in a single
     * file, splits them over two files and shows that they diverge in the
     * subsequent job when the JENA-820 workaround is not enabled
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void blank_node_divergence_02() throws IOException, InterruptedException {
        Assume.assumeTrue("Requires ParserProfile be respected", this.respectsParserProfile());
        Assume.assumeFalse("Requires that Blank Node identity not be preserved", this.preservesBlankNodeIdentity());

        // Temporary files
        File a = File.createTempFile("bnode_divergence", getInitialInputExtension());
        File intermediateOutputDir = Files.createTempDirectory("bnode_divergence", new FileAttribute[0]).toFile();

        try {
            // Prepare the input data
            // Two mentions of the same blank node in the same file
            List<T> tuples = new ArrayList<>();
            Node bnode = NodeFactory.createBlankNode();
            Node pred = NodeFactory.createURI("http://example.org/predicate");
            tuples.add(createTuple(bnode, pred, NodeFactory.createLiteral("first")));
            tuples.add(createTuple(bnode, pred, NodeFactory.createLiteral("second")));
            writeTuples(a, tuples);

            // Set up fake job which will process the file as a single split
            Configuration config = new Configuration(true);
            InputFormat<LongWritable, TValue> inputFormat = createInitialInputFormat();
            Job job = Job.getInstance(config);
            job.setInputFormatClass(inputFormat.getClass());
            NLineInputFormat.setNumLinesPerSplit(job, 100);
            FileInputFormat.setInputPaths(job, new Path(a.getAbsolutePath()));
            FileOutputFormat.setOutputPath(job, new Path(intermediateOutputDir.getAbsolutePath()));
            JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            List<InputSplit> splits = inputFormat.getSplits(context);
            Assert.assertEquals(1, splits.size());

            for (InputSplit split : splits) {
                // Initialize the input reading
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        createAttemptID(1, 1, 1));
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                // Copy the input to the output - each triple goes to a separate
                // output file
                // This is how we force multiple files to be produced
                int taskID = 1;
                while (reader.nextKeyValue()) {
                    // Prepare the output writing
                    OutputFormat<LongWritable, TValue> outputFormat = createIntermediateOutputFormat();
                    TaskAttemptContext outputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                            createAttemptID(1, ++taskID, 1));
                    RecordWriter<LongWritable, TValue> writer = outputFormat.getRecordWriter(outputTaskContext);

                    writer.write(reader.getCurrentKey(), reader.getCurrentValue());
                    writer.close(outputTaskContext);
                }
            }

            // Promote outputs from temporary status
            promoteInputs(intermediateOutputDir);

            // Now we need to create a subsequent job that reads the
            // intermediate outputs
            // As described in JENA-820 at this point the blank nodes are
            // consistent, however when we read them from different files they
            // by default get treated as different nodes and so the blank nodes
            // diverge which is incorrect and undesirable behaviour in
            // multi-stage pipelines. However it is the default behaviour
            // because when we start from external inputs we want them to be
            // file scoped.
            LOGGER.debug("Intermediate output directory is {}", intermediateOutputDir.getAbsolutePath());
            job = Job.getInstance(config);
            inputFormat = createIntermediateInputFormat();
            job.setInputFormatClass(inputFormat.getClass());
            FileInputFormat.setInputPaths(job, new Path(intermediateOutputDir.getAbsolutePath()));

            // Make sure JENA-820 flag is disabled
            job.getConfiguration().setBoolean(RdfIOConstants.GLOBAL_BNODE_IDENTITY, false);
            context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            splits = inputFormat.getSplits(context);
            Assert.assertEquals(2, splits.size());

            // Expect to end up with a single blank node
            Set<Node> nodes = new HashSet<Node>();
            for (InputSplit split : splits) {
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        new TaskAttemptID());
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                while (reader.nextKeyValue()) {
                    nodes.add(getSubject(reader.getCurrentValue().get()));
                }
            }
            // Nodes should have diverged
            Assert.assertEquals(2, nodes.size());

        } finally {
            a.delete();
            deleteDirectory(intermediateOutputDir);
        }
    }

    /**
     * Test that starts with two blank nodes in two different files and checks
     * that writing them to a single file does not conflate them
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void blank_node_identity_01() throws IOException, InterruptedException {
        Assume.assumeTrue("Requires ParserProfile be respected", this.respectsParserProfile());
        Assume.assumeFalse("Requires that Blank Node identity not be preserved", this.preservesBlankNodeIdentity());

        // Temporary files
        File a = File.createTempFile("bnode_identity", getInitialInputExtension());
        File b = File.createTempFile("bnode_identity", getInitialInputExtension());
        File intermediateOutputDir = Files.createTempDirectory("bnode_identity", new FileAttribute[0]).toFile();

        try {
            // Prepare the input data
            // Different blank nodes in different files
            List<T> tuples = new ArrayList<>();
            Node bnode1 = NodeFactory.createBlankNode();
            Node bnode2 = NodeFactory.createBlankNode();
            Node pred = NodeFactory.createURI("http://example.org/predicate");

            tuples.add(createTuple(bnode1, pred, NodeFactory.createLiteral("first")));
            writeTuples(a, tuples);

            tuples.clear();
            tuples.add(createTuple(bnode2, pred, NodeFactory.createLiteral("second")));
            writeTuples(b, tuples);

            // Set up fake job which will process the two files
            Configuration config = new Configuration(true);
            InputFormat<LongWritable, TValue> inputFormat = createInitialInputFormat();
            Job job = Job.getInstance(config);
            job.setInputFormatClass(inputFormat.getClass());
            NLineInputFormat.setNumLinesPerSplit(job, 100);
            FileInputFormat.setInputPaths(job, new Path(a.getAbsolutePath()), new Path(b.getAbsolutePath()));
            FileOutputFormat.setOutputPath(job, new Path(intermediateOutputDir.getAbsolutePath()));
            JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            List<InputSplit> splits = inputFormat.getSplits(context);
            Assert.assertEquals(2, splits.size());

            // Prepare the output writing - putting all output to a single file
            OutputFormat<LongWritable, TValue> outputFormat = createIntermediateOutputFormat();
            TaskAttemptContext outputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(), createAttemptID(
                    1, 2, 1));
            RecordWriter<LongWritable, TValue> writer = outputFormat.getRecordWriter(outputTaskContext);

            for (InputSplit split : splits) {
                // Initialize the input reading
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        createAttemptID(1, 1, 1));
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                // Copy the input to the output - all triples go to a single
                // output
                while (reader.nextKeyValue()) {
                    writer.write(reader.getCurrentKey(), reader.getCurrentValue());
                }
            }
            writer.close(outputTaskContext);

            // Promote outputs from temporary status
            promoteInputs(intermediateOutputDir);

            // Now we need to create a subsequent job that reads the
            // intermediate outputs
            // The Blank nodes should have been given separate identities so we
            // should not be conflating them, this is the opposite problem to
            // that described in JENA-820
            LOGGER.debug("Intermediate output directory is {}", intermediateOutputDir.getAbsolutePath());
            job = Job.getInstance(config);
            inputFormat = createIntermediateInputFormat();
            job.setInputFormatClass(inputFormat.getClass());
            NLineInputFormat.setNumLinesPerSplit(job, 100);
            FileInputFormat.setInputPaths(job, new Path(intermediateOutputDir.getAbsolutePath()));
            context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            splits = inputFormat.getSplits(context);
            Assert.assertEquals(1, splits.size());

            // Expect to end up with a single blank node
            Set<Node> nodes = new HashSet<Node>();
            for (InputSplit split : splits) {
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        new TaskAttemptID());
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                while (reader.nextKeyValue()) {
                    nodes.add(getSubject(reader.getCurrentValue().get()));
                }
            }
            // Nodes must not have converged
            Assert.assertEquals(2, nodes.size());

        } finally {
            a.delete();
            b.delete();
            deleteDirectory(intermediateOutputDir);
        }
    }

    /**
     * Test that starts with two blank nodes in two different files and checks
     * that writing them to a single file does not conflate them
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void blank_node_identity_02() throws IOException, InterruptedException {
        Assume.assumeTrue("Requires ParserProfile be respected", this.respectsParserProfile());
        Assume.assumeFalse("Requires that Blank Node identity not be preserved", this.preservesBlankNodeIdentity());

        // Temporary files
        File a = File.createTempFile("bnode_identity", getInitialInputExtension());
        File b = File.createTempFile("bnode_identity", getInitialInputExtension());
        File intermediateOutputDir = Files.createTempDirectory("bnode_identity", new FileAttribute[0]).toFile();

        try {
            // Prepare the input data
            // Same blank node but in different files so must be treated as
            // different blank nodes and not converge
            List<T> tuples = new ArrayList<>();
            Node bnode = NodeFactory.createBlankNode();
            Node pred = NodeFactory.createURI("http://example.org/predicate");

            tuples.add(createTuple(bnode, pred, NodeFactory.createLiteral("first")));
            writeTuples(a, tuples);

            tuples.clear();
            tuples.add(createTuple(bnode, pred, NodeFactory.createLiteral("second")));
            writeTuples(b, tuples);

            // Set up fake job which will process the two files
            Configuration config = new Configuration(true);
            InputFormat<LongWritable, TValue> inputFormat = createInitialInputFormat();
            Job job = Job.getInstance(config);
            job.setInputFormatClass(inputFormat.getClass());
            NLineInputFormat.setNumLinesPerSplit(job, 100);
            FileInputFormat.setInputPaths(job, new Path(a.getAbsolutePath()), new Path(b.getAbsolutePath()));
            FileOutputFormat.setOutputPath(job, new Path(intermediateOutputDir.getAbsolutePath()));
            JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            List<InputSplit> splits = inputFormat.getSplits(context);
            Assert.assertEquals(2, splits.size());

            // Prepare the output writing - putting all output to a single file
            OutputFormat<LongWritable, TValue> outputFormat = createIntermediateOutputFormat();
            TaskAttemptContext outputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(), createAttemptID(
                    1, 2, 1));
            RecordWriter<LongWritable, TValue> writer = outputFormat.getRecordWriter(outputTaskContext);

            for (InputSplit split : splits) {
                // Initialize the input reading
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        createAttemptID(1, 1, 1));
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                // Copy the input to the output - all triples go to a single
                // output
                while (reader.nextKeyValue()) {
                    writer.write(reader.getCurrentKey(), reader.getCurrentValue());
                }
            }
            writer.close(outputTaskContext);

            // Promote outputs from temporary status
            promoteInputs(intermediateOutputDir);

            // Now we need to create a subsequent job that reads the
            // intermediate outputs
            // The Blank nodes should have been given separate identities so we
            // should not be conflating them, this is the opposite problem to
            // that described in JENA-820
            LOGGER.debug("Intermediate output directory is {}", intermediateOutputDir.getAbsolutePath());
            job = Job.getInstance(config);
            inputFormat = createIntermediateInputFormat();
            job.setInputFormatClass(inputFormat.getClass());
            NLineInputFormat.setNumLinesPerSplit(job, 100);
            FileInputFormat.setInputPaths(job, new Path(intermediateOutputDir.getAbsolutePath()));
            context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            splits = inputFormat.getSplits(context);
            Assert.assertEquals(1, splits.size());

            // Expect to end up with a single blank node
            Set<Node> nodes = new HashSet<Node>();
            for (InputSplit split : splits) {
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        new TaskAttemptID());
                RecordReader<LongWritable, TValue> reader = inputFormat.createRecordReader(split, inputTaskContext);
                reader.initialize(split, inputTaskContext);

                while (reader.nextKeyValue()) {
                    nodes.add(getSubject(reader.getCurrentValue().get()));
                }
            }
            // Nodes must not diverge
            Assert.assertEquals(2, nodes.size());

        } finally {
            a.delete();
            b.delete();
            deleteDirectory(intermediateOutputDir);
        }
    }

    private TaskAttemptID createAttemptID(int jobID, int taskID, int id) {
        return new TaskAttemptID("outputTest", jobID, TaskType.MAP, taskID, 1);
    }

    private void promoteInputs(File baseDir) throws IOException {
        for (File f : baseDir.listFiles()) {
            if (f.isDirectory()) {
                promoteInputs(baseDir, f);
            }
        }
    }

    private void promoteInputs(File targetDir, File dir) throws IOException {
        java.nio.file.Path target = Paths.get(targetDir.toURI());
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                promoteInputs(targetDir, f);
            } else {
                LOGGER.debug("Moving {} to {}", f.getAbsolutePath(), target.resolve(f.getName()));
                Files.move(Paths.get(f.toURI()), target.resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Remove defunct sub-directory
        dir.delete();
    }

    private void deleteDirectory(File dir) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isFile())
                f.delete();
            if (f.isDirectory())
                deleteDirectory(f);
        }
        dir.delete();
    }
}
