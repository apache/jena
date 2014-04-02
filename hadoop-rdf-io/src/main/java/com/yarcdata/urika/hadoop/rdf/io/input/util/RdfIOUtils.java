/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.util;

import java.util.UUID;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.ParserProfileBase;
import org.apache.jena.riot.system.Prologue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yarcdata.urika.hadoop.rdf.io.HadoopIOConstants;

/**
 * RDF IO utility functions
 * 
 * @author rvesse
 * 
 */
public class RdfIOUtils {
    private static final Logger log = LoggerFactory.getLogger(RdfIOUtils.class);

    /**
     * Private constructor prevents instantiation
     */
    private RdfIOUtils() {
    }

    /**
     * Creates a parser profile for the given job context
     * 
     * @param context
     *            Context
     * @param path
     *            File path
     * @return Parser profile
     */
    public static ParserProfile createParserProfile(JobContext context, Path path) {
        Prologue prologue = new Prologue(null, IRIResolver.createNoResolve());
        UUID seed = RdfIOUtils.getSeed(context, path);
        LabelToNode labelMapping = LabelToNode.createScopeByDocumentHash(seed);
        return new ParserProfileBase(prologue, ErrorHandlerFactory.errorHandlerStd, labelMapping);
    }

    /**
     * Selects a seed for use in generating blank node identifiers
     * 
     * @param context
     *            Job Context
     * @param path
     *            File path
     * @return Seed
     */
    public static UUID getSeed(JobContext context, Path path) {
        // This is to ensure that blank node allocation policy is constant when
        // subsequent MapReduce jobs need that
        String jobId = context.getJobID().toString();
        if (jobId == null) {
            jobId = String.valueOf(System.currentTimeMillis());
            log.warn(
                    "Job ID was not set, using current milliseconds of {}. Sequence of MapReduce jobs must handle carefully blank nodes.",
                    jobId);
        }
        log.debug("MapReduceAllocator({}, {})", jobId, path);

        // Form a reproducible seed for the run
        return new UUID(jobId.hashCode(), path.hashCode());
    }
}
