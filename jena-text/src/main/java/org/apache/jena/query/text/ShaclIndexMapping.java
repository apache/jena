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

package org.apache.jena.query.text;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.lucene.analysis.Analyzer;

/**
 * Parsed representation of SHACL-like index shapes for entity-per-document indexing.
 * Pure data model — no RDF parsing or Lucene indexing logic.
 */
public class ShaclIndexMapping {

    public enum FieldType {
        TEXT, KEYWORD, INT, LONG, DOUBLE
    }

    public static class FieldDef {
        private final String fieldName;
        private final FieldType fieldType;
        private final Analyzer analyzer;
        private final boolean stored;
        private final boolean indexed;
        private final boolean facetable;
        private final boolean sortable;
        private final boolean multiValued;
        private final boolean defaultSearch;
        private final Set<Node> predicates;
        private final Path path;

        public FieldDef(String fieldName, FieldType fieldType, Analyzer analyzer,
                        boolean stored, boolean indexed, boolean facetable,
                        boolean sortable, boolean multiValued, boolean defaultSearch,
                        Set<Node> predicates) {
            this(fieldName, fieldType, analyzer, stored, indexed, facetable,
                 sortable, multiValued, defaultSearch, predicates, null);
        }

        public FieldDef(String fieldName, FieldType fieldType, Analyzer analyzer,
                        boolean stored, boolean indexed, boolean facetable,
                        boolean sortable, boolean multiValued, boolean defaultSearch,
                        Set<Node> predicates, Path path) {
            this.fieldName = Objects.requireNonNull(fieldName);
            this.fieldType = fieldType != null ? fieldType : FieldType.TEXT;
            this.analyzer = analyzer;
            this.stored = stored;
            this.indexed = indexed;
            this.facetable = facetable;
            this.sortable = sortable;
            this.multiValued = multiValued;
            this.defaultSearch = defaultSearch;
            this.predicates = predicates != null ? Collections.unmodifiableSet(new LinkedHashSet<>(predicates)) : Collections.emptySet();
            this.path = path;
        }

        public String getFieldName()       { return fieldName; }
        public FieldType getFieldType()     { return fieldType; }
        public Analyzer getAnalyzer()       { return analyzer; }
        public boolean isStored()           { return stored; }
        public boolean isIndexed()          { return indexed; }
        public boolean isFacetable()        { return facetable; }
        public boolean isSortable()         { return sortable; }
        public boolean isMultiValued()      { return multiValued; }
        public boolean isDefaultSearch()    { return defaultSearch; }
        public Set<Node> getPredicates()    { return predicates; }

        /** The structured path for this field. Null for simple predicate fields (backward compat). */
        public Path getPath()              { return path; }

        /** True if this field uses a complex path (sequence, inverse, or nested). */
        public boolean hasComplexPath() {
            return path != null && !(path instanceof P_Link);
        }

        @Override
        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    public static class IndexProfile {
        private final Node shapeNode;
        private final Set<Node> targetClasses;
        private final String docIdField;
        private final String discriminatorField;
        private final List<FieldDef> fields;

        public IndexProfile(Node shapeNode, Set<Node> targetClasses,
                            String docIdField, String discriminatorField,
                            List<FieldDef> fields) {
            this.shapeNode = shapeNode;
            this.targetClasses = targetClasses != null ? Collections.unmodifiableSet(new LinkedHashSet<>(targetClasses)) : Collections.emptySet();
            this.docIdField = docIdField != null ? docIdField : "uri";
            this.discriminatorField = discriminatorField != null ? discriminatorField : "docType";
            this.fields = fields != null ? Collections.unmodifiableList(new ArrayList<>(fields)) : Collections.emptyList();
        }

        public Node getShapeNode()          { return shapeNode; }
        public Set<Node> getTargetClasses() { return targetClasses; }
        public String getDocIdField()       { return docIdField; }
        public String getDiscriminatorField() { return discriminatorField; }
        public List<FieldDef> getFields()   { return fields; }

        @Override
        public String toString() {
            return "IndexProfile(" + shapeNode + " -> " + targetClasses + ", fields=" + fields + ")";
        }
    }

    /** A (profile, field) pair returned from predicate lookups. */
    public static class ProfileField {
        private final IndexProfile profile;
        private final FieldDef field;

        public ProfileField(IndexProfile profile, FieldDef field) {
            this.profile = profile;
            this.field = field;
        }

        public IndexProfile getProfile()    { return profile; }
        public FieldDef getField()          { return field; }
    }

    private final List<IndexProfile> profiles;
    private final Map<Node, List<ProfileField>> predicateLookup;
    private final Map<Node, List<IndexProfile>> classLookup;

    public ShaclIndexMapping(List<IndexProfile> profiles) {
        this.profiles = Collections.unmodifiableList(new ArrayList<>(profiles));

        // Build predicate → (profile, field) lookup
        Map<Node, List<ProfileField>> predMap = new HashMap<>();
        for (IndexProfile profile : profiles) {
            for (FieldDef field : profile.getFields()) {
                for (Node pred : field.getPredicates()) {
                    predMap.computeIfAbsent(pred, k -> new ArrayList<>())
                           .add(new ProfileField(profile, field));
                }
            }
        }
        this.predicateLookup = Collections.unmodifiableMap(predMap);

        // Build targetClass → profiles lookup
        Map<Node, List<IndexProfile>> clsMap = new HashMap<>();
        for (IndexProfile profile : profiles) {
            for (Node cls : profile.getTargetClasses()) {
                clsMap.computeIfAbsent(cls, k -> new ArrayList<>())
                      .add(profile);
            }
        }
        this.classLookup = Collections.unmodifiableMap(clsMap);
    }

    public List<IndexProfile> getProfiles() {
        return profiles;
    }

    public boolean isRelevantPredicate(Node p) {
        return predicateLookup.containsKey(p);
    }

    public List<ProfileField> getProfilesForPredicate(Node p) {
        return predicateLookup.getOrDefault(p, Collections.emptyList());
    }

    public List<IndexProfile> getProfilesForClass(Node cls) {
        return classLookup.getOrDefault(cls, Collections.emptyList());
    }

    /** Return all field names marked as facetable across all profiles. */
    public List<String> getFacetFieldNames() {
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (IndexProfile profile : profiles) {
            for (FieldDef field : profile.getFields()) {
                if (field.isFacetable() && seen.add(field.getFieldName())) {
                    result.add(field.getFieldName());
                }
            }
        }
        return result;
    }
}
