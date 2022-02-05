/**
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

package org.apache.jena.jdbc.remote.utils;

import java.net.http.HttpClient;
import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.exec.http.DSP;
import org.apache.jena.sparql.exec.http.GSP;

/**
 * Test utility methods - network
 */
public class TestJdbcRemoteUtils {
    /**
     * Copies a dataset to a remote service that provides SPARQL 1.1 Graph Store
     * protocol support
     *
     * @param source
     *            Source Dataset
     * @param service
     *            Remote Graph Store protocol service
     */
    public static void copyToRemoteDataset(Dataset source, String service) {
        copyToRemoteDataset(source, service, null);
    }

    /**
     * Copies a dataset to a remote service that provides SPARQL 1.1 Graph Store
     * protocol support
     *
     * @param source
     *            Source Dataset
     * @param service
     *            Remote Graph Store protocol service
     * @param client
     *            HTTP Client
     */
    public static void copyToRemoteDataset(Dataset source, String service, HttpClient client) {
        if ( client == null )
            client = HttpEnv.getDftHttpClient();
        DSP.service(service).httpClient(client).PUT(source.asDatasetGraph());
    }

    // Code extracted from DatasetGraphAccessorHTTP so Apache Http Client still works.
    private static void copyToRemoteGraph(String service, Graph data, String gn, HttpClient client) {
        RDFFormat syntax = RDFFormat.TURTLE_BLOCKS;
        if ( client == null )
            client = HttpEnv.getDftHttpClient();
        GSP.service(service).defaultGraph().contentType(syntax).httpClient(client).PUT(data);
    }

    /**
     * Renames a graph of a dataset producing a new dataset so as to not modify
     * the original dataset
     *
     * @param ds
     *            Dataset
     * @param oldUri
     *            Old URI
     * @param newUri
     *            New URI
     * @return New Dataset
     */
    public static Dataset renameGraph(Dataset ds, String oldUri, String newUri) {
        Dataset dest = DatasetFactory.createTxnMem();
        if (oldUri == null) {
            // Rename default graph
            dest.addNamedModel(newUri, ds.getDefaultModel());
        } else {
            // Copy across default graph
            dest.setDefaultModel(ds.getDefaultModel());
        }

        Iterator<String> uris = ds.listNames();
        while (uris.hasNext()) {
            String uri = uris.next();
            if (uri.equals(oldUri)) {
                // Rename named graph
                if (newUri == null) {
                    dest.setDefaultModel(ds.getNamedModel(oldUri));
                } else {
                    dest.addNamedModel(newUri, ds.getNamedModel(oldUri));
                }
            } else {
                // Copy across named graph
                dest.addNamedModel(uri, ds.getNamedModel(uri));
            }
        }

        return dest;
    }

}
