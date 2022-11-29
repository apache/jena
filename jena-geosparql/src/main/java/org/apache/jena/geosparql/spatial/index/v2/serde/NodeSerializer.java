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
package org.apache.jena.geosparql.spatial.index.v2.serde;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A RDF1.2 node serializer using a custom format for intermediate serialization.
 */
public class NodeSerializer
    extends Serializer<Node>
{
    protected static final int TYPE_MASK         = 0x70; // 0111 0000
    protected static final int TYPE_IRI          = 0x10; // 0001 0000
    protected static final int TYPE_BNODE        = 0x20; // 0010 0000
    protected static final int TYPE_LITERAL      = 0x30; // 0011 0000
    protected static final int TYPE_VAR          = 0x40; // 0100 0000
    protected static final int TYPE_TRIPLE       = 0x50; // 0101 0000

    protected static final int LITERAL_MASK      = 0x03; // 0000 0011
    protected static final int LITERAL_STRING    = 0x00; // 0000 0000
    protected static final int LITERAL_LANG      = 0x01; // 0000 0001
    protected static final int LITERAL_DTYPE     = 0x02; // 0000 0010

    // Iff (TYPE_LITERAL and LITERAL_LANG) the following applies:
    protected static final int LITERAL_LANG_MASK = 0x0c; // 0000 1100
    protected static final int LITERAL_LANG_LTR  = 0x04; // 0000 0100
    protected static final int LITERAL_LANG_RTL  = 0x08; // 0000 1000

    // Iff (TYPE_IRI) or (TYPE_LITERAL & LITERAL_DYTE): Whether the value field is abbreviated.
    protected static final int ABBREV_IRI        = 0x08; // 0000 1000

    private static Map<String, String> globalPrefixToIri = new HashMap<>();

    private Map<String, String> prefixToIri = new HashMap<>();
    private Map<String, String> iriToPrefix = new HashMap<>();

    static {
        globalPrefixToIri.put("a", RDF.type.getURI());
        globalPrefixToIri.put("d", Quad.defaultGraphIRI.getURI());
        globalPrefixToIri.put("g", Quad.defaultGraphNodeGenerated.getURI());
        globalPrefixToIri.put("x", XSD.NS);
        globalPrefixToIri.put("r", RDF.uri);
        globalPrefixToIri.put("s", RDFS.uri);
        globalPrefixToIri.put("o", OWL.NS);
    }

    /**
     * Takes a guess for the namespace URI string to use in abbreviation.
     * Finds the part of the IRI string before the last '#' or '/'.
     *
     * @param iriString String string
     * @return String or null
     */
    private static String getPossibleKey(String iriString) {
        int n = iriString.length();
        int i;
        outer: for (i = n - 1; i >= 0; --i) {
            char c = iriString.charAt(i);
            switch(c) {
            case '#':
            case '/':
            case ':':
            case '.':
                // We could add ':' here, it is used as a separator in URNs.
                // But it is a multiple use character and always present in the scheme name.
                // This is a fast-track guess so don't try guessing based on ':'.
                break outer;
            default:
                // break;
            }
        }
        String result = i >= 0 ? iriString.substring(0, i + 1) : null;
        return result;
    }

    private static String encode(Map<String, String> iriToPrefix, String iri) {
        String result = iriToPrefix.get(iri);
        if (result == null) {
            String key = getPossibleKey(iri);
            if (key != null) {
                String prefix = iriToPrefix.get(key);
                if (prefix != null) {
                    result = prefix + ":" + iri.substring(key.length());
                }
            }
        }
        return result;
    }

    private static String decode(Map<String, String> prefixToIri, String curie) {
        String result;
        int idx = curie.indexOf(':');
        if (idx < 0) {
            result = prefixToIri.get(curie);
        } else {
            String prefix = curie.substring(0, idx);
            String iri = prefixToIri.get(prefix);
            result = iri + curie.substring(idx + 1);
        }
        return result;
    }

    protected TypeMapper typeMapper;

    public NodeSerializer() {
        this(TypeMapper.getInstance());
    }

    public NodeSerializer(TypeMapper typeMapper) {
        this(typeMapper, globalPrefixToIri);
    }

    public NodeSerializer(TypeMapper typeMapper, Map<String, String> prefixToIri) {
        super();
        this.typeMapper = typeMapper;
        this.prefixToIri = new HashMap<>(prefixToIri);

        this.iriToPrefix = prefixToIri.entrySet().stream()
                .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    }

    @Override
    public void write(Kryo kryo, Output output, Node node) {
        if (node.isURI()) {
            String uri = node.getURI();
            String curie = encode(iriToPrefix, uri);
            if (curie != null) {
                output.writeByte(TYPE_IRI | ABBREV_IRI);
                output.writeString(curie);
            } else {
                output.writeByte(TYPE_IRI);
                output.writeString(uri);
            }
        } else if (node.isLiteral()) {
            String lex = node.getLiteralLexicalForm();
            String lang = node.getLiteralLanguage();
            String dt = node.getLiteralDatatypeURI();
            TextDirection dir = node.getLiteralTextDirection();

            if (lang != null && !lang.isEmpty()) {
                byte langDirBits = switch(dir) {
                    case LTR -> LITERAL_LANG_LTR;
                    case RTL -> LITERAL_LANG_RTL;
                    default -> (byte)0;
                };
                output.writeByte(TYPE_LITERAL | LITERAL_LANG | langDirBits);
                output.writeString(lex);
                output.writeString(lang);
            } else if (dt != null && !dt.isEmpty() && !dt.equals(XSD.xstring.getURI())) {
                String dtCurie = encode(iriToPrefix, dt);
                if (dtCurie != null) {
                    output.writeByte(TYPE_LITERAL | LITERAL_DTYPE | ABBREV_IRI);
                    output.writeString(lex);
                    output.writeString(dtCurie);
                } else {
                    output.writeByte(TYPE_LITERAL | LITERAL_DTYPE);
                    output.writeString(lex);
                    output.writeString(dt);
                }
            } else {
                output.writeByte(TYPE_LITERAL);
                output.writeString(lex);
            }
        } else if (node.isBlank()) {
            output.writeByte(TYPE_BNODE);
            output.writeString(node.getBlankNodeLabel());
        } else if (node.isVariable()) {
            output.writeByte(TYPE_VAR);
            output.writeString(node.getName());
        } else if (node.isNodeTriple()) {
            output.writeByte(TYPE_TRIPLE);
            kryo.writeObject(output, node.getTriple());
        } else {
            throw new RuntimeException("Unknown node type: " + node);
        }
    }

    @Override
    public Node read(Kryo kryo, Input input, Class<Node> cls) {
        Node result;
        String v1, v2;
        Triple t;

        byte type = input.readByte();

        int typeVal = type & TYPE_MASK;
        switch (typeVal) {
            case TYPE_IRI:
                v1 = input.readString();
                if ((type & ABBREV_IRI) != 0) {
                    v1 = decode(prefixToIri, v1);
                }
                result = NodeFactory.createURI(v1);
                break;
            case TYPE_LITERAL:
                int subTypeVal = type & LITERAL_MASK;
                switch (subTypeVal) {
                    case LITERAL_STRING:
                        v1 = input.readString();
                        result = NodeFactory.createLiteralString(v1);
                        break;
                    case LITERAL_LANG:
                        int langDirBits = type & LITERAL_LANG_MASK;

                        TextDirection textDir = switch(langDirBits) {
                            case LITERAL_LANG_LTR -> TextDirection.LTR;
                            case LITERAL_LANG_RTL -> TextDirection.RTL;
                            default -> Node.noTextDirection;
                        };

                        v1 = input.readString();
                        v2 = input.readString();
                        result = NodeFactory.createLiteralDirLang(v1, v2, textDir);
                        break;
                    case LITERAL_DTYPE:
                        v1 = input.readString();
                        v2 = input.readString();
                        if ((type & ABBREV_IRI) != 0) {
                            v2 = decode(prefixToIri, v2);
                        }
                        RDFDatatype dtype = typeMapper.getSafeTypeByName(v2);
                        result = NodeFactory.createLiteralDT(v1, dtype);
                        break;
                    default:
                        throw new RuntimeException("Unknown literal sub-type: " + subTypeVal);
                }
                break;
            case TYPE_BNODE:
                v1 = input.readString();
                result = NodeFactory.createBlankNode(v1);
                break;
            case TYPE_VAR:
                v1 = input.readString();
                result = Var.alloc(v1); // NodeFactory.createVariable ?
                break;
            case TYPE_TRIPLE:
                t = kryo.readObject(input, Triple.class);
                result = NodeFactory.createTripleNode(t);
                break;
            default:
                throw new RuntimeException("Unknown node type: " + typeVal);
        }
        return result;
    }
}
