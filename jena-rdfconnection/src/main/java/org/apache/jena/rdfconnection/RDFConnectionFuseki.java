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

package org.apache.jena.rdfconnection;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkFuseki;

/**
 * Implementation of the {@link RDFConnection} interface for connecting to an Apache Jena Fuseki.
 * <p>
 * This adds the ability to work with blank nodes across the network.
 */
public interface RDFConnectionFuseki extends RDFConnectionRemote {

    /**
     * Create a connection builder which is initialized for the default Fuseki
     * configuration. The application must call
     * {@link RDFConnectionRemoteBuilder#destination(String)} to set the URL of the remote
     * dataset.
     * @return RDFConnectionRemoteBuilder
     */
    public static RDFConnectionRemoteBuilder create() {
        return new RDFConnectionFusekiBuilder();
    }

    public static RDFConnectionRemoteBuilder newBuilder() {
        return new RDFConnectionFusekiBuilder();
    }

    /** Create a {@link RDFConnectionFuseki} for a remote destination. */
    public static RDFConnectionRemoteBuilder service(String destinationURL) {
        return newBuilder().destination(destinationURL);
    }

    static class RDFConnectionFusekiBuilder extends RDFConnectionRemoteBuilder {
        protected RDFConnectionFusekiBuilder() {
            super(RDFLinkFuseki.newBuilder());
        }

        @Override
        protected RDFLink buildLink() {
            return  builder.build();
        }

        @Override
        protected RDFConnection adaptLink(RDFLink rdfLink) {
            try {
                return new RDFConnectionFusekiImpl((RDFLinkFuseki)rdfLink);
            } catch (ClassCastException ex) {
                throw new InternalErrorException("Attempt to build a RDFConnectionFuseki from class "+rdfLink.getClass().getSimpleName());
            }
        }
    }

    static class RDFConnectionFusekiImpl extends RDFConnectionAdapter implements RDFConnectionFuseki {
        private RDFConnectionFusekiImpl(RDFLinkFuseki linkFuseki) {
            super(linkFuseki);
        }
    }
}

