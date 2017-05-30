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
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.io.input.util.* ;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder ;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation for a record reader that reads records from blocks
 * of files, this is a hybrid between {@link AbstractLineBasedNodeTupleReader}
 * and {@link AbstractWholeFileNodeTupleReader} in that it can only be used by
 * formats which can be split by lines but reduces the overhead by parsing the
 * split as a whole rather than as individual lines.
 * <p>
 * The keys produced are the approximate position in the file at which a tuple
 * was found and the values will be node tuples. Positions are approximate
 * because they are recorded after the point at which the most recent tuple was
 * parsed from the input thus they reflect the approximate position in the
 * stream immediately after which the triple was found.
 * </p>
 * 
 * 
 * 
 * @param <TValue>
 *            Value type
 * @param <T>
 *            Tuple type
 */
public abstract class AbstractBlockBasedNodeTupleReader<TValue, T extends AbstractNodeTupleWritable<TValue>> extends RecordReader<LongWritable, T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBlockBasedNodeTupleReader.class);
    private CompressionCodec compressionCodecs;
    private TrackableInputStream input;
    private LongWritable key;
    private long start, length;
    private T tuple;
    private TrackedPipedRDFStream<TValue> stream;
    private PipedRDFIterator<TValue> iter;
    private Thread parserThread;
    private boolean finished = false;
    private boolean ignoreBadTuples = true;
    private boolean parserFinished = false;
    private Throwable parserError = null;

    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
        LOG.debug("initialize({}, {})", genericSplit, context);

        // Assuming file split
        if (!(genericSplit instanceof FileSplit))
            throw new IOException("This record reader only supports FileSplit inputs");
        FileSplit split = (FileSplit) genericSplit;

        // Configuration
        Configuration config = context.getConfiguration();
        this.ignoreBadTuples = config.getBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, true);
        if (this.ignoreBadTuples)
            LOG.warn(
                    "Configured to ignore bad tuples, parsing errors will be logged and further parsing aborted but no user visible errors will be thrown.  Consider setting {} to false to disable this behaviour",
                    RdfIOConstants.INPUT_IGNORE_BAD_TUPLES);

        // Figure out what portion of the file to read
        start = split.getStart();
        long end = start + split.getLength();
        final Path file = split.getPath();
        long totalLength = file.getFileSystem(context.getConfiguration()).getFileStatus(file).getLen();
        boolean readToEnd = end == totalLength;
        CompressionCodecFactory factory = new CompressionCodecFactory(config);
        this.compressionCodecs = factory.getCodec(file);

        LOG.info(String.format("Got split with start %d and length %d for file with total length of %d", new Object[] { start, split.getLength(), totalLength }));

        // Open the file and prepare the input stream
        FileSystem fs = file.getFileSystem(config);
        FSDataInputStream fileIn = fs.open(file);
        this.length = split.getLength();
        if (start > 0)
            fileIn.seek(start);

        if (this.compressionCodecs != null) {
            // Compressed input
            // For compressed input NLineInputFormat will have failed to find
            // any line breaks and will give us a split from 0 -> (length - 1)
            // Add 1 and re-verify readToEnd so we can abort correctly if ever
            // given a partial split of a compressed file
            end++;
            readToEnd = end == totalLength;
            if (start > 0 || !readToEnd)
                throw new IOException("This record reader can only be used with compressed input where the split is a whole file");
            input = new TrackedInputStream(this.compressionCodecs.createInputStream(fileIn));
        } else {
            // Uncompressed input

            if (readToEnd) {
                input = new TrackedInputStream(fileIn);
            } else {
                // Need to limit the portion of the file we are reading
                input = new BlockInputStream(fileIn, split.getLength());
            }
        }

        // Set up background thread for parser
        iter = this.getPipedIterator();
        this.stream = this.getPipedStream(iter, this.input);
        RDFParserBuilder builder = RdfIOUtils.createRDFParserBuilder(context, file);
        Runnable parserRunnable = this.createRunnable(this, this.input, stream, this.getRdfLanguage(), builder);
        
        this.parserThread = new Thread(parserRunnable);
        this.parserThread.setDaemon(true);
        this.parserThread.start();
    }

    /**
     * Gets the RDF iterator to use
     * 
     * @return Iterator
     */
    protected abstract PipedRDFIterator<TValue> getPipedIterator();

    /**
     * Gets the RDF stream to parse to
     * 
     * @param iterator
     *            Iterator
     * @return RDF stream
     */
    protected abstract TrackedPipedRDFStream<TValue> getPipedStream(PipedRDFIterator<TValue> iterator, TrackableInputStream input);

    /**
     * Gets the RDF language to use for parsing
     * 
     * @return
     */
    protected abstract Lang getRdfLanguage();

    /**
     * Creates the runnable upon which the parsing will run
     * 
     * @param input
     *            Input
     * @param stream
     *            Stream
     * @param lang
     *            Language to use for parsing
     * @param builder 
     *     RDFParser setup
     * @return Parser runnable
     */
    private Runnable createRunnable(final AbstractBlockBasedNodeTupleReader<?, ?> reader, final InputStream input,
                                    final PipedRDFStream<TValue> stream, final Lang lang, RDFParserBuilder builder) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    builder.lang(lang).source(input).parse(stream);
                    reader.setParserFinished(null);
                } catch (Throwable e) {
                    reader.setParserFinished(e);
                }
            }
        };
    }

    /**
     * Sets the parser thread finished state
     * 
     * @param e
     *            Error (if any)
     */
    private void setParserFinished(Throwable e) {
        synchronized (this.parserThread) {
            this.parserError = e;
            this.parserFinished = true;
        }
    }

    /**
     * Waits for the parser thread to have reported as finished
     * 
     * @throws InterruptedException
     */
    private void waitForParserFinished() throws InterruptedException {
        do {
            synchronized (this.parserThread) {
                if (this.parserFinished)
                    return;
            }
            Thread.sleep(50);
        } while (true);
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
    public boolean nextKeyValue() throws IOException {
        // Reuse key for efficiency
        if (key == null) {
            key = new LongWritable();
        }

        if (this.finished)
            return false;

        try {
            if (this.iter.hasNext()) {
                // Position will be relative to the start for the split we're
                // processing
                Long l = this.start + this.stream.getPosition();
                if (l != null) {
                    this.key.set(l);
                    // For compressed input the actual length from which we
                    // calculate progress is likely less than the actual
                    // uncompressed length so we need to increment the
                    // length as we go along
                    // We always add 1 more than the current length because we
                    // don't want to report 100% progress until we really have
                    // finished
                    if (this.compressionCodecs != null && l > this.length)
                        this.length = l + 1;
                }
                this.tuple = this.createInstance(this.iter.next());
                return true;
            } else {
                // Need to ensure that the parser thread has finished in order
                // to determine whether we finished without error
                this.waitForParserFinished();
                if (this.parserError != null) {
                    LOG.error("Error parsing block, aborting further parsing", this.parserError);
                    if (!this.ignoreBadTuples)
                        throw new IOException("Error parsing block at position " + (this.start + this.input.getBytesRead()) + ", aborting further parsing",
                                this.parserError);
                }

                this.key = null;
                this.tuple = null;
                this.finished = true;
                // This is necessary so that when compressed input is used we
                // report 100% progress once we've reached the genuine end of
                // the stream
                if (this.compressionCodecs != null)
                    this.length--;
                return false;
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            // Failed to read the tuple on this line
            LOG.error("Error parsing block, aborting further parsing", e);
            if (!this.ignoreBadTuples) {
                this.iter.close();
                throw new IOException("Error parsing block at position " + (this.start + this.input.getBytesRead()) + ", aborting further parsing", e);
            }
            this.key = null;
            this.tuple = null;
            this.finished = true;
            return false;
        }
    }

    @Override
    public LongWritable getCurrentKey() {
        return this.key;
    }

    @Override
    public T getCurrentValue() {
        return this.tuple;
    }

    @Override
    public float getProgress() {
        float progress = 0.0f;
        if (this.key == null) {
            // We've either not started or we've finished
            progress = (this.finished ? 1.0f : 0.0f);
        } else if (this.key.get() == Long.MIN_VALUE) {
            // We don't have a position so we've either in-progress or finished
            progress = (this.finished ? 1.0f : 0.5f);
        } else {
            // We're some way through the file
            progress = (this.key.get() - this.start) / (float) this.length;
        }
        LOG.debug("getProgress() --> {}", progress);
        return progress;
    }

    @Override
    public void close() throws IOException {
        this.iter.close();
        this.input.close();
        this.finished = true;
    }

}
