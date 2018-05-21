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

import org.apache.jena.graph.Node;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

public class SerializerRDF {
    
    public static void init() {
        org.apache.jena.sys.Serializer.setNodeSerializer(SNode::new);
        org.apache.jena.sys.Serializer.setTripleSerializer(STriple::new);
        org.apache.jena.riot.system.Serializer.setQuadSerializer(SQuad::new);
    }

    /* <PRE>
    * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
    * </PRE><p>
    *
    * This writeReplace method is invoked by serialization if the method
    * exists and it would be accessible from a method defined within the
    * class of the object being serialized. Thus, the method can have private,
    * protected and package-private access. Subclass access to this method
    * follows java accessibility rules. <p>
    *
    * Classes that need to designate a replacement when an instance of it
    * is read from the stream should implement this special method with the
    * exact signature.
    *
    * <PRE>
    * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
    * </PRE><p>
    */
    
    static Node read(TProtocol protocol, RDF_Term tterm) {
        tterm.clear();
        try { tterm.read(protocol); }
        catch (TException e) { TRDF.exception(e); }
        return ThriftConvert.convert(tterm);
    }
    
    // For now - no prefix map, no value encoding.  
    // The benefit unclear, sometimes even a bit slower (few percent).
    private static final PrefixMap pmap = null;
    private static final boolean encodeValues = false;
    
    static void write(TProtocol protocol, RDF_Term tterm, Node node) {
        tterm.clear();
        ThriftConvert.toThrift(node, pmap, tterm, encodeValues);
        try { tterm.write(protocol); }
        catch (TException e) { TRDF.exception(e); }
    }
}
