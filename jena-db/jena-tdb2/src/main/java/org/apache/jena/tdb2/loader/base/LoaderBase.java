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

package org.apache.jena.tdb2.loader.base;

import java.io.InputStream;
import java.util.List;

import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.graph.Node;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.progress.MonitorOutput;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.tdb2.loader.DataLoader;

/**
 * Bulk loader framework.
 * <p>
 * It puts a write-transaction around the whole process if {@link #bulkUseTransaction}
 * returns true. It calls {@link #loadOne} for each file.
 * <p>
 * If a graph name is provided, it converts triples to quads in that named graph.
 */
public abstract class LoaderBase implements DataLoader {

    protected final DatasetGraph dsg;
    protected final Node graphName;
    private Timer timer;
    protected final MonitorOutput output;

    protected LoaderBase(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        this.dsg = dsg;
        this.graphName = graphName;
        this.output = output;
    }

    @Override
    public void startBulk() {
        this.timer = new Timer();
        timer.startTimer();
        if ( bulkUseTransaction() )
            dsg.begin(TxnType.WRITE);
    }

    @Override
    public void finishBulk() {
        if ( bulkUseTransaction() ) {
            dsg.commit();
            dsg.end();
        }
        long totalElapsed = timer.endTimer();
        outputSummary(totalElapsed);
    }

    @Override
    public void finishException(Exception ex) {
        if ( bulkUseTransaction() ) {
            dsg.abort();
            dsg.end();
        }
    }

    @Override
    public void load(List<String> filenames) {
        if ( filenames.isEmpty() ) {
            output.print("No files to load");
            return;
        }

        ProgressMonitor monitor = createProgressMonitor(output);

        boolean multipleFiles = (filenames.size()>1);

        String file1 = null;
        if ( ! multipleFiles ) {
            file1 = filenames.get(0);
            if ( file1.equals("-") )
                file1 = "stdin";
        }
        try {
            if ( multipleFiles )
                monitor.startMessage("Start: "+filenames.size()+" files");
            else
                monitor.startMessage("Start: "+file1);
            monitor.start();
            filenames.forEach(fn->{
                if ( multipleFiles )
                    monitor.startSection();
                loadOne(fn, monitor);
                if ( multipleFiles )
                    monitor.finishSection();
            });
            monitor.finish();
            if ( multipleFiles )
                monitor.finishMessage("Finished: "+filenames.size()+" files");
            else
                monitor.finishMessage("Finished: "+file1);
        } catch (Exception ex) {
            finishException(ex);
            throw ex;
        }
    }

    @Override
    public void loadFromInputStream(String label, InputStream input, Lang syntax) {
        ProgressMonitor monitor = createProgressMonitor(output);
        monitor.startMessage("Start: "+label);
        monitor.start();
        LoaderOps.inputStream(stream(), input, syntax, monitor);
        monitor.finish();
        monitor.finishMessage("Finished: "+label);
    }

    protected abstract ProgressMonitor createProgressMonitor(MonitorOutput output);

    /** Subclasses must provide a setting. */
    protected abstract boolean bulkUseTransaction();

    protected void loadOne(String filename, ProgressMonitor monitor) {
        String label = LoaderOps.label(filename);
        monitor.setLabel(label);
        LoaderOps.inputFile(stream(), filename, monitor);
    }

    protected void outputSummary(long totalElapsed) {
        if ( output != null ) {
            long count = countTriples()+countQuads();
            String label = "Triples/Quads";
            if ( countTriples() == 0 && countQuads() > 0 )
                label = "Quads";
            if ( countTriples() > 0 && countQuads() == 0 )
                label = "Triples";
            double seconds = totalElapsed/1000.0;
            if ( seconds > 1 )
                output.print("Time = %,.3f seconds : %s = %,d : Rate = %,.0f /s", seconds, label, count, count/seconds);
        }
    }
}
