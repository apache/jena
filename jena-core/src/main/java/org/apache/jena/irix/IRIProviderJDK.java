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

package org.apache.jena.irix;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * {@link IRIProvider} implemented using {@code java.net.URI}.
 * <p>
 * <b>For information : do not use in production.</b>
 * <p>
 * Issues with java.net.URI:
 * <ul>
 * <li> It does not resolve correctly e.g. "http://host" and "path" =&gt; "http://hostpath" not "http://host/path"
 * <li> Does not resolve with "" correctly.
 * <li> Generates file:/path URIs
 * </ul>
 */
public class IRIProviderJDK implements IRIProvider {

    public IRIProviderJDK() {}

    private static <X> X exec(Supplier<X> action) {
        try {
            return action.get();
        } catch (Throwable ex) {
            throw new IRIException(ex.getMessage());
        }
    }

    /** {@link IRIx} implementation for the java.net.URI provider. */
    static class IRIxJDK extends IRIx {
        private final URI javaURI;

        private IRIxJDK(String iriStr, URI iri) {
            super(iri.toString());
            this.javaURI = iri;
        }

        @Override
        public boolean isAbsolute() {
            return javaURI.isAbsolute();
        }

        @Override
        public boolean isRelative() {
            return javaURI.getScheme() == null;
        }

        @Override
        public boolean isReference() {
            if ( javaURI.isOpaque() )
                return true;
            // isHierarchical.
            // There is always a path even if it's ""
            // Accept empty host for file:
            if ( javaURI.getScheme() == null )
                return false;
            return true;
        }

        @Override
        public boolean hasScheme(String scheme) {
            String iriScheme = scheme();
            if ( iriScheme == null )
                return false;
            return iriScheme.equalsIgnoreCase(scheme);
        }

        @Override
        public String scheme() {
            return javaURI.getScheme();
        }

        @Override
        public IRIx resolve(String other) {
            return exec(()->{
                URI iriOther = URI.create(other);
                URI iri2 = this.javaURI.resolve(iriOther);
                //exceptions(iri2);
                return new IRIxJDK(iri2.toString(), iri2);
            });
        }

        @Override
        public IRIx resolve(IRIx other) {
            return exec(()->{
                IRIxJDK iriOther = (IRIxJDK)other;
                URI iri2 = this.javaURI.resolve(iriOther.javaURI);
                //exceptions(iri2);
                return new IRIxJDK(iri2.toString(), iri2);
            });
        }

        @Override
        public IRIx normalize() {
            URI uri = javaURI.normalize();
            return new IRIxJDK(uri.toString(), uri);
        }

        @Override
        public IRIx relativize(IRIx other) {
            return exec(()->{
                IRIxJDK iriOther = (IRIxJDK)other;
                URI iri2 = this.javaURI.relativize(iriOther.javaURI);
                return new IRIxJDK(iri2.toString(), iri2);
            });
        }

        @Override
        public URI getImpl() { return javaURI; }

        @Override
        public boolean hasViolations() {
            return false;
        }

        @Override
        public void handleViolations(BiConsumer<Boolean, String> handler) {}

        @Override
        public int hashCode() {
            return Objects.hash(javaURI);
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            IRIxJDK other = (IRIxJDK)obj;
            return Objects.equals(javaURI, other.javaURI);
        }
    }

    @Override
    public IRIx create(String iri) throws IRIException {
        try {
            URI uri = new URI(iri);
            return new IRIxJDK(iri, uri);
        } catch (URISyntaxException ex) {
            throw new IRIException(ex.getMessage());
        }
    }

    @Override
    public void check(String iriStr) throws IRIException { create(iriStr); }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void strictMode(String scheme, boolean runStrict) {
        // No configuration options.
    }

    @Override
    public boolean isStrictMode(String scheme) { return false; }
}

