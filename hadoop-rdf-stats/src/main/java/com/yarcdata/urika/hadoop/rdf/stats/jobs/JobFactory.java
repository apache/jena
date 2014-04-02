/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.stats.jobs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;

import com.hp.hpl.jena.vocabulary.RDF;
import com.yarcdata.urika.hadoop.rdf.io.input.NQuadsInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.NTriplesInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.QuadsInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.TriplesInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.input.TriplesOrQuadsInputFormat;
import com.yarcdata.urika.hadoop.rdf.io.output.NQuadsOutputFormat;
import com.yarcdata.urika.hadoop.rdf.io.output.NTriplesNodeOutputFormat;
import com.yarcdata.urika.hadoop.rdf.io.output.NTriplesOutputFormat;
import com.yarcdata.urika.hadoop.rdf.mapreduce.KeyMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.RdfMapReduceConstants;
import com.yarcdata.urika.hadoop.rdf.mapreduce.TextCountReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics.CharacteristicSetReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics.QuadCharacteristicSetGeneratingReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics.TripleCharacteristicSetGeneratingReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.NodeCountReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.QuadNodeCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.TripleNodeCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.datatypes.QuadDataTypeCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.datatypes.TripleDataTypeCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.namespaces.QuadNamespaceCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.namespaces.TripleNamespaceCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.positional.QuadObjectCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.positional.TripleObjectCountMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.filter.positional.QuadFilterByPredicateMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.filter.positional.TripleFilterByPredicateUriMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.group.QuadGroupBySubjectMapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.group.TripleGroupBySubjectMapper;
import com.yarcdata.urika.hadoop.rdf.types.CharacteristicSetWritable;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Factory that can produce {@link Job} instances for computing various RDF
 * statistics
 * 
 * @author rvesse
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
