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

package org.apache.jena.hadoop.rdf.io.input.compressed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.jena.hadoop.rdf.io.HadoopIOConstants;
import org.apache.jena.hadoop.rdf.io.input.AbstractNodeTupleInputFormatTests;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;


/**
 * 
 * 
 * @param <TValue>
 * @param <T>
 */
public abstract class AbstractCompressedNodeTupleInputFormatTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractNodeTupleInputFormatTests<TValue, T> {
    
    @Override
    protected Configuration prepareConfiguration() {
        Configuration config = super.prepareConfiguration();
        config.set(HadoopIOConstants.IO_COMPRESSION_CODECS, this.getCompressionCodec().getClass().getCanonicalName());
        return config;
    }

    @Override
    protected OutputStream getOutputStream(File f) throws IOException {
        CompressionCodec codec = this.getCompressionCodec();
        if (codec instanceof Configurable) {
            ((Configurable) codec).setConf(this.prepareConfiguration());
        }
        FileOutputStream fileOutput = new FileOutputStream(f, false);
        return codec.createOutputStream(fileOutput);
    }

    /**
     * Gets the compression codec to use
     * 
     * @return Compression codec
     */
    protected abstract CompressionCodec getCompressionCodec();

    /**
     * Indicates whether inputs can be split, defaults to false for compressed
     * input tests
     */
    @Override
    protected boolean canSplitInputs() {
        return false;
    }
}
