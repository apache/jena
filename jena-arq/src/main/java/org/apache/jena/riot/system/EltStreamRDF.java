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

package org.apache.jena.riot.system;

import java.io.Serializable;
import java.util.Objects;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Quad;

/** An item of a StreamRDF, including exceptions. */
public class EltStreamRDF
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final EltStreamRDFType type;
    private final Triple triple;
    private final Quad quad;
    private final String prefix; // Null implies "base".
    private final String iri;
    private final Throwable exception;

    /* Prefer static constructors in EltStreamRDF */

    EltStreamRDF(Triple triple) { this(EltStreamRDFType.TRIPLE, triple, null, null, null, null); }
    EltStreamRDF(Quad quad) { this(EltStreamRDFType.QUAD, null, quad, null, null, null); }
    EltStreamRDF(String prefix, String iri) { this(EltStreamRDFType.PREFIX, null, null, prefix, iri, null); }
    EltStreamRDF(String iri) { this(EltStreamRDFType.BASE, null, null, null, iri, null); }
    EltStreamRDF(Throwable exception) { this(EltStreamRDFType.EXCEPTION, null, null, null, null, exception); }

    EltStreamRDF(EltStreamRDFType eltType, Triple triple, Quad quad, String prefix, String iri, Throwable exception) {
        this.type = eltType;
        this.triple = triple;
        this.quad = quad;
        this.prefix = prefix;
        this.iri = iri;
        this.exception = exception;
    }

    public boolean   isTriple()    { return EltStreamRDFType.TRIPLE.equals(type); }
    public Triple    triple()      { return triple; }
    public boolean   isQuad()      { return EltStreamRDFType.QUAD.equals(type); }
    public Quad      quad()        { return quad; }
    public boolean   isPrefix()    { return EltStreamRDFType.PREFIX.equals(type); }
    public String    prefix()      { return prefix; }
    public boolean   isBase()      { return EltStreamRDFType.BASE.equals(type); }
    public String    iri()         { return iri; }
    public boolean   isException() { return EltStreamRDFType.EXCEPTION.equals(type); }
    public Throwable exception()   { return exception; }

    public EltStreamRDFType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, triple, quad, prefix, iri, exception);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EltStreamRDF other = (EltStreamRDF) obj;
        return Objects.equals(type, other.type) && Objects.equals(triple, other.triple) &&
                Objects.equals(quad, other.quad) && Objects.equals(prefix, other.prefix) &&
                Objects.equals(iri, other.iri) && Objects.equals(exception, other.exception);
    }

    @Override
    public String toString() {
        String str;
        switch (getType()) {
        case TRIPLE: str = "triple(" + NodeFmtLib.str(triple()) + ")"; break;
        case QUAD: str = "quad(" + NodeFmtLib.str(quad()) + ")"; break;
        case PREFIX: str = "prefix(" + Objects.toString(prefix() + ", " + iri()) + ")"; break;
        case BASE: str = "base(" + Objects.toString(iri()) + ")"; break;
        case EXCEPTION: str = "exception(" + Objects.toString(exception()) + ")"; break;
        default: str = "unknown"; break;
        }
        return str;
    }

    public static EltStreamRDF triple(Triple triple) { return new EltStreamRDF(Objects.requireNonNull(triple)); }
    public static EltStreamRDF quad(Quad quad) { return new EltStreamRDF(Objects.requireNonNull(quad)); }
    public static EltStreamRDF base(String iri) { return new EltStreamRDF(Objects.requireNonNull(iri)); }
    public static EltStreamRDF prefix(String prefix, String iri) { return new EltStreamRDF(Objects.requireNonNull(prefix), Objects.requireNonNull(iri)); }
    public static EltStreamRDF exception(Throwable exception) { return new EltStreamRDF(Objects.requireNonNull(exception)); }
}
