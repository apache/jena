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

import static java.util.Arrays.asList;

import java.util.List;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.system.progress.MonitorOutput;

/** TDB2 loader operations.
 *  These operations only work on TDB2 datasets.
 *
 * @see LoaderFactory
 * @see DataLoader
 */
public class Loader {
    /** Load the contents of files or remote web data into a dataset. */
    public static void load(DatasetGraph dataset, String...dataURLs) {
        load(dataset, false, dataURLs);
    }

    /** Load the contents of files or remote web data into a dataset. */
    public static void load(DatasetGraph dataset, boolean showProgress, String...dataURLs) {
        load(dataset, asList(dataURLs), showProgress);
    }

    /** Load the contents of files or remote web data into a dataset. */
    public static void load(DatasetGraph dataset, List<String> dataURLs, boolean showProgress) {
        DataLoader loader = create(dataset, showProgress);
        loader.startBulk();
        try {
            loader.load(dataURLs);
            loader.finishBulk();
        }
        catch (RuntimeException ex) {
            loader.finishException(ex);
            throw ex;
        }
    }

    /**
     * Create a {@link DataLoader}. {@code DataLoader}s provide a {@code StreamRDF}
     * interface as well as an operation to load data from files or URLs.
     * <p>
     * To use the loader:
     *
     * <pre>
     *  loader.startBulk();
     *    send data ...
     *        use stream()
     *        or load(files)
     *        or a mixture.
     *  loader.finishBulk();
     * </pre>
     */
    public static DataLoader create(DatasetGraph dataset, boolean showProgress) {
        MonitorOutput output = showProgress ? LoaderOps.outputToLog() : MonitorOutputs.nullOutput();
        DataLoader loader = LoaderFactory.createLoader(dataset, output);
        return loader;
    }

    /** Load the contents of files or remote web data into a dataset using the basic data loader. */
    public static void read(DatasetGraph dataset, String...dataURLs) {
        read(dataset, false, dataURLs);
    }

    /** Load the contents of files or remote web data into a dataset using the basic data loader.. */
    public static void read(DatasetGraph dataset, boolean showProgress, String...dataURLs) {
        read(dataset, asList(dataURLs), showProgress);
    }

    /** Load the contents of files or remote web data into a dataset using the basic data loader. */
    public static void read(DatasetGraph dataset, List<String> dataURLs, boolean showProgress) {
        MonitorOutput output = showProgress ? LoaderOps.outputToLog() : MonitorOutputs.nullOutput();
        DataLoader loader = LoaderFactory.basicLoader(dataset, output);
        loader.startBulk();
        try {
            loader.load(dataURLs);
            loader.finishBulk();
        }
        catch (RuntimeException ex) {
            loader.finishException(ex);
            throw ex;
        }
    }
}
