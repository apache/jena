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

package org.apache.jena.rdfpatch.system;

import static org.apache.jena.rdfpatch.system.URNs.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.impl.Util;

// XXX Should use this! At the moemnt a patch id is a Node.
/**
 * An identifier. When encoded, the {@code Id} can be a URI or string literal.
 * The preferred form is a {@code <uuid:...>}.
 * <ul>
 * <li>{@code <uuid:...>}
 * <li>{@code <urn:uuid:...>}
 * <li>{@code "id:..."}
 * <li>{@code "..."}
 * </ul>
 * where {@code ...} is a UUID as a string.
 * <p>
 * If anything else is encountered, the lexical form or URI string is used.
 */
public final class Id {
    private static final String SCHEME = "id:";
    // All zeros : https://tools.ietf.org/html/rfc4122#page-9
    private static final String nilStr = "00000000-0000-0000-0000-000000000000";
    private static final Id nilId = Id.fromUUID(UUID.fromString(nilStr));

    /** Length in chars of a UUID string, without any scheme info */
    public static int lenStrUUID() { return nilStr.length(); }

    /** Quick test of whether a string looks like an UUID or not */
    public static boolean maybeUUID(String str) {
        return str.length() == nilStr.length() && str.charAt(8)=='-';
    }

    public static Id nullId() { return nilId; }

    /** Create a fresh {@code Id}, based on a UUID. */
    public static Id create() {
        return new Id(genUUID()) ;
    }

    /** Create a {@code Id}, according to byte.
     * @see #asBytes
     */
    public static Id fromBytes(byte[] bytes) {
        if ( bytes.length == 2*Long.BYTES ) {
            long mostSig = Bytes.getLong(bytes, 0);
            long leastSig = Bytes.getLong(bytes, Long.BYTES);
            UUID uuid = new UUID(mostSig, leastSig);
            return fromUUID(uuid);
        }
        String str = new String(bytes, StandardCharsets.UTF_8);
        return new Id(str);
    }

    /** Convenience operation to make a displayable string from a Node, that has been used for an Id. */
    public static String str(Node node) {
        if ( node == null )
            return "<null>";
        return fromNode(node).toString();
    }

    /**
     * Convert a {@link Node} to an {@code Id}. The {@link Node} can be a URI or
     * a string literal. The preferred form is a {@code <uuid:...>}.
     * <p>
     * An argument of {@code null} returns {@code null}.
     *
     * @param node
     * @return Id
     */
    public static Id fromNode(Node node) {
        if ( node == null )
            return null ;

        String s = null ;

        if ( node.isURI() )
            s = node.getURI() ;
        else if ( Util.isSimpleString(node) )
            s = node.getLiteralLexicalForm() ;
        if ( s == null )
            throw new IllegalArgumentException("Id input is not a URI or a string") ;
        return fromString$(s) ;
    }

    public static Id fromUUID(UUID uuid) { return new Id(uuid) ; }

    private static Id fromString$(String str) {
        if ( str.startsWith(SchemeUuid) )
            str = str.substring(SchemeUuid.length()) ;
        else if ( str.startsWith(SchemeUrnUuid) )
            str = str.substring(SchemeUrnUuid.length()) ;
        return fromString(str) ;
    }

    public static Id fromStringOrNull(String str) {
        return ( str == null ) ? null : fromString(str);
    }

    public static Id fromString(String str) {
        Objects.requireNonNull(str);
        switch(str) {
            case nilStr:
            case "id:nil":
                return nullId();
        }
        if ( str.startsWith(SCHEME) )
            str = str.substring(SCHEME.length());
        try {
            UUID uuid = UUID.fromString(str) ;
            return new Id(uuid) ;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("String for id does not match a UUID: '"+str+"'");
            //return new Id(str) ;
        }
    }

    /** Parse a UUID string, return a default if it does not parse correctly */
    public static UUID parseUUID(String patchStr, UUID dft) {
        try {
            return UUID.fromString(patchStr);
        } catch (IllegalArgumentException ex) {
            return dft;
        }
    }

    /**
     * Create an Id from a UUID string, return a default if the UUID string
     * does not parse correctly.
     */
    public static Id parseId(String uuidStr, Id dft) {
        try {
            return Id.fromUUID(UUID.fromString(uuidStr));
        } catch (IllegalArgumentException ex) {
            return dft;
        }
    }

    private final UUID uuid ;
    private final String string ;

    private Id(UUID id) {
        uuid = Objects.requireNonNull(id) ;
        string = null ;
    }

    private Id(String id) {
        uuid = null ;
        string = Objects.requireNonNull(id) ;
    }

    public boolean isNil() {
        return this.equals(nilId);
    }

    /** Suitable for putting into an HTTP request query string. */
    public String asParam() {
        if ( uuid != null )
            return uuid.toString() ;
        return string ;
    }

    /**
     * Encode as bytes (network order, 16 byte number).
     * @see #fromBytes(byte[])
     */
    public byte[] asBytes() {
        if ( uuid != null ) {
            byte[] bytes = new byte[2*Long.BYTES];
            // As a 16 byte number, network order - most significant byte in byte[0].
            Bytes.setLong(uuid.getMostSignificantBits(), bytes, 0);
            Bytes.setLong(uuid.getLeastSignificantBits(), bytes, Long.BYTES);
            return bytes;
        }
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /** Without any adornment */
    public String asPlainString() {
        if ( uuid != null )
            return uuid.toString() ;
        if ( string != null )
            return string ;
        throw new InternalErrorException("Id has null UUID and string");
    }

    /** With "schema" */
    public String asString() {
        if ( uuid != null )
            return SCHEME+uuid.toString() ;
        if ( string != null )
            return string ;
        throw new InternalErrorException("Id has null UUID and null string");
    }

    /** Convert to a Node, as a URI or as a plain string. */
    public Node asNode() {
        if ( uuid != null )
            return NodeFactory.createURI(SchemeUuid+uuid.toString());
        return NodeFactory.createLiteral(string);
    }

    @Override
    public String toString() {
        return toSchemeString(SCHEME);
    }

    /** For labelling with the type of thing id'ed. eg  "ds:abcdef" */
    public String toSchemeString(String scheme) {
        if ( this == nilId  )
            return "id:nil";
        if ( uuid != null )
            return scheme+shortUUIDstr(uuid);
        return scheme+"\""+string+"\"" ;
    }

    public static String shortUUIDstr(UUID uuid) {
        String str = uuid.toString();
        int version = uuid.version();
        if ( version == 1 )
            // Type 1 : include varying part! xxxx-yyyy
            // 0-6 is the low end of the clock.
            return uuid.toString().substring(0,6);
        if ( version == 4 )
            // Type 4 - use the first few hex characters.
            return uuid.toString().substring(0,6);
        return uuid.toString().substring(0,8);
    }

    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((string == null) ? 0 : string.hashCode()) ;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( getClass() != obj.getClass() )
            return false ;
        Id other = (Id)obj ;
        if ( string == null ) {
            if ( other.string != null )
                return false ;
        } else if ( !string.equals(other.string) )
            return false ;
        if ( uuid == null ) {
            if ( other.uuid != null )
                return false ;
        } else if ( !uuid.equals(other.uuid) )
            return false ;
        return true ;
    }
}
