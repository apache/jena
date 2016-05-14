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

package org.apache.jena.hadoop.rdf.mapreduce.count.namespaces;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.jena.graph.Node ;
import org.apache.jena.hadoop.rdf.mapreduce.TextCountReducer;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;

/**
 * Abstract mapper class for mappers which split node tuple values and extract
 * the namespace URIs they use and outputs pairs of namespaces keys with a long
 * value of 1. Can be used in conjunction with a {@link TextCountReducer} to
 * count the usages of each unique namespace.
 * 
 * 
 * 
 * @param <TKey>
 * @param <TValue>
 * @param <T>
 */
public abstract class AbstractNodeTupleNamespaceCountMapper<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        Mapper<TKey, T, Text, LongWritable> {

    private LongWritable initialCount = new LongWritable(1);
    protected static final String NO_NAMESPACE = null;

    @Override
    protected void map(TKey key, T value, Context context) throws IOException, InterruptedException {
        NodeWritable[] ns = this.getNodes(value);
        for (NodeWritable n : ns) {
            String namespace = this.extractNamespace(n);
            if (namespace != null) {
                context.write(new Text(namespace), this.initialCount);
            }
        }
    }

    /**
     * Extracts the namespace from a node
     * <p>
     * Finds the URI for the node (if any) and then invokes
     * {@link #extractNamespace(String)} to extract the actual namespace URI.
     * </p>
     * <p>
     * Derived classes may override this to change the logic of how namespaces
     * are extracted.
     * </p>
     * 
     * @param nw
     *            Node
     * @return Namespace
     */
    protected String extractNamespace(NodeWritable nw) {
        Node n = nw.get();
        if (n.isBlank() || n.isVariable())
            return NO_NAMESPACE;
        if (n.isLiteral()) {
            String dtUri = n.getLiteralDatatypeURI();
            if (dtUri == null)
                return NO_NAMESPACE;
            return extractNamespace(dtUri);
        }
        return extractNamespace(n.getURI());
    }

    /**
     * Extracts the namespace from a URI
     * <p>
     * First tries to extract a hash based namespace. If that is not possible it
     * tries to extract a slash based namespace, if this is not possible then
     * the full URI is returned.
     * </p>
     * <p>
     * Derived classes may override this to change the logic of how namespaces
     * are extracted.
     * </p>
     * 
     * @param uri
     *            URI
     * @return Namespace
     */
    protected String extractNamespace(String uri) {
        if (uri.contains("#")) {
            // Extract hash namespace
            return uri.substring(0, uri.lastIndexOf('#') + 1);
        } else if (uri.contains("/")) {
            // Ensure that this is not immediately after the scheme component or
            // at end of URI
            int index = uri.lastIndexOf('/');
            int schemeSepIndex = uri.indexOf(':');
            if (index - schemeSepIndex <= 2 || index == uri.length() - 1) {
                // Use full URI
                return uri;
            }

            // Otherwise safe to extract slash namespace
            return uri.substring(0, uri.lastIndexOf('/') + 1);
        } else {
            // Use full URI
            return uri;
        }
    }

    /**
     * Gets the nodes of the tuple whose namespaces are to be counted
     * 
     * @param tuple
     *            Tuple
     * @return Nodes
     */
    protected abstract NodeWritable[] getNodes(T tuple);
}
