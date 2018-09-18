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

package jena;

import arq.cmdline.CmdARQ;
import jena.cmd.ArgDecl ;
import jena.cmd.CmdException ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.spatial.DatasetGraphSpatial;
import org.apache.jena.query.spatial.SpatialDatasetFactory;
import org.apache.jena.query.spatial.SpatialIndex;
import org.apache.jena.query.spatial.SpatialIndexContext;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spatial indexer application that will read a dataset and index its triples in
 * its spatial index.
 */
public class spatialindexer extends CmdARQ {

	private static Logger log = LoggerFactory.getLogger(spatialindexer.class);

	public static final ArgDecl assemblerDescDecl = new ArgDecl(
			ArgDecl.HasValue, "desc", "dataset");

	protected DatasetGraphSpatial dataset = null;
	protected SpatialIndex spatialIndex = null;
	protected SpatialIndexContext context = null; 
	protected ProgressMonitor progressMonitor;
		
	static public void main(String... argv) {
		new spatialindexer(argv).mainRun();
	}

	static public void testMain(String... argv) {
		new spatialindexer(argv).mainMethod();
	}

	protected spatialindexer(String[] argv) {
		super(argv);
		super.add(assemblerDescDecl, "--desc=", "Assembler description file");
		progressMonitor = new ProgressMonitor("properties indexed");
	}

	@Override
	protected void processModulesAndArgs() {
		super.processModulesAndArgs();
		// Two forms : with and without arg.
		// Maximises similarity with other tools.
		String file;
		if (super.contains(assemblerDescDecl)) {
			if (getValues(assemblerDescDecl).size() != 1)
				throw new CmdException("Multiple assembler descriptions given");
			if (getPositional().size() != 0)
				throw new CmdException(
						"Additional assembler descriptions given");
			file = getValue(assemblerDescDecl);
		} else {
			if (getNumPositional() != 1)
				throw new CmdException("Multiple assembler descriptions given");
			file = getPositionalArg(0);
		}

		if (file == null)
			throw new CmdException("No dataset specified");
		// Assumes a single test daatset description in the assembler file.
		Dataset ds = SpatialDatasetFactory.create(file);
		if (ds == null)
			throw new CmdException("No dataset description found");
		// get index.
		dataset = (DatasetGraphSpatial) (ds.asDatasetGraph());
		spatialIndex = dataset.getSpatialIndex();
		if (spatialIndex == null)
			throw new CmdException("Dataset has no spatial index");
		
		context= new SpatialIndexContext(spatialIndex);
	}

	@Override
	protected String getSummary() {
		return getCommandName() + " assemblerFile";
	}

	@Override
	protected void exec() {
		spatialIndex.startIndexing();
		Txn.executeRead(dataset, () -> dataset.find().forEachRemaining(quad -> {
			context.index(quad);
			progressMonitor.progressByOne();

		}));
		spatialIndex.finishIndexing();
		progressMonitor.close();
	}

	// TDBLoader has a similar progress monitor
	// Not used here to avoid making ARQ dependent on TDB
	// So potential to rationalise and put progress monitor in a common
	// utility class
	private static class ProgressMonitor {
		String progressMessage;
		long startTime;
		long progressCount;
		long intervalStartTime;
		long progressAtStartOfInterval;
		long reportingInterval = 10000; // milliseconds

		ProgressMonitor(String progressMessage) {
			this.progressMessage = progressMessage;
			start(); // in case start not called
		}

		void start() {
			startTime = System.currentTimeMillis();
			progressCount = 0L;
			startInterval();
		}

		private void startInterval() {
			intervalStartTime = System.currentTimeMillis();
			progressAtStartOfInterval = progressCount;
		}

		void progressByOne() {
			progressCount++;
			long now = System.currentTimeMillis();
			if (reportDue(now)) {
				report(now);
				startInterval();
			}
		}

		boolean reportDue(long now) {
			return now - intervalStartTime >= reportingInterval;
		}

		private void report(long now) {
			long progressThisInterval = progressCount
					- progressAtStartOfInterval;
			long intervalDuration = now - intervalStartTime;
			long overallDuration = now - startTime;
			String message = progressCount + " (" + progressThisInterval
					/ (intervalDuration / 1000) + " per second)"
					+ progressMessage + " (" + progressCount
					/ Math.max(overallDuration / 1000, 1)
					+ " per second overall)";
			log.info(message);
		}

		void close() {
			long overallDuration = System.currentTimeMillis() - startTime;
			String message = progressCount + " (" + progressCount
					/ Math.max(overallDuration / 1000, 1) + " per second) "
					+ progressMessage;
			log.info(message);
		}
	}
	
}
