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

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.sparql.core.Quad;
import org.apache.thrift.protocol.TProtocol;

/** Serialization of a {@link Quad} using Thrift for the serialization. */
public final class SQuad implements Serializable {
    private static final long serialVersionUID = 0x993530716d72ff06L;
    private transient Quad quad;

    public SQuad(Quad quad)     { this.quad = quad; }
    public Quad getQuad()       { return quad; }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        TProtocol protocol = TRDF.protocol(out);
        RDF_Term tterm  = new RDF_Term();
        SerializerRDF.write(protocol, tterm, quad.getGraph());
        SerializerRDF.write(protocol, tterm, quad.getSubject());
        SerializerRDF.write(protocol, tterm, quad.getPredicate());
        SerializerRDF.write(protocol, tterm, quad.getObject());
        TRDF.flush(protocol);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException {
        TProtocol protocol = TRDF.protocol(in);
        RDF_Term tterm  = new RDF_Term();
        Node g = SerializerRDF.read(protocol, tterm);
        Node s = SerializerRDF.read(protocol, tterm);
        Node p = SerializerRDF.read(protocol, tterm);
        Node o = SerializerRDF.read(protocol, tterm);
        quad = Quad.create(g, s, p, o);
    }

    Object readResolve() throws ObjectStreamException
    { return quad; }
}
