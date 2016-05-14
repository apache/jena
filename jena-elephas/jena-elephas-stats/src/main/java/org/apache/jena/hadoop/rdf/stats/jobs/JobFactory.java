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

package org.apache.jena.hadoop.rdf.stats.jobs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.jena.hadoop.rdf.io.input.QuadsInputFormat;
import org.apache.jena.hadoop.rdf.io.input.TriplesInputFormat;
import org.apache.jena.hadoop.rdf.io.input.TriplesOrQuadsInputFormat;
import org.apache.jena.hadoop.rdf.io.input.nquads.NQuadsInputFormat;
import org.apache.jena.hadoop.rdf.io.input.ntriples.NTriplesInputFormat;
import org.apache.jena.hadoop.rdf.io.output.nquads.NQuadsOutputFormat;
import org.apache.jena.hadoop.rdf.io.output.ntriples.NTriplesNodeOutputFormat;
import org.apache.jena.hadoop.rdf.io.output.ntriples.NTriplesOutputFormat;
import org.apache.jena.hadoop.rdf.mapreduce.KeyMapper;
import org.apache.jena.hadoop.rdf.mapreduce.RdfMapReduceConstants;
import org.apache.jena.hadoop.rdf.mapreduce.TextCountReducer;
import org.apache.jena.hadoop.rdf.mapreduce.characteristics.CharacteristicSetReducer;
import org.apache.jena.hadoop.rdf.mapreduce.characteristics.QuadCharacteristicSetGeneratingReducer;
import org.apache.jena.hadoop.rdf.mapreduce.characteristics.TripleCharacteristicSetGeneratingReducer;
import org.apache.jena.hadoop.rdf.mapreduce.count.NodeCountReducer;
import org.apache.jena.hadoop.rdf.mapreduce.count.QuadNodeCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.TripleNodeCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.datatypes.QuadDataTypeCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.datatypes.TripleDataTypeCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.namespaces.QuadNamespaceCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.namespaces.TripleNamespaceCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.positional.QuadGraphCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.positional.QuadObjectCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.count.positional.TripleObjectCountMapper;
import org.apache.jena.hadoop.rdf.mapreduce.filter.positional.QuadFilterByPredicateMapper;
import org.apache.jena.hadoop.rdf.mapreduce.filter.positional.TripleFilterByPredicateUriMapper;
import org.apache.jena.hadoop.rdf.mapreduce.group.QuadGroupBySubjectMapper;
import org.apache.jena.hadoop.rdf.mapreduce.group.TripleGroupBySubjectMapper;
import org.apache.jena.hadoop.rdf.mapreduce.transform.TriplesToQuadsConstantGraphMapper;
import org.apache.jena.hadoop.rdf.types.CharacteristicSetWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.vocabulary.RDF ;

/**
 * Factory that can produce {@link Job} instances for computing various RDF
 * statistics
 * 
 * 
 * 
 */
public class JobFactory {

    /**
     * Private constructor prevents instantiation
     */
    private JobFactory() {
    }

    /**
     * Gets a job for computing node counts on RDF triple inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getTripleNodeCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Triples Node Usage Count");

        // Map/Reduce classes
        job.setMapperClass(TripleNodeCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(TriplesInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Gets a job for computing node counts on RDF quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getQuadNodeCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Quads Node Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadNodeCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(QuadsInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Gets a job for computing node counts on RDF triple and/or quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getNodeCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Node Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadNodeCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(TriplesOrQuadsInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }
    
    public static Job getTripleGraphSizesJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Triples Graph Sizes");
        
        // Map/Reduce classes
        ChainMapper.addMapper(job, TriplesToQuadsConstantGraphMapper.class, LongWritable.class, TripleWritable.class, LongWritable.class, QuadWritable.class, config);
        ChainMapper.addMapper(job, QuadGraphCountMapper.class, LongWritable.class, QuadWritable.class, NodeWritable.class, LongWritable.class, config);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);
        
        // Input and Output
        job.setInputFormatClass(TriplesInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        
        return job;
    }
    
    public static Job getQuadGraphSizesJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Quads Graph Sizes");
        
        // Map/Reduce classes
        job.setMapperClass(QuadGraphCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);
        
        // Input and Output
        job.setInputFormatClass(QuadsInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        
        return job;
    }
    
    public static Job getGraphSizesJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Graph Sizes");
        
        // Map/Reduce classes
        job.setMapperClass(QuadGraphCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);
        
        // Input and Output
        job.setInputFormatClass(TriplesOrQuadsInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        
        return job;
    }

    /**
     * Gets a sequence of jobs that can be used to compute characteristic sets
     * for RDF triples
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param intermediateOutputPath
     *            Intermediate output path
     * @param outputPath
     *            Final output path
     * @return Sequence of jobs
     * @throws IOException
     */
    public static Job[] getTripleCharacteristicSetJobs(Configuration config, String[] inputPaths, String intermediateOutputPath,
            String outputPath) throws IOException {
        Job[] jobs = new Job[2];

        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Triples Characteristic Set (Generation)");

        // Map/Reduce classes
        job.setMapperClass(TripleGroupBySubjectMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(TripleWritable.class);
        job.setReducerClass(TripleCharacteristicSetGeneratingReducer.class);
        job.setOutputKeyClass(CharacteristicSetWritable.class);
        job.setOutputValueClass(NullWritable.class);

        // Input and Output
        job.setInputFormatClass(TriplesInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(intermediateOutputPath));
        SequenceFileOutputFormat.setCompressOutput(job, true);
        FileOutputFormat.setOutputCompressorClass(job, BZip2Codec.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);

        jobs[0] = job;

        job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Triples Characteristic Set (Reduction)");

        // Map/Reduce classes
        job.setMapperClass(KeyMapper.class);
        job.setMapOutputKeyClass(CharacteristicSetWritable.class);
        job.setMapOutputValueClass(CharacteristicSetWritable.class);
        job.setReducerClass(CharacteristicSetReducer.class);
        job.setOutputKeyClass(CharacteristicSetWritable.class);
        job.setOutputValueClass(CharacteristicSetWritable.class);

        // Input and Output
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.setInputPaths(job, intermediateOutputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        jobs[1] = job;
        return jobs;
    }

    /**
     * Gets a sequence of jobs that can be used to compute characteristic sets
     * for RDF quads
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param intermediateOutputPath
     *            Intermediate output path
     * @param outputPath
     *            Final output path
     * @return Sequence of jobs
     * @throws IOException
     */
    public static Job[] getQuadCharacteristicSetJobs(Configuration config, String[] inputPaths, String intermediateOutputPath,
            String outputPath) throws IOException {
        Job[] jobs = new Job[2];

        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Quads Characteristic Set (Generation)");

        // Map/Reduce classes
        job.setMapperClass(QuadGroupBySubjectMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(QuadWritable.class);
        job.setReducerClass(QuadCharacteristicSetGeneratingReducer.class);
        job.setOutputKeyClass(CharacteristicSetWritable.class);
        job.setOutputValueClass(NullWritable.class);

        // Input and Output
        job.setInputFormatClass(QuadsInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(intermediateOutputPath));
        SequenceFileOutputFormat.setCompressOutput(job, true);
        FileOutputFormat.setOutputCompressorClass(job, BZip2Codec.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);

        jobs[0] = job;

        job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Quads Characteristic Set (Reduction)");

        // Map/Reduce classes
        job.setMapperClass(KeyMapper.class);
        job.setMapOutputKeyClass(CharacteristicSetWritable.class);
        job.setMapOutputValueClass(CharacteristicSetWritable.class);
        job.setReducerClass(CharacteristicSetReducer.class);
        job.setOutputKeyClass(CharacteristicSetWritable.class);
        job.setOutputValueClass(CharacteristicSetWritable.class);

        // Input and Output
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.setInputPaths(job, intermediateOutputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        jobs[1] = job;
        return jobs;
    }

    /**
     * Gets a sequence of jobs that can be used to compute characteristic sets
     * for RDF triple and/or quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param intermediateOutputPath
     *            Intermediate output path
     * @param outputPath
     *            Final output path
     * @return Sequence of jobs
     * @throws IOException
     */
    public static Job[] getCharacteristicSetJobs(Configuration config, String[] inputPaths, String intermediateOutputPath,
            String outputPath) throws IOException {
        Job[] jobs = new Job[2];

        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Characteristic Set (Generation)");

        // Map/Reduce classes
        job.setMapperClass(QuadGroupBySubjectMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(QuadWritable.class);
        job.setReducerClass(QuadCharacteristicSetGeneratingReducer.class);
        job.setOutputKeyClass(CharacteristicSetWritable.class);
        job.setOutputValueClass(NullWritable.class);

        // Input and Output
        job.setInputFormatClass(TriplesOrQuadsInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(intermediateOutputPath));
        SequenceFileOutputFormat.setCompressOutput(job, true);
        FileOutputFormat.setOutputCompressorClass(job, BZip2Codec.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);

        jobs[0] = job;

        job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Characteristic Set (Reduction)");

        // Map/Reduce classes
        job.setMapperClass(KeyMapper.class);
        job.setMapOutputKeyClass(CharacteristicSetWritable.class);
        job.setMapOutputValueClass(CharacteristicSetWritable.class);
        job.setReducerClass(CharacteristicSetReducer.class);
        job.setOutputKeyClass(CharacteristicSetWritable.class);
        job.setOutputValueClass(CharacteristicSetWritable.class);

        // Input and Output
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.setInputPaths(job, intermediateOutputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        jobs[1] = job;
        return jobs;
    }

    /**
     * Gets a job for computing type counts on RDF triple inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param intermediateOutputPath
     *            Path for intermediate output which will be all the type
     *            declaration triples present in the inputs
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job[] getTripleTypeCountJobs(Configuration config, String[] inputPaths, String intermediateOutputPath,
            String outputPath) throws IOException {
        Job[] jobs = new Job[2];

        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Type Triples Extraction");

        // Map/Reduce classes
        job.getConfiguration().setStrings(RdfMapReduceConstants.FILTER_PREDICATE_URIS, RDF.type.getURI());
        job.setMapperClass(TripleFilterByPredicateUriMapper.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(TripleWritable.class);

        // Input and Output Format
        job.setInputFormatClass(TriplesInputFormat.class);
        job.setOutputFormatClass(NTriplesOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(intermediateOutputPath));

        jobs[0] = job;

        // Object Node Usage count job
        job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Triples Type Usage Count");

        // Map/Reduce classes
        job.setMapperClass(TripleObjectCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(NTriplesInputFormat.class);
        NLineInputFormat.setNumLinesPerSplit(job, 10000); // TODO Would be
                                                          // better if this was
                                                          // intelligently
                                                          // configured
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, intermediateOutputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        jobs[1] = job;

        return jobs;
    }

    /**
     * Gets a job for computing type counts on RDF quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param intermediateOutputPath
     *            Path for intermediate output which will be all the type
     *            declaration quads present in the inputs
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job[] getQuadTypeCountJobs(Configuration config, String[] inputPaths, String intermediateOutputPath,
            String outputPath) throws IOException {
        Job[] jobs = new Job[2];

        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Type Quads Extraction");

        // Map/Reduce classes
        job.getConfiguration().setStrings(RdfMapReduceConstants.FILTER_PREDICATE_URIS, RDF.type.getURI());
        job.setMapperClass(QuadFilterByPredicateMapper.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(QuadWritable.class);

        // Input and Output Format
        job.setInputFormatClass(QuadsInputFormat.class);
        job.setOutputFormatClass(NQuadsOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(intermediateOutputPath));

        jobs[0] = job;

        // Object Node Usage count job
        job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Quads Type Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadObjectCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(NQuadsInputFormat.class);
        NLineInputFormat.setNumLinesPerSplit(job, 10000); // TODO Would be
                                                          // better if this was
                                                          // intelligently
                                                          // configured
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, intermediateOutputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        jobs[1] = job;

        return jobs;
    }

    /**
     * Gets a job for computing type counts on RDF triple and/or quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param intermediateOutputPath
     *            Path for intermediate output which will be all the type
     *            declaration quads present in the inputs
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job[] getTypeCountJobs(Configuration config, String[] inputPaths, String intermediateOutputPath,
            String outputPath) throws IOException {
        Job[] jobs = new Job[2];

        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Type Extraction");

        // Map/Reduce classes
        job.getConfiguration().setStrings(RdfMapReduceConstants.FILTER_PREDICATE_URIS, RDF.type.getURI());
        job.setMapperClass(QuadFilterByPredicateMapper.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(QuadWritable.class);

        // Input and Output Format
        job.setInputFormatClass(TriplesOrQuadsInputFormat.class);
        job.setOutputFormatClass(NQuadsOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(intermediateOutputPath));

        jobs[0] = job;

        // Object Node Usage count job
        job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Type Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadObjectCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(NQuadsInputFormat.class);
        NLineInputFormat.setNumLinesPerSplit(job, 10000); // TODO Would be
                                                          // better if this was
                                                          // intelligently
                                                          // configured
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, intermediateOutputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        jobs[1] = job;

        return jobs;
    }

    /**
     * Gets a job for computing literal data type counts on RDF triple inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getTripleDataTypeCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Triples Literal Data Type Usage Count");

        // Map/Reduce classes
        job.setMapperClass(TripleDataTypeCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(TriplesInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Gets a job for computing literal data type counts on RDF quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getQuadDataTypeCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Quads Literal Data Type Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadDataTypeCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(QuadsInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Gets a job for computing literal data type counts on RDF triple and/or
     * quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getDataTypeCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Literal Data Type Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadDataTypeCountMapper.class);
        job.setMapOutputKeyClass(NodeWritable.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(NodeCountReducer.class);

        // Input and Output
        job.setInputFormatClass(TriplesOrQuadsInputFormat.class);
        job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Gets a job for computing literal data type counts on RDF triple inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getTripleNamespaceCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Triples Namespace Usage Count");

        // Map/Reduce classes
        job.setMapperClass(TripleNamespaceCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(TextCountReducer.class);

        // Input and Output
        job.setInputFormatClass(TriplesInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Gets a job for computing literal data type counts on RDF quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getQuadNamespaceCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Quads Namespace Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadNamespaceCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(TextCountReducer.class);

        // Input and Output
        job.setInputFormatClass(QuadsInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Gets a job for computing literal data type counts on RDF triple and/or
     * quad inputs
     * 
     * @param config
     *            Configuration
     * @param inputPaths
     *            Input paths
     * @param outputPath
     *            Output path
     * @return Job
     * @throws IOException
     */
    public static Job getNamespaceCountJob(Configuration config, String[] inputPaths, String outputPath) throws IOException {
        Job job = Job.getInstance(config);
        job.setJarByClass(JobFactory.class);
        job.setJobName("RDF Namespace Usage Count");

        // Map/Reduce classes
        job.setMapperClass(QuadNamespaceCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setReducerClass(TextCountReducer.class);

        // Input and Output
        job.setInputFormatClass(TriplesOrQuadsInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.setInputPaths(job, StringUtils.arrayToString(inputPaths));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }
}
