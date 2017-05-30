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

package org.apache.jena.hadoop.rdf.io.input.readers;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;
import org.apache.jena.hadoop.rdf.io.HadoopIOConstants;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.io.input.util.RdfIOUtils;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of a record reader that reads records from line
 * based tuple formats. This only supports reading from file splits currently.
 * <p>
 * The keys produced are the position of the line in the file and the values
 * will be node tuples
 * </p>
 * 
 * 
 * 
 * @param <TValue>
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractLineBasedNodeTupleReader<TValue, T extends AbstractNodeTupleWritable<TValue>> extends RecordReader<LongWritable, T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLineBasedNodeTupleReader.class);
    private CompressionCodecFactory compressionCodecs = null;
    private long start, pos, end, estLength;
    private int maxLineLength;
    private LineReader in;
    private LongWritable key = null;
    private Text value = null;
    private T tuple = null;
    private ParserProfile maker = null;
    private boolean ignoreBadTuples = true;

    @Override
    public final void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
        LOG.debug("initialize({}, {})", genericSplit, context);

        // Assuming file split
        if (!(genericSplit instanceof FileSplit))
            throw new IOException("This record reader only supports FileSplit inputs");
        FileSplit split = (FileSplit) genericSplit;

        // Intermediate : RDFParser but need to make a Iterator<Quad/Triple>
        LabelToNode labelToNode = RdfIOUtils.createLabelToNode(context, split.getPath());
        maker = new ParserProfileStd(RiotLib.factoryRDF(labelToNode), 
                                     ErrorHandlerFactory.errorHandlerStd, 
                                     IRIResolver.create(), PrefixMapFactory.createForInput(), 
                                     null, true, false); 
        
        Configuration config = context.getConfiguration();
        this.ignoreBadTuples = config.getBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, true);
        if (this.ignoreBadTuples)
            LOG.warn(
                    "Configured to ignore bad tuples, parsing errors will be logged and the bad line skipped but no errors will be thrownConsider setting {} to false to disable this behaviour",
                    RdfIOConstants.INPUT_IGNORE_BAD_TUPLES);

        // Figure out what portion of the file to read
        this.maxLineLength = config.getInt(HadoopIOConstants.MAX_LINE_LENGTH, Integer.MAX_VALUE);
        start = split.getStart();
        end = start + split.getLength();
        final Path file = split.getPath();
        long totalLength = file.getFileSystem(context.getConfiguration()).getFileStatus(file).getLen();
        compressionCodecs = new CompressionCodecFactory(config);
        final CompressionCodec codec = compressionCodecs.getCodec(file);

        LOG.info(String.format("Got split with start %d and length %d for file with total length of %d", new Object[] { start, split.getLength(), totalLength }));

        // Open the file and seek to the start of the split
        FileSystem fs = file.getFileSystem(config);
        FSDataInputStream fileIn = fs.open(file);
        boolean skipFirstLine = false;
        if (codec != null) {
            // Compressed input
            // For compressed input NLineInputFormat will have failed to find
            // any line breaks and will give us a split from 0 -> (length - 1)
            // Add 1 and verify we got complete split
            if (totalLength > split.getLength() + 1)
                throw new IOException("This record reader can only be used with compressed input where the split covers the whole file");
            in = new LineReader(codec.createInputStream(fileIn), config);
            estLength = end;
            end = Long.MAX_VALUE;
        } else {
            // Uncompressed input
            if (start != 0) {
                skipFirstLine = true;
                --start;
                fileIn.seek(start);
            }
            in = new LineReader(fileIn, config);
        }
        // Skip first line and re-establish "start".
        // This is to do with how line reader reads lines and how
        // NLineInputFormat will provide the split information to use
        if (skipFirstLine) {
            start += in.readLine(new Text(), 0, (int) Math.min(Integer.MAX_VALUE, end - start));
        }
        this.pos = start;
    }

    /**
     * Gets an iterator over the data on the current line
     * 
     * @param line
     *            Line
     * @param builder
     *            Parser setup.
     * @return Iterator
     */
    protected abstract Iterator<TValue> getIterator(String line, ParserProfile maker);
    
    /** Create a tokenizer for a line
     * @param line
     *          Content
     * @return Tokenizer
     */
    protected Tokenizer getTokenizer(String line) {
        return TokenizerFactory.makeTokenizerString(line);
    }


    /**
     * Creates an instance of a writable tuple from the given tuple value
     * 
     * @param tuple
     *            Tuple value
     * @return Writable tuple
     */
    protected abstract T createInstance(TValue tuple);

    @Override
    public final boolean nextKeyValue() throws IOException {
        // Reuse key for efficiency
        if (key == null) {
            key = new LongWritable();
        }

        // Reset value which we use for reading lines
        if (value == null) {
            value = new Text();
        }
        tuple = null;

        // Try to read the next valid line
        int newSize = 0;
        while (pos < end) {
            // Read next line
            newSize = in.readLine(value, maxLineLength, Math.max((int) Math.min(Integer.MAX_VALUE, end - pos), maxLineLength));

            // Once we get an empty line we've reached the end of our input
            if (newSize == 0) {
                break;
            }

            // Update position, remember that where inputs are compressed we may
            // be at a larger position then we expected because the length of
            // the split is likely less than the length of the data once
            // decompressed
            key.set(pos);
            pos += newSize;
            if (pos > estLength)
                estLength = pos + 1;

            // Skip lines that exceed the line length limit that has been set
            if (newSize >= maxLineLength) {
                LOG.warn("Skipped oversized line of size {} at position {}", newSize, (pos - newSize));
                continue;
            }

            // Attempt to read the tuple from current line
            try {
                Iterator<TValue> iter = this.getIterator(value.toString(), maker);
                if (iter.hasNext()) {
                    tuple = this.createInstance(iter.next());

                    // If we reach here we've found a valid tuple so we can
                    // break out of the loop
                    break;
                } else {
                    // Empty line/Comment line
                    LOG.debug("Valid line with no triple at position {}", (pos - newSize));
                    continue;
                }
            } catch (Throwable e) {
                // Failed to read the tuple on this line
                LOG.error("Bad tuple at position " + (pos - newSize), e);
                if (this.ignoreBadTuples)
                    continue;
                throw new IOException(String.format("Bad tuple at position %d", (pos - newSize)), e);
            }
        }
        boolean result = this.tuple != null;

        // End of input
        if (newSize == 0) {
            key = null;
            value = null;
            tuple = null;
            result = false;
            estLength = pos;
        }
        LOG.debug("nextKeyValue() --> {}", result);
        return result;
    }

    @Override
    public LongWritable getCurrentKey() {
        LOG.debug("getCurrentKey() --> {}", key);
        return key;
    }

    @Override
    public T getCurrentValue() {
        LOG.debug("getCurrentValue() --> {}", tuple);
        return tuple;
    }

    @Override
    public float getProgress() {
        float progress = 0.0f;
        if (start != end) {
            if (end == Long.MAX_VALUE) {
                if (estLength == 0)
                    return 1.0f;
                // Use estimated length
                progress = Math.min(1.0f, (pos - start) / (float) (estLength - start));
            } else {
                // Use actual length
                progress = Math.min(1.0f, (pos - start) / (float) (end - start));
            }
        }
        LOG.debug("getProgress() --> {}", progress);
        return progress;
    }

    @Override
    public void close() throws IOException {
        LOG.debug("close()");
        if (in != null) {
            in.close();
        }
    }

}