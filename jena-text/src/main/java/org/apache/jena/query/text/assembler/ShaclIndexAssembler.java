/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.text.assembler;

import java.util.*;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.graph.Node;
import org.apache.jena.query.text.*;
import org.apache.jena.query.text.ShaclIndexMapping.*;
import org.apache.jena.rdf.model.*;
import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses SHACL-like shape definitions from an RDF config into a {@link ShaclIndexMapping}.
 * <p>
 * Not an {@code AssemblerBase} subclass — called from {@link TextIndexLuceneAssembler}.
 * <p>
 * Uses standard Jena RDF API to read {@code sh:targetClass}, {@code sh:path},
 * {@code sh:alternativePath} — no jena-shacl dependency.
 */
public class ShaclIndexAssembler {
    private static final Logger log = LoggerFactory.getLogger(ShaclIndexAssembler.class);

    // SHACL namespace — we only read config, not run validation
    private static final String SH = "http://www.w3.org/ns/shacl#";
    private static final Property shTargetClass     = ResourceFactory.createProperty(SH, "targetClass");
    private static final Property shProperty        = ResourceFactory.createProperty(SH, "property");
    private static final Property shPath            = ResourceFactory.createProperty(SH, "path");
    private static final Property shAlternativePath = ResourceFactory.createProperty(SH, "alternativePath");

    private ShaclIndexAssembler() {}

    /**
     * Parse an RDF list of shape resources into a {@link ShaclIndexMapping}.
     *
     * @param a the assembler context (for resolving analyzer resources)
     * @param shapesList the RDF resource that is the head of the shapes list
     * @return parsed mapping
     */
    public static ShaclIndexMapping parseShapes(Assembler a, Resource shapesList) {
        List<IndexProfile> profiles = new ArrayList<>();

        RDFList rdfList = shapesList.as(RDFList.class);
        for (RDFNode item : rdfList.asJavaList()) {
            if (!item.isResource()) {
                throw new TextIndexException("text:shapes list item is not a resource: " + item);
            }
            profiles.add(parseProfile(a, item.asResource()));
        }

        if (profiles.isEmpty()) {
            throw new TextIndexException("text:shapes list is empty");
        }

        return new ShaclIndexMapping(profiles);
    }

    private static IndexProfile parseProfile(Assembler a, Resource shape) {
        Node shapeNode = shape.asNode();

        // sh:targetClass
        Set<Node> targetClasses = new LinkedHashSet<>();
        StmtIterator tcIter = shape.listProperties(shTargetClass);
        while (tcIter.hasNext()) {
            RDFNode tc = tcIter.next().getObject();
            if (tc.isResource()) {
                targetClasses.add(tc.asNode());
            }
        }
        if (targetClasses.isEmpty()) {
            throw new TextIndexException("Shape " + shape + " has no sh:targetClass");
        }

        // idx:docIdField (optional, default "uri")
        String docIdField = getOptionalString(shape, IndexVocab.pDocIdField);

        // idx:discriminatorField (optional, default "docType")
        String discriminatorField = getOptionalString(shape, IndexVocab.pDiscriminatorField);

        // Parse fields from idx:field list or sh:property
        List<FieldDef> fields = new ArrayList<>();

        // idx:field — an RDF list of field resources
        Statement fieldStmt = shape.getProperty(IndexVocab.pField);
        if (fieldStmt != null) {
            RDFNode fieldNode = fieldStmt.getObject();
            if (fieldNode.isResource()) {
                try {
                    RDFList fieldList = fieldNode.asResource().as(RDFList.class);
                    for (RDFNode fn : fieldList.asJavaList()) {
                        if (fn.isResource()) {
                            fields.add(parseFieldDef(a, fn.asResource()));
                        }
                    }
                } catch (Exception e) {
                    throw new TextIndexException("idx:field on " + shape + " is not a valid RDF list: " + e.getMessage());
                }
            }
        }

        // Also parse sh:property nodes (for shapes that use SHACL property shapes directly)
        StmtIterator propIter = shape.listProperties(shProperty);
        while (propIter.hasNext()) {
            Resource propShape = propIter.next().getObject().asResource();
            fields.add(parseFieldDef(a, propShape));
        }

        if (fields.isEmpty()) {
            throw new TextIndexException("Shape " + shape + " has no fields (idx:field or sh:property)");
        }

        return new IndexProfile(shapeNode, targetClasses, docIdField, discriminatorField, fields);
    }

    private static FieldDef parseFieldDef(Assembler a, Resource fieldRes) {
        // idx:fieldName (required)
        String fieldName = getRequiredString(fieldRes, IndexVocab.pFieldName,
            "Field " + fieldRes + " missing idx:fieldName");

        // idx:fieldType (optional, default TEXT)
        FieldType fieldType = FieldType.TEXT;
        Statement ftStmt = fieldRes.getProperty(IndexVocab.pFieldType);
        if (ftStmt != null) {
            fieldType = parseFieldType(ftStmt.getObject());
        }

        // idx:analyzer (optional)
        Analyzer analyzer = null;
        Statement analyzerStmt = fieldRes.getProperty(IndexVocab.pAnalyzer);
        if (analyzerStmt != null && analyzerStmt.getObject().isResource()) {
            analyzer = (Analyzer) a.open(analyzerStmt.getObject().asResource());
        }

        // Boolean flags with defaults
        boolean stored = getOptionalBoolean(fieldRes, IndexVocab.pStored, true);
        boolean indexed = getOptionalBoolean(fieldRes, IndexVocab.pIndexed, true);
        boolean facetable = getOptionalBoolean(fieldRes, IndexVocab.pFacetable, false);
        boolean sortable = getOptionalBoolean(fieldRes, IndexVocab.pSortable, false);
        boolean multiValued = getOptionalBoolean(fieldRes, IndexVocab.pMultiValued, false);
        boolean defaultSearch = getOptionalBoolean(fieldRes, IndexVocab.pDefaultSearch, false);

        // Predicates: from sh:path or idx:path
        Set<Node> predicates = extractPredicates(fieldRes);
        if (predicates.isEmpty()) {
            throw new TextIndexException("Field " + fieldRes + " (" + fieldName + ") has no path/predicate");
        }

        log.debug("Parsed field: {} type={} predicates={} facetable={}", fieldName, fieldType, predicates, facetable);
        return new FieldDef(fieldName, fieldType, analyzer, stored, indexed,
                           facetable, sortable, multiValued, defaultSearch, predicates);
    }

    /**
     * Extract predicate URIs from sh:path or idx:path on a field/property-shape resource.
     * <p>
     * Supports:
     * <ul>
     *   <li>{@code sh:path <uri>} — single predicate</li>
     *   <li>{@code sh:path [ sh:alternativePath (<uri1> <uri2> ...) ]} — set of predicates</li>
     *   <li>{@code idx:path <uri>} — single predicate (convenience)</li>
     * </ul>
     */
    static Set<Node> extractPredicates(Resource fieldRes) {
        Set<Node> predicates = new LinkedHashSet<>();

        // Try sh:path first
        Statement pathStmt = fieldRes.getProperty(shPath);
        if (pathStmt != null) {
            RDFNode pathNode = pathStmt.getObject();
            if (pathNode.isURIResource()) {
                // sh:path <uri> — direct predicate
                predicates.add(pathNode.asNode());
            } else if (pathNode.isResource()) {
                // sh:path [ sh:alternativePath (...) ]
                Statement altStmt = pathNode.asResource().getProperty(shAlternativePath);
                if (altStmt != null && altStmt.getObject().isResource()) {
                    RDFList altList = altStmt.getObject().asResource().as(RDFList.class);
                    for (RDFNode alt : altList.asJavaList()) {
                        if (alt.isURIResource()) {
                            predicates.add(alt.asNode());
                        }
                    }
                }
            }
        }

        // Also try idx:path as a convenience
        Statement idxPathStmt = fieldRes.getProperty(IndexVocab.pPath);
        if (idxPathStmt != null) {
            RDFNode pathNode = idxPathStmt.getObject();
            if (pathNode.isURIResource()) {
                predicates.add(pathNode.asNode());
            }
        }

        return predicates;
    }

    /**
     * Build an {@link EntityDefinition} from a {@link ShaclIndexMapping} for backward
     * compatibility with existing query methods ({@code text:query}, {@code text:facet}).
     */
    public static EntityDefinition deriveEntityDefinition(ShaclIndexMapping mapping) {
        IndexProfile firstProfile = mapping.getProfiles().get(0);
        String entityField = firstProfile.getDocIdField();

        // Find the first defaultSearch field
        String primaryField = null;
        for (IndexProfile profile : mapping.getProfiles()) {
            for (FieldDef field : profile.getFields()) {
                if (field.isDefaultSearch()) {
                    primaryField = field.getFieldName();
                    break;
                }
            }
            if (primaryField != null) break;
        }
        // Fallback: use first TEXT field
        if (primaryField == null) {
            for (FieldDef field : firstProfile.getFields()) {
                if (field.getFieldType() == FieldType.TEXT) {
                    primaryField = field.getFieldName();
                    break;
                }
            }
        }
        // Last resort: first field
        if (primaryField == null) {
            primaryField = firstProfile.getFields().get(0).getFieldName();
        }

        EntityDefinition defn = new EntityDefinition(entityField, primaryField);
        defn.setLangField("lang");
        defn.setUidField("uid");

        // Register all fields and their predicates
        for (IndexProfile profile : mapping.getProfiles()) {
            for (FieldDef field : profile.getFields()) {
                for (Node pred : field.getPredicates()) {
                    defn.set(field.getFieldName(), pred);
                }
                if (field.getAnalyzer() != null) {
                    defn.setAnalyzer(field.getFieldName(), field.getAnalyzer());
                }
            }
        }

        return defn;
    }

    private static FieldType parseFieldType(RDFNode node) {
        if (!node.isResource()) {
            throw new TextIndexException("idx:fieldType must be a resource, got: " + node);
        }
        String uri = node.asResource().getURI();
        if (IndexVocab.TextField.getURI().equals(uri)) return FieldType.TEXT;
        if (IndexVocab.KeywordField.getURI().equals(uri)) return FieldType.KEYWORD;
        if (IndexVocab.IntField.getURI().equals(uri)) return FieldType.INT;
        if (IndexVocab.LongField.getURI().equals(uri)) return FieldType.LONG;
        if (IndexVocab.DoubleField.getURI().equals(uri)) return FieldType.DOUBLE;
        throw new TextIndexException("Unknown idx:fieldType: " + uri);
    }

    private static String getRequiredString(Resource r, Property p, String errorMsg) {
        Statement s = r.getProperty(p);
        if (s == null) throw new TextIndexException(errorMsg);
        return s.getObject().asLiteral().getString();
    }

    private static String getOptionalString(Resource r, Property p) {
        Statement s = r.getProperty(p);
        if (s == null) return null;
        return s.getObject().asLiteral().getString();
    }

    private static boolean getOptionalBoolean(Resource r, Property p, boolean defaultValue) {
        Statement s = r.getProperty(p);
        if (s == null) return defaultValue;
        return s.getObject().asLiteral().getBoolean();
    }
}
