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

import java.util.Objects;

/**
 * Non-resolving provider that accepts anything string for &lt;...&gt;.
 * <p>
 * <i>Caveat emptor</i>.
 */
public class IRIProviderAny  implements IRIProvider {

    /** The IRIProvider builder does not create this kind of IRIProvider! */
    public static IRIProviderAny stringProvider() {return new IRIProviderAny(); }

    public IRIProviderAny() {}

    static class IRIxString extends IRIx {

        protected IRIxString(String string) {
            super(string);
        }

        // Meaningless.
        @Override public boolean isAbsolute()               { return true; }
        @Override public boolean isRelative()               { return false; }
        @Override public boolean hasScheme(String scheme)   { return str().startsWith(scheme); }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public IRIx resolve(String other) {
            return new IRIxString(other);
        }

        @Override
        public IRIx resolve(IRIx other) {
            return new IRIxString(other.str());
        }

        @Override
        public IRIx normalize() {
            return this;
        }

        @Override
        public IRIx relativize(IRIx other) {
            return null;
        }
        @Override
        public Object getImpl() {
            return str();
        }

        @Override
        public int hashCode() {
            return 29 + super.str().hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            IRIxString other = (IRIxString)obj;
            return Objects.equals(str(), other.str());
        }
    }

    @Override
    public IRIx create(String iri) {
        return new IRIxString(iri);
    }

    @Override
    public void check(String iriStr) throws IRIException { }

    @Override
    public void strictMode(String scheme, boolean runStrict) {}

    @Override
    public boolean isStrictMode(String scheme) { return false; }
}
