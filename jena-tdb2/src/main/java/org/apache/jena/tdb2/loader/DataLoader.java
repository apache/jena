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

package org.apache.jena.tdb2.loader;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;

/**
 * Bulk loaders improve the loading of data into datasets. Each bulk loader has
 * consequences in achieving its improvements, including in some cases locking out all
 * other access to the dataset while the loading is underway.
 * <p>
 * Finding the best loader to use takes experimentation.
 * Loading speed depends on hardware, particularly for the parallel bulk loader.
 * <p>
 * Giving a loader more heap space does <em>not</em> improve performance, and will likely decrease it.
 * All loaders use OS file system caching, not in JVM caches (except when run in {@link FileMode#direct direct file mode}s
 * for special circumstances).
 *
 * <h4>basic</h4>
 * The basic loader is full transactional and good for incrementally adding data up to a few million triples/quads
 * to large datasets and does not max out the hardware so it is suitable for runtime operation at larger scales.
 *
 * <h4>sequential</h4>
 * A fully transactional loader that loads the primary indexes then does multiple passes to load the secondary indexes.
 * This maximises RAM file system caching effects.
 * It can be useful when hardware is restricted and I/O is slow (disk, not non volatile storage liek SSDs).
 *
 * <h4>phased</h4>
 * The phased loader use some multiple threads to process data and to index the {@code DatasetGraph}.
 * It proceeds by loading data into the primary indexes, then, separately, builds the other indexes.
 * Loading is not fully transaction-safe in the presence of persistent
 * storage problems or a JVM/machine crash when finishing writing.
 * Otherwise it is transactional.
 *
 * <h4>parallel</h4>
 * The parallel loader use multiple threads to process data and to index the {@code DatasetGraph}.
 * Loading is not fully transaction-safe in the presence of persistent
 * storage problems or a JVM/machine crash when finishing writing.
 * Otherwise it is transactional.
 * Because it uses many threads to write to persistent storage,
 * it can interfere with performance of other applications on the machine it is run on.
 *
 * <h4>{@code DataLoader} API</h4>
 *
 * To use a {@code DataLoader}:
 *
 * <pre>
 *   loader.startBulk();
 *   try {
 *   send data ...
 *        use stream()
 *        or load(files)
 *        or a mixture.
 *   loader.finishBulk();
 *   } catch (RuntimeException ex) {
 *     loader.finishException(ex);
 *     .. optionally rethrow exception ..
 *   }
 * </pre>
 *
 * @see LoaderFactory LoaderFactory for creating DataLoaders.
 * @see Loader Loader for convenience operations to invoke the default loader.
 */
public interface DataLoader {

    /** Start bulk loading. */
    public void startBulk();

    /**
     * Finish bulk loading. This operation waits until the loading process has completed
     * and all changes have been committed.
     */
    public void finishBulk();

    /**
     * Alternative finish in case something went wrong.
     * This operation attempts to clear up and abort the changes.
     * If there was a file system problem with the {@code DatasetGraph} being
     * loaded, then recovery may not have been possible.
     * The ability of loaders to cleanup is implementation specific.
     */
    public void finishException(Exception ex);

    /**
     * Load files with syntax given by the file name extension,
     * or URLs, with content negotiation.
     * @param filenames
     */
    public void load(List<String> filenames);

    /**
     * Load from an {@link InputStream} with the given syntax.
     * @param label Label for progress monitor
     * @param input
     * @param syntax
     */
    public void loadFromInputStream(String label, InputStream input, Lang syntax);

    /**
     * Load files with syntax given by the file name extension,
     * or URLs, with content negotiation.
     * @param filenames
     */
    default public void load(String ... filenames) { load(Arrays.asList(filenames)); }

    /** Send data to the loader by {@link StreamRDF} */
    public StreamRDF stream();

    /** Return count of triples sent to the loader and added. This is not a count of unique triples. */
    public long countTriples();

    /** Return count of quads sent to the loader and added. This is not a count of unique quads. */
    public long countQuads();
}
