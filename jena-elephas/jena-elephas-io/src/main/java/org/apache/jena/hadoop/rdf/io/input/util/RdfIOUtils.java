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

package org.apache.jena.hadoop.rdf.io.input.util;

import java.util.UUID;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.riot.RDFParser ;
import org.apache.jena.riot.RDFParserBuilder ;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.* ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RDF IO utility functions
 * 
 * 
 * 
 */
public class RdfIOUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdfIOUtils.class);

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
     * @deprecated Legacy - use {@link #createRDFParserBuilder}.
     */
    @Deprecated
    public static ParserProfile createParserProfile(JobContext context, Path path) {
        LabelToNode labelMapping = createLabelToNode(context, path);
        ParserProfile profile = RiotLib.createParserProfile(RiotLib.factoryRDF(labelMapping), ErrorHandlerFactory.errorHandlerStd,
                                                  IRIResolver.createNoResolve(), false);
        return profile;
    }

    public static RDFParserBuilder createRDFParserBuilder(JobContext context, Path path) {
        LabelToNode labelMapping = createLabelToNode(context, path);
        RDFParserBuilder builder = RDFParser.create()
                .labelToNode(labelMapping)
                .errorHandler(ErrorHandlerFactory.errorHandlerStd) ;
        return builder ;
    }
    
    public static LabelToNode createLabelToNode(JobContext context, Path path) {
        UUID seed = RdfIOUtils.getSeed(context, path);
        LabelToNode labelMapping = LabelToNode.createScopeByDocumentHash(seed);
        return labelMapping;
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
            LOGGER.warn(
                    "Job ID was not set, using current milliseconds of {}. Sequence of MapReduce jobs must carefully handle blank nodes.",
                    jobId);
        }

        if (!context.getConfiguration().getBoolean(RdfIOConstants.GLOBAL_BNODE_IDENTITY, false)) {
            // Using normal file scoped blank node allocation
            LOGGER.debug("Generating Blank Node Seed from Job Details (ID={}, Input Path={})", jobId, path);

            // Form a reproducible seed for the run
            return new UUID(jobId.hashCode(), path.hashCode());
        } else {
            // Using globally scoped blank node allocation
            LOGGER.warn(
                    "Using globally scoped blank node allocation policy from Job Details (ID={}) - this is unsafe if your RDF inputs did not originate from a previous job",
                    jobId);
            
            return new UUID(jobId.hashCode(), 0);
        }
    }
}
