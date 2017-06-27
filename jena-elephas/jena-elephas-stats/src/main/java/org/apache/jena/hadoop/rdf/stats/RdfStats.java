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

package org.apache.jena.hadoop.rdf.stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.jena.hadoop.rdf.stats.jobs.JobFactory;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.io.colors.BasicColor;
import com.github.rvesse.airline.io.output.AnsiBasicColorizedOutputStream;
import com.github.rvesse.airline.io.output.ColorizedOutputStream;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.parser.errors.ParseException;

/**
 * Entry point for the Hadoop job, handles launching all the relevant Hadoop
 * jobs
 */
@Command(name = "rdf-stats", description = "A command which computes statistics on RDF data using Hadoop")
//@formatter:off
@Examples(examples = 
        {
            "hadoop jar PATH_TO_JAR org.apache.jena.hadoop.rdf.stats.RdfStats -n -o /example/node-counts /example/input.nt" 
        }, 
        descriptions = 
        {
            "Runs the JAR under Hadoop Map/Reduce calculating node counts for /example/input.nt and outputting them to /example/node-counts" 
        })
//@formatter:on
public class RdfStats implements Tool {
    //@formatter:off
    private static final String DATA_TYPE_TRIPLES = "triples", 
                                DATA_TYPE_QUADS = "quads", 
                                DATA_TYPE_MIXED = "mixed";
    //@formatter:on

    /**
     * Help option
     */
    @Inject
    public HelpOption<RdfStats> helpOption;

    /**
     * Gets/Sets whether all available statistics will be calculated
     */
    @Option(name = { "-a", "--all" }, description = "Requests that all available statistics be calculated")
    public boolean all = false;

    /**
     * Gets/Sets whether node usage counts will be calculated
     */
    @Option(name = { "-n", "--node-count" }, description = "Requests that node usage counts be calculated")
    public boolean nodeCount = false;

    /**
     * Gets/Sets whether characteristic sets will be calculated
     */
    @Option(name = { "-c",
            "--characteristic-sets" }, hidden = true, description = "Requests that characteristic sets be calculated (hidden as this has scalability issues)")
    public boolean characteristicSets = false;

    /**
     * Gets/Sets whether type counts will be calculated
     */
    @Option(name = { "-t", "--type-count" }, description = "Requests that rdf:type usage counts be calculated")
    public boolean typeCount = false;

    /**
     * Gets/Sets whether data type counts will be calculated
     */
    @Option(name = { "-d", "--data-types" }, description = "Requests that literal data type usage counts be calculated")
    public boolean dataTypeCount = false;

    /**
     * Gets/Sets whether namespace counts will be calculated
     */
    @Option(name = { "--namespaces" }, description = "Requests that namespace usage counts be calculated")
    public boolean namespaceCount = false;

    @Option(name = { "-g", "--graph-sizes" }, description = "Requests that the size of each named graph be counted")
    public boolean graphSize = false;

    /**
     * Gets/Sets the input data type used
     */
    @Option(name = {
            "--input-type" }, description = "Specifies whether the input data is a mixture of quads and triples, just quads or just triples.  Using the most specific data type will yield the most accurate statistics")
    @AllowedRawValues(allowedValues = { DATA_TYPE_MIXED, DATA_TYPE_QUADS, DATA_TYPE_TRIPLES })
    public String inputType = DATA_TYPE_MIXED;

    /**
     * Gets/Sets the output path
     */
    @Option(name = { "-o", "--output" }, title = "OutputPath", description = "Sets the output path", arity = 1)
    @Required
    public String outputPath = null;

    /**
     * Gets/Sets the input path(s)
     */
    @Arguments(description = "Sets the input path(s)", title = "InputPath")
    @Required
    public List<String> inputPaths = new ArrayList<String>();

    private Configuration config;

    /**
     * Entry point method
     * 
     * @param args
     *            Arguments
     */
    public static void main(String[] args) {
        try(ColorizedOutputStream<BasicColor> error = new AnsiBasicColorizedOutputStream(new CloseShieldOutputStream(System.err))){
            try {
                // Run and exit with result code if no errors bubble up
                // Note that the exit code may still be a error code
                int res = ToolRunner.run(new Configuration(true), new RdfStats(), args);
                System.exit(res);
            } catch (Throwable e) {
                // This will only happen if Hadoop option parsing errors
                // The run() method will handle its error itself
                error.setForegroundColor(BasicColor.RED);
                error.println(e.getMessage());
                e.printStackTrace(error);
            }
        }
        // If any errors bubble up exit with non-zero code
        System.exit(1);
    }

    private static void showUsage() throws IOException {
        CommandMetadata metadata = SingleCommand.singleCommand(RdfStats.class).getCommandMetadata();
        Help.help(metadata, System.err);
        System.exit(1);
    }

    @Override
    public void setConf(Configuration conf) {
        this.config = conf;
    }

    @Override
    public Configuration getConf() {
        return this.config;
    }

    @Override
    public int run(String[] args) {
        try(ColorizedOutputStream<BasicColor> error = new AnsiBasicColorizedOutputStream(new CloseShieldOutputStream(System.err))) {
            try {
                if (args.length == 0) {
                    showUsage();
                }
    
                // Parse custom arguments
                RdfStats cmd = SingleCommand.singleCommand(RdfStats.class).parse(args);
    
                // Copy Hadoop configuration across
                cmd.setConf(this.getConf());
    
                // Show help if requested and exit with success
                if (cmd.helpOption.showHelpIfRequested()) {
                    return 0;
                }
    
                // Run the command and exit with success
                cmd.run();
                return 0;
            } catch (ParseException e) {
                error.setForegroundColor(BasicColor.RED);
                error.println(e.getMessage());
                error.println();
            } catch (Throwable e) {
                error.setForegroundColor(BasicColor.RED);
                error.println(e.getMessage());
                e.printStackTrace(error);
                error.println();
            }
        }
        return 1;
    }

    private void run() throws Throwable {
        if (!this.outputPath.endsWith("/")) {
            this.outputPath += "/";
        }

        // If all statistics requested turn on all statistics
        if (this.all) {
            this.nodeCount = true;
            this.characteristicSets = true;
            this.typeCount = true;
            this.dataTypeCount = true;
            this.namespaceCount = true;
        }

        // How many statistics were requested?
        int statsRequested = 0;
        if (this.nodeCount)
            statsRequested++;
        if (this.characteristicSets)
            statsRequested++;
        if (this.typeCount)
            statsRequested++;
        if (this.dataTypeCount)
            statsRequested++;
        if (this.namespaceCount)
            statsRequested++;
        if (this.graphSize)
            statsRequested++;

        // Error if no statistics requested
        if (statsRequested == 0) {
            System.err.println(
                    "You did not request any statistics to be calculated, please use one/more of the relevant options to select the statistics to be computed");
            return;
        }
        int statsComputed = 1;

        // Compute statistics
        if (this.nodeCount) {
            Job job = this.selectNodeCountJob();
            statsComputed = this.computeStatistic(job, statsComputed, statsRequested);
        }
        if (this.graphSize) {
            Job job = this.selectGraphSizeJob();
            statsComputed = this.computeStatistic(job, statsComputed, statsRequested);
        }
        if (this.typeCount) {
            Job[] jobs = this.selectTypeCountJobs();
            statsComputed = this.computeStatistic(jobs, false, false, statsComputed, statsRequested);
        }
        if (this.dataTypeCount) {
            Job job = this.selectDataTypeCountJob();
            statsComputed = this.computeStatistic(job, statsComputed, statsRequested);
        }
        if (this.namespaceCount) {
            Job job = this.selectNamespaceCountJob();
            statsComputed = this.computeStatistic(job, statsComputed, statsRequested);
        }
        if (this.characteristicSets) {
            Job[] jobs = this.selectCharacteristicSetJobs();
            statsComputed = this.computeStatistic(jobs, false, false, statsComputed, statsRequested);
        }
    }

    private int computeStatistic(Job job, int statsComputed, int statsRequested) throws Throwable {
        System.out.println(String.format("Computing Statistic %d of %d requested", statsComputed, statsRequested));
        this.runJob(job);
        System.out.println(String.format("Computed Statistic %d of %d requested", statsComputed, statsRequested));
        System.out.println();
        return ++statsComputed;
    }

    private int computeStatistic(Job[] jobs, boolean continueOnFailure, boolean continueOnError, int statsComputed,
            int statsRequested) {
        System.out.println(String.format("Computing Statistic %d of %d requested", statsComputed, statsRequested));
        this.runJobSequence(jobs, continueOnFailure, continueOnError);
        System.out.println(String.format("Computed Statistic %d of %d requested", statsComputed, statsRequested));
        System.out.println();
        return ++statsComputed;
    }

    private boolean runJob(Job job) throws Throwable {
        System.out.println("Submitting Job " + job.getJobName());
        long start = System.nanoTime();
        try {
            job.submit();
            if (job.monitorAndPrintJob()) {
                System.out.println("Job " + job.getJobName() + " succeeded");
                return true;
            } else {
                System.out.println("Job " + job.getJobName() + " failed");
                return false;
            }
        } catch (Throwable e) {
            System.out.println("Unexpected failure in Job " + job.getJobName());
            throw e;
        } finally {
            long end = System.nanoTime();
            System.out.println("Job " + job.getJobName() + " finished after "
                    + String.format("%,d milliseconds", TimeUnit.NANOSECONDS.toMillis(end - start)));
            System.out.println();
        }
    }

    private void runJobSequence(Job[] jobs, boolean continueOnFailure, boolean continueOnError) {
        for (int i = 0; i < jobs.length; i++) {
            Job job = jobs[i];
            try {
                boolean success = this.runJob(job);
                if (!success && !continueOnFailure)
                    throw new IllegalStateException(
                            "Unable to complete job sequence because Job " + job.getJobName() + " failed");
            } catch (IllegalStateException e) {
                throw e;
            } catch (Throwable e) {
                if (!continueOnError)
                    throw new IllegalStateException(
                            "Unable to complete job sequence because job " + job.getJobName() + " errorred", e);
            }
        }
    }

    private Job selectNodeCountJob() throws IOException {
        String realOutputPath = outputPath + "node-counts/";
        String[] inputs = new String[this.inputPaths.size()];
        this.inputPaths.toArray(inputs);

        if (DATA_TYPE_QUADS.equals(this.inputType)) {
            return JobFactory.getQuadNodeCountJob(this.config, inputs, realOutputPath);
        } else if (DATA_TYPE_TRIPLES.equals(this.inputType)) {
            return JobFactory.getTripleNodeCountJob(this.config, inputs, realOutputPath);
        } else {
            return JobFactory.getNodeCountJob(this.config, inputs, realOutputPath);
        }
    }

    private Job selectGraphSizeJob() throws IOException {
        String realOutputPath = outputPath + "graph-sizes/";
        String[] inputs = new String[this.inputPaths.size()];
        this.inputPaths.toArray(inputs);

        if (DATA_TYPE_QUADS.equals(this.inputType)) {
            return JobFactory.getQuadGraphSizesJob(this.config, inputs, realOutputPath);
        } else if (DATA_TYPE_TRIPLES.equals(this.inputType)) {
            return JobFactory.getTripleGraphSizesJob(this.config, inputs, realOutputPath);
        } else {
            return JobFactory.getGraphSizesJob(this.config, inputs, realOutputPath);
        }
    }

    private Job selectDataTypeCountJob() throws IOException {
        String realOutputPath = outputPath + "data-type-counts/";
        String[] inputs = new String[this.inputPaths.size()];
        this.inputPaths.toArray(inputs);

        if (DATA_TYPE_QUADS.equals(this.inputType)) {
            return JobFactory.getQuadDataTypeCountJob(this.config, inputs, realOutputPath);
        } else if (DATA_TYPE_TRIPLES.equals(this.inputType)) {
            return JobFactory.getTripleDataTypeCountJob(this.config, inputs, realOutputPath);
        } else {
            return JobFactory.getDataTypeCountJob(this.config, inputs, realOutputPath);
        }
    }

    private Job selectNamespaceCountJob() throws IOException {
        String realOutputPath = outputPath + "namespace-counts/";
        String[] inputs = new String[this.inputPaths.size()];
        this.inputPaths.toArray(inputs);

        if (DATA_TYPE_QUADS.equals(this.inputType)) {
            return JobFactory.getQuadNamespaceCountJob(this.config, inputs, realOutputPath);
        } else if (DATA_TYPE_TRIPLES.equals(this.inputType)) {
            return JobFactory.getTripleNamespaceCountJob(this.config, inputs, realOutputPath);
        } else {
            return JobFactory.getNamespaceCountJob(this.config, inputs, realOutputPath);
        }
    }

    private Job[] selectCharacteristicSetJobs() throws IOException {
        String intermediateOutputPath = outputPath + "characteristics/intermediate/";
        String finalOutputPath = outputPath + "characteristics/final/";
        String[] inputs = new String[this.inputPaths.size()];
        this.inputPaths.toArray(inputs);

        if (DATA_TYPE_QUADS.equals(this.inputType)) {
            return JobFactory.getQuadCharacteristicSetJobs(this.config, inputs, intermediateOutputPath,
                    finalOutputPath);
        } else if (DATA_TYPE_TRIPLES.equals(this.inputType)) {
            return JobFactory.getTripleCharacteristicSetJobs(this.config, inputs, intermediateOutputPath,
                    finalOutputPath);
        } else {
            return JobFactory.getCharacteristicSetJobs(this.config, inputs, intermediateOutputPath, finalOutputPath);
        }
    }

    private Job[] selectTypeCountJobs() throws IOException {
        String intermediateOutputPath = outputPath + "type-declarations/";
        String finalOutputPath = outputPath + "type-counts/";
        String[] inputs = new String[this.inputPaths.size()];
        this.inputPaths.toArray(inputs);

        if (DATA_TYPE_QUADS.equals(this.inputType)) {
            return JobFactory.getQuadTypeCountJobs(this.config, inputs, intermediateOutputPath, finalOutputPath);
        } else if (DATA_TYPE_TRIPLES.equals(this.inputType)) {
            return JobFactory.getTripleTypeCountJobs(this.config, inputs, intermediateOutputPath, finalOutputPath);
        } else {
            return JobFactory.getTypeCountJobs(this.config, inputs, intermediateOutputPath, finalOutputPath);
        }
    }
}
