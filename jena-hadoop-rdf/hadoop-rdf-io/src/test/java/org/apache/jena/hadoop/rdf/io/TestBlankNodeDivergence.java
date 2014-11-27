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
package org.apache.jena.hadoop.rdf.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
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
import org.apache.jena.hadoop.rdf.io.input.ntriples.NTriplesInputFormat;
import org.apache.jena.hadoop.rdf.io.output.ntriples.NTriplesOutputFormat;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

/**
 * Test case that embodies the scenario described in JENA-820
 */
public class TestBlankNodeDivergence {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestBlankNodeDivergence.class);

    @BeforeClass
    public static void setup() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }

    @Test
    @Ignore
    // Ignored due to JENA-820, serves as a test case that demonstrates the
    // issue, once a workaround is available this test can be enabled and should
    // pass
    public void blank_node_divergence_01() throws IOException, InterruptedException {
        File a = File.createTempFile("bnode_divergence", ".nt");
        File intermediateOutputDir = Files.createTempDirectory("bnode_divergence", new FileAttribute[0]).toFile();
        try {
            writeTriple(a, "a", "\"first\"");
            writeTriple(a, "a", "\"second\"");

            // Set up fake job which will process the file as a single split
            Configuration config = new Configuration(true);
            NTriplesInputFormat inputFormat = new NTriplesInputFormat();
            Job job = Job.getInstance(config);
            job.setInputFormatClass(inputFormat.getClass());
            NLineInputFormat.setNumLinesPerSplit(job, 2);
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
                RecordReader<LongWritable, TripleWritable> reader = inputFormat.createRecordReader(split,
                        inputTaskContext);
                reader.initialize(split, inputTaskContext);

                // Copy the input to the output - each triple goes to a separate
                // output file
                // This is how we force multiple files to be produced
                int taskID = 1;
                while (reader.nextKeyValue()) {
                    // Prepare the output writing
                    NTriplesOutputFormat<LongWritable> outputFormat = new NTriplesOutputFormat<>();
                    TaskAttemptContext outputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                            createAttemptID(1, ++taskID, 1));
                    RecordWriter<LongWritable, TripleWritable> writer = outputFormat.getRecordWriter(outputTaskContext);

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
            // get treated as different nodes and so the blank nodes diverge
            // which is incorrect behaviour
            System.out.println(intermediateOutputDir.getAbsolutePath());
            job = Job.getInstance(config);
            job.setInputFormatClass(NTriplesInputFormat.class);
            FileInputFormat.setInputPaths(job, new Path(intermediateOutputDir.getAbsolutePath()));
            context = new JobContextImpl(job.getConfiguration(), job.getJobID());

            // Get the splits
            splits = inputFormat.getSplits(context);
            Assert.assertEquals(2, splits.size());

            // Expect to end up with a single blank node
            Set<Node> nodes = new HashSet<Node>();
            for (InputSplit split : splits) {
                TaskAttemptContext inputTaskContext = new TaskAttemptContextImpl(job.getConfiguration(),
                        new TaskAttemptID());
                RecordReader<LongWritable, TripleWritable> reader = inputFormat.createRecordReader(split,
                        inputTaskContext);
                reader.initialize(split, inputTaskContext);

                while (reader.nextKeyValue()) {
                    nodes.add(reader.getCurrentValue().get().getSubject());
                }
            }
            Assert.assertEquals(1, nodes.size());

        } finally {
            a.delete();
            // TODO Delete directory
        }
    }

    private TaskAttemptID createAttemptID(int jobID, int taskID, int id) {
        return new TaskAttemptID("outputTest", jobID, TaskType.MAP, taskID, 1);
    }

    private void writeTriple(File f, String id, String object) {
        try {
            FileWriter writer = new FileWriter(f, true);
            writer.write("_:");
            writer.write(id);
            writer.write(" <http://p> ");
            writer.write(object);
            writer.write(".\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {

        }
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
}
