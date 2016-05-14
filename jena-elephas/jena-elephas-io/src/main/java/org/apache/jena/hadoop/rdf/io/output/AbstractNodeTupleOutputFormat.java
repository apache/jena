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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of an output format for line based tuple formats
 * where the key is ignored and only the tuple values will be output
 * 
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Tuple value type
 * @param <T>
 *            Writable node tuple type
 * 
 */
public abstract class AbstractNodeTupleOutputFormat<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        FileOutputFormat<TKey, T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeTupleOutputFormat.class);

    @Override
    public RecordWriter<TKey, T> getRecordWriter(TaskAttemptContext context) throws IOException {
        Configuration config = context.getConfiguration();
        boolean isCompressed = getCompressOutput(context);
        CompressionCodec codec = null;
        
        // Build the output file path
        String extension = this.getFileExtension();
        if (isCompressed) {
            // Add compression extension if applicable
            Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(context, GzipCodec.class);
            codec = ReflectionUtils.newInstance(codecClass, config);
            extension += codec.getDefaultExtension();
        }
        Path file = getDefaultWorkFile(context, extension);
        LOG.info("Writing output to file " + file);
        
        // Open the file appropriately and create a record writer for it
        FileSystem fs = file.getFileSystem(config);
        if (!isCompressed) {
            FSDataOutputStream fileOut = fs.create(file, false);
            return this.getRecordWriter(new OutputStreamWriter(fileOut), config, file);
        } else {
            FSDataOutputStream fileOut = fs.create(file, false);
            return this.getRecordWriter(new OutputStreamWriter(codec.createOutputStream(fileOut)), config, file);
        }
    }

    /**
     * Gets the file extension to use for output
     * 
     * @return File extension including the '.'
     */
    protected abstract String getFileExtension();

    /**
     * Gets the record writer to use
     * 
     * @param writer
     *            Writer to write output to
     * @param config
     *            Configuration
     * @param outputPath
     *            Output path being written to
     * @return Record writer
     * @throws IOException
     *             May be thrown if a record writer cannot be obtained for any
     *             reason
     */
    protected abstract RecordWriter<TKey, T> getRecordWriter(Writer writer, Configuration config, Path outputPath)
            throws IOException;

}
