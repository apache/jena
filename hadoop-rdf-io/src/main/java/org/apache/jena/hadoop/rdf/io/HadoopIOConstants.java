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

/**
 * Hadoop IO related constants
 * 
 * 
 * 
 */
public class HadoopIOConstants {

    /**
     * Private constructor prevents instantiation
     */
    private HadoopIOConstants() {
    }

    /**
     * Map Reduce configuration setting for max line length
     */
    public static final String MAX_LINE_LENGTH = "mapreduce.input.linerecordreader.line.maxlength";

    /**
     * Run ID
     */
    public static final String RUN_ID = "runId";
    
    /**
     * Compression codecs to use
     */
    public static final String IO_COMPRESSION_CODECS = "io.compression.codecs";
}
