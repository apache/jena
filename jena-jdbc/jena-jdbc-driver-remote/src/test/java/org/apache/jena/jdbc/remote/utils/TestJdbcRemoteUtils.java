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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.Graph;
import org.apache.jena.jdbc.remote.http.HttpOp1;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

/**
 * Test utility methods - network
 */
@SuppressWarnings("deprecation")
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
        copyToRemoteGraph(service, source.getDefaultModel().getGraph(), null, client);
        Iterator<String> uris = source.listNames();
        while (uris.hasNext()) {
            String uri = uris.next();
            copyToRemoteGraph(service, source.getNamedModel(uri).getGraph(), uri, client);
        }
    }

    // Code extracted from DatasetGraphAccessorHTTP so Apache Http Client still works.
    private static void copyToRemoteGraph(String service, Graph data, String gn, HttpClient client) {
        RDFFormat syntax = RDFFormat.TURTLE_BLOCKS;
        String url = ( gn == null ) ? service+"?default" : service+"?graph="+IRILib.encodeUriComponent(gn);
        String ct = syntax.getLang().getContentType().toHeaderString();
        ByteArrayOutputStream out = new ByteArrayOutputStream(128*1024);
        RDFDataMgr.write(out, data, syntax);
        IO.close(out);
        ByteArrayEntity entity = new ByteArrayEntity(out.toByteArray());
        entity.setContentType(ct);
        HttpOp1.execHttpPut(url, entity, client, null) ;
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
