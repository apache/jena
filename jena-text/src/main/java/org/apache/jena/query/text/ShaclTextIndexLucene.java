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

import java.io.IOException;
import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.query.text.cql.CqlExpression;
import org.apache.jena.query.text.cql.CqlToLuceneCompiler;
import org.apache.jena.rdf.model.Resource;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SHACL entity-per-document Lucene index.
 * <p>
 * Extends {@link TextIndexLucene} with faceting, CQL filtering, sort pushdown,
 * and SHACL-driven document building. This class holds all SHACL-specific
 * state and methods so that the parent remains a clean classic triple-per-document
 * implementation.
 */
public class ShaclTextIndexLucene extends TextIndexLucene {
    private static final Logger log = LoggerFactory.getLogger(ShaclTextIndexLucene.class);

    private final ShaclIndexMapping shaclMapping;
    private final List<String> facetFields;
    private final FacetsConfig facetsConfig;
    private final int maxFacetHits;

    public ShaclTextIndexLucene(Directory directory, TextIndexConfig config) {
        super(directory, config);
        this.shaclMapping = config.getShaclMapping();
        this.maxFacetHits = config.getMaxFacetHits();

        this.facetFields = new ArrayList<>(config.getFacetFields());
        this.facetsConfig = new FacetsConfig();
        for (String facetField : this.facetFields) {
            facetsConfig.setMultiValued(facetField, true);
        }
        for (ShaclIndexMapping.IndexProfile profile : this.shaclMapping.getProfiles()) {
            for (ShaclIndexMapping.FieldDef field : profile.getFields()) {
                if (field.isFacetable() && field.isMultiValued()) {
                    facetsConfig.setMultiValued(field.getFieldName(), true);
                }
            }
        }
        if (!this.facetFields.isEmpty()) {
            log.info("Faceting enabled for fields: {}", this.facetFields);
        }
    }

    public ShaclIndexMapping getShaclMapping() {
        return shaclMapping;
    }

    public boolean isShaclMode() {
        return true;
    }

    // ---- Document building ----

    protected Document docFromMapping(Entity entity, ShaclIndexMapping.IndexProfile profile) {
        Document doc = new Document();

        String docIdField = profile.getDocIdField();
        doc.add(new Field(docIdField, entity.getId(), ftIRI));

        String discriminatorField = profile.getDiscriminatorField();
        if (discriminatorField != null && !profile.getTargetClasses().isEmpty()) {
            Node firstClass = profile.getTargetClasses().iterator().next();
            String localName = firstClass.getLocalName();
            if (localName != null && !localName.isEmpty()) {
                doc.add(new StringField(discriminatorField, localName, Field.Store.YES));
            }
        }

        for (ShaclIndexMapping.FieldDef fieldDef : profile.getFields()) {
            Object value = entity.get(fieldDef.getFieldName());
            if (value == null) continue;

            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> values = (List<Object>) value;
                for (Object v : values) {
                    addFieldToDoc(doc, fieldDef, v);
                }
            } else {
                addFieldToDoc(doc, fieldDef, value);
            }
        }

        return doc;
    }

    private void addFieldToDoc(Document doc, ShaclIndexMapping.FieldDef fieldDef, Object value) {
        String fieldName = fieldDef.getFieldName();
        Field.Store store =
            fieldDef.isStored() ? Field.Store.YES : Field.Store.NO;

        switch (fieldDef.getFieldType()) {
            case TEXT:
                if (fieldDef.isIndexed()) {
                    FieldType ft = fieldDef.isStored() ? TextField.TYPE_STORED : TextField.TYPE_NOT_STORED;
                    doc.add(new Field(fieldName, value.toString(), ft));
                } else if (fieldDef.isStored()) {
                    doc.add(new StoredField(fieldName, value.toString()));
                }
                break;

            case KEYWORD:
                String strVal = value.toString();
                if (fieldDef.isIndexed()) {
                    doc.add(new StringField(fieldName, strVal, store));
                } else if (fieldDef.isStored()) {
                    doc.add(new StoredField(fieldName, strVal));
                }
                if (fieldDef.isFacetable() && strVal != null && !strVal.isEmpty()) {
                    doc.add(new SortedSetDocValuesFacetField(fieldName, strVal));
                }
                if (fieldDef.isSortable()) {
                    doc.add(new SortedDocValuesField(fieldName, new BytesRef(strVal)));
                }
                break;

            case INT: {
                int intVal = (value instanceof Number) ? ((Number) value).intValue() : Integer.parseInt(value.toString());
                if (fieldDef.isIndexed()) {
                    doc.add(new IntPoint(fieldName, intVal));
                }
                if (fieldDef.isStored()) {
                    doc.add(new StoredField(fieldName, intVal));
                }
                if (fieldDef.isSortable()) {
                    doc.add(new NumericDocValuesField(fieldName, intVal));
                }
                break;
            }

            case LONG: {
                long longVal = (value instanceof Number) ? ((Number) value).longValue() : Long.parseLong(value.toString());
                if (fieldDef.isIndexed()) {
                    doc.add(new LongPoint(fieldName, longVal));
                }
                if (fieldDef.isStored()) {
                    doc.add(new StoredField(fieldName, longVal));
                }
                if (fieldDef.isSortable()) {
                    doc.add(new NumericDocValuesField(fieldName, longVal));
                }
                break;
            }

            case DOUBLE: {
                double dblVal = (value instanceof Number) ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
                if (fieldDef.isIndexed()) {
                    doc.add(new DoublePoint(fieldName, dblVal));
                }
                if (fieldDef.isStored()) {
                    doc.add(new StoredField(fieldName, dblVal));
                }
                if (fieldDef.isSortable()) {
                    doc.add(new NumericDocValuesField(fieldName, Double.doubleToRawLongBits(dblVal)));
                }
                break;
            }
        }
    }

    // ---- Entity update/delete ----

    public void updateEntityForProfile(Entity entity, ShaclIndexMapping.IndexProfile profile) {
        try {
            Document doc = docFromMapping(entity, profile);
            Document indexDoc = facetFields.isEmpty() ? doc : facetsConfig.build(doc);

            String docIdField = profile.getDocIdField();
            String discriminatorField = profile.getDiscriminatorField();
            Node firstClass = profile.getTargetClasses().iterator().next();
            String localName = firstClass.getLocalName();

            BooleanQuery deleteQuery = new BooleanQuery.Builder()
                .add(new TermQuery(new Term(docIdField, entity.getId())), BooleanClause.Occur.MUST)
                .add(new TermQuery(new Term(discriminatorField, localName)), BooleanClause.Occur.MUST)
                .build();

            getIndexWriter().deleteDocuments(deleteQuery);
            getIndexWriter().addDocument(indexDoc);
            log.trace("updateEntityForProfile: {} profile={}", entity.getId(), profile.getShapeNode());
        } catch (IOException e) {
            throw new TextIndexException("updateEntityForProfile", e);
        }
    }

    public void deleteEntityByUri(String entityUri) {
        try {
            Set<String> docIdFields = new HashSet<>();
            for (ShaclIndexMapping.IndexProfile profile : shaclMapping.getProfiles()) {
                docIdFields.add(profile.getDocIdField());
            }
            if (docIdFields.isEmpty()) {
                docIdFields.add(getDocDef().getEntityField());
            }
            for (String field : docIdFields) {
                getIndexWriter().deleteDocuments(new Term(field, entityUri));
            }
            log.trace("deleteEntityByUri: {}", entityUri);
        } catch (IOException e) {
            throw new TextIndexException("deleteEntityByUri", e);
        }
    }

    // ---- Faceting ----

    public boolean isFacetingEnabled() {
        return !facetFields.isEmpty();
    }

    public List<String> getFacetFields() {
        return Collections.unmodifiableList(facetFields);
    }

    private int facetSearchLimit() {
        return maxFacetHits > 0 ? maxFacetHits : Integer.MAX_VALUE;
    }

    public Map<String, List<FacetValue>> getFacetCounts(List<String> facetFieldsToQuery, int maxValues) {
        return getFacetCounts(null, facetFieldsToQuery, maxValues);
    }

    public Map<String, List<FacetValue>> getFacetCounts(String queryString, List<String> facetFieldsToQuery, int maxValues) {
        return getFacetCounts(queryString, facetFieldsToQuery, maxValues, 0);
    }

    public Map<String, List<FacetValue>> getFacetCounts(String queryString, List<String> facetFieldsToQuery, int maxValues, int minCount) {
        Map<String, List<FacetValue>> result = new HashMap<>();

        if (facetFieldsToQuery == null || facetFieldsToQuery.isEmpty()) {
            return result;
        }

        try (IndexReader indexReader = DirectoryReader.open(getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(indexReader, facetsConfig);

            Facets facets;
            if (queryString == null || queryString.isEmpty()) {
                facets = new SortedSetDocValuesFacetCounts(state);
            } else {
                Query query = parseQuery(queryString, getQueryAnalyzer());
                FacetsCollector fc = new FacetsCollector();
                searcher.search(query, fc);
                facets = new SortedSetDocValuesFacetCounts(state, fc);
            }

            for (String field : facetFieldsToQuery) {
                List<FacetValue> fieldFacets = new ArrayList<>();
                try {
                    FacetResult facetResult = (maxValues <= 0)
                        ? facets.getAllChildren(field)
                        : facets.getTopChildren(maxValues, field);
                    if (facetResult != null && facetResult.labelValues != null) {
                        for (LabelAndValue lv : facetResult.labelValues) {
                            if (minCount <= 0 || lv.value.longValue() >= minCount) {
                                fieldFacets.add(new FacetValue(lv.label, lv.value.longValue()));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.debug("No facet data for field '{}': {}", field, e.getMessage());
                }
                result.put(field, fieldFacets);
            }
        } catch (IOException ex) {
            throw new TextIndexException("getFacetCounts", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(queryString, ex.getMessage());
        }

        return result;
    }

    public Map<String, List<FacetValue>> getFacetCountsWithFilters(
            String queryString, List<String> facetFieldsToQuery,
            Map<String, List<String>> filters, int maxValues) {
        return getFacetCountsWithFilters(queryString, facetFieldsToQuery, filters, maxValues, 0);
    }

    public Map<String, List<FacetValue>> getFacetCountsWithFilters(
            String queryString, List<String> facetFieldsToQuery,
            Map<String, List<String>> filters, int maxValues, int minCount) {

        log.debug("getFacetCountsWithFilters: query='{}' filters={}", queryString, filters);
        Map<String, List<FacetValue>> result = new HashMap<>();

        if (facetFieldsToQuery == null || facetFieldsToQuery.isEmpty()) {
            return result;
        }

        try (IndexReader indexReader = DirectoryReader.open(getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(indexReader);

            SortedSetDocValuesReaderState state =
                new DefaultSortedSetDocValuesReaderState(indexReader, facetsConfig);

            BooleanQuery.Builder combined = new BooleanQuery.Builder();

            if (queryString != null && !queryString.isEmpty()) {
                combined.add(parseQuery(queryString, getQueryAnalyzer()), BooleanClause.Occur.MUST);
            }

            if (filters != null) {
                for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    List<String> values = entry.getValue();
                    if (values.size() == 1) {
                        combined.add(new TermQuery(new Term(field, values.get(0))),
                            BooleanClause.Occur.MUST);
                    } else {
                        List<BytesRef> valRefs = new ArrayList<>(values.size());
                        for (String v : values) {
                            valRefs.add(new BytesRef(v));
                        }
                        combined.add(new TermInSetQuery(field, valRefs),
                            BooleanClause.Occur.MUST);
                    }
                }
            }

            Facets facets;
            BooleanQuery bq = combined.build();
            if (bq.clauses().isEmpty()) {
                facets = new SortedSetDocValuesFacetCounts(state);
            } else {
                FacetsCollector fc = new FacetsCollector();
                searcher.search(bq, fc);
                facets = new SortedSetDocValuesFacetCounts(state, fc);
            }

            for (String field : facetFieldsToQuery) {
                List<FacetValue> fieldFacets = new ArrayList<>();
                try {
                    FacetResult facetResult = (maxValues <= 0)
                        ? facets.getAllChildren(field)
                        : facets.getTopChildren(maxValues, field);
                    if (facetResult != null && facetResult.labelValues != null) {
                        for (LabelAndValue lv : facetResult.labelValues) {
                            if (minCount <= 0 || lv.value.longValue() >= minCount) {
                                fieldFacets.add(new FacetValue(lv.label, lv.value.longValue()));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.debug("No facet data for field '{}': {}", field, e.getMessage());
                }
                result.put(field, fieldFacets);
            }
        } catch (IOException ex) {
            throw new TextIndexException("getFacetCountsWithFilters", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(queryString, ex.getMessage());
        }

        return result;
    }

    // ---- Filtered queries ----

    public List<TextHit> queryWithFilters(List<Resource> props, String qs,
            Map<String, List<String>> filters, String graphURI, String lang,
            int limit, String highlight) {

        if (filters == null || filters.isEmpty()) {
            return query(props, qs, graphURI, lang, limit, highlight);
        }

        try (IndexReader indexReader = DirectoryReader.open(getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(indexReader);

            BooleanQuery.Builder combined = new BooleanQuery.Builder();

            if (qs != null && !qs.isEmpty()) {
                combined.add(parseQuery(qs, getQueryAnalyzer()), BooleanClause.Occur.MUST);
            }

            for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                String field = entry.getKey();
                List<String> values = entry.getValue();
                if (values.size() == 1) {
                    combined.add(new TermQuery(new Term(field, values.get(0))),
                        BooleanClause.Occur.MUST);
                } else {
                    List<BytesRef> valRefs = new ArrayList<>(values.size());
                    for (String v : values) {
                        valRefs.add(new BytesRef(v));
                    }
                    combined.add(new TermInSetQuery(field, valRefs),
                        BooleanClause.Occur.MUST);
                }
            }

            int maxHits = limit > 0 ? limit : MAX_N;
            TopDocs topDocs = searcher.search(combined.build(), maxHits);

            List<TextHit> results = new ArrayList<>();
            String entityField = getDocDef().getEntityField();
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = storedFields.document(sd.doc);
                String uri = doc.get(entityField);
                if (uri != null) {
                    Node entityNode = TextQueryFuncs.stringToNode(uri);
                    results.add(new TextHit(entityNode, sd.score, null));
                }
            }
            return results;
        } catch (IOException ex) {
            throw new TextIndexException("queryWithFilters", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(qs, ex.getMessage());
        }
    }

    public long countQuery(String queryString, Map<String, List<String>> filters) {
        try (IndexReader indexReader = DirectoryReader.open(getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            if (queryString != null && !queryString.isEmpty()) {
                bq.add(parseQuery(queryString, getQueryAnalyzer()), BooleanClause.Occur.MUST);
            }
            if (filters != null) {
                for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    List<String> values = entry.getValue();
                    if (values.size() == 1) {
                        bq.add(new TermQuery(new Term(field, values.get(0))),
                            BooleanClause.Occur.MUST);
                    } else {
                        List<BytesRef> valRefs = new ArrayList<>(values.size());
                        for (String v : values) {
                            valRefs.add(new BytesRef(v));
                        }
                        bq.add(new TermInSetQuery(field, valRefs),
                            BooleanClause.Occur.MUST);
                    }
                }
            }
            BooleanQuery query = bq.build();
            if (query.clauses().isEmpty()) {
                return indexReader.numDocs();
            }
            return searcher.count(query);
        } catch (IOException ex) {
            throw new TextIndexException("countQuery", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(queryString, ex.getMessage());
        }
    }

    // ---- CQL-based query methods ----

    public List<TextHit> queryWithCql(List<Resource> props, String qs,
            CqlExpression cqlFilter, List<SortSpec> sortSpecs,
            String graphURI, String lang, int limit, String highlight) {

        if (cqlFilter == null && (sortSpecs == null || sortSpecs.isEmpty())) {
            return query(props, qs, graphURI, lang, limit, highlight);
        }

        try (IndexReader indexReader = DirectoryReader.open(getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(indexReader);

            BooleanQuery.Builder combined = new BooleanQuery.Builder();

            if (qs != null && !qs.isEmpty()) {
                combined.add(parseQuery(qs, getQueryAnalyzer()), BooleanClause.Occur.MUST);
            }

            if (cqlFilter != null) {
                CqlToLuceneCompiler compiler = new CqlToLuceneCompiler(shaclMapping);
                CqlToLuceneCompiler.CompileResult result = compiler.compile(cqlFilter);
                if (result.pushed() != null) {
                    combined.add(result.pushed(), BooleanClause.Occur.MUST);
                }
                if (result.residual() != null) {
                    log.warn("CQL filter has residual expressions that cannot be pushed to Lucene and will be ignored: {}",
                        result.residual().toCanonical());
                }
            }

            int maxHits = limit > 0 ? limit : MAX_N;
            Sort luceneSort = buildLuceneSort(sortSpecs);

            TopDocs topDocs;
            if (luceneSort != null) {
                topDocs = searcher.search(combined.build(), maxHits, luceneSort);
            } else {
                topDocs = searcher.search(combined.build(), maxHits);
            }

            List<TextHit> results = new ArrayList<>();
            String entityField = getDocDef().getEntityField();
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = storedFields.document(sd.doc);
                String uri = doc.get(entityField);
                if (uri != null) {
                    Node entityNode = TextQueryFuncs.stringToNode(uri);
                    results.add(new TextHit(entityNode, sd.score, null));
                }
            }
            return results;
        } catch (IOException ex) {
            throw new TextIndexException("queryWithCql", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(qs, ex.getMessage());
        }
    }

    public Map<String, List<FacetValue>> getFacetCountsWithCql(
            String queryString, List<String> facetFieldsToQuery,
            CqlExpression cqlFilter, int maxValues, int minCount) {

        Map<String, List<FacetValue>> result = new HashMap<>();

        if (facetFieldsToQuery == null || facetFieldsToQuery.isEmpty()) {
            return result;
        }

        try (IndexReader indexReader = DirectoryReader.open(getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            SortedSetDocValuesReaderState state =
                new DefaultSortedSetDocValuesReaderState(indexReader, facetsConfig);

            BooleanQuery.Builder combined = new BooleanQuery.Builder();

            if (queryString != null && !queryString.isEmpty()) {
                combined.add(parseQuery(queryString, getQueryAnalyzer()), BooleanClause.Occur.MUST);
            }

            if (cqlFilter != null) {
                CqlToLuceneCompiler compiler = new CqlToLuceneCompiler(shaclMapping);
                CqlToLuceneCompiler.CompileResult cr = compiler.compile(cqlFilter);
                if (cr.pushed() != null) {
                    combined.add(cr.pushed(), BooleanClause.Occur.MUST);
                }
                if (cr.residual() != null) {
                    log.warn("CQL filter has residual expressions that cannot be pushed to Lucene and will be ignored: {}",
                        cr.residual().toCanonical());
                }
            }

            Facets facets;
            BooleanQuery bq = combined.build();
            if (bq.clauses().isEmpty()) {
                facets = new SortedSetDocValuesFacetCounts(state);
            } else {
                FacetsCollector fc = new FacetsCollector();
                searcher.search(bq, fc);
                facets = new SortedSetDocValuesFacetCounts(state, fc);
            }

            for (String field : facetFieldsToQuery) {
                List<FacetValue> fieldFacets = new ArrayList<>();
                try {
                    FacetResult facetResult = (maxValues <= 0)
                        ? facets.getAllChildren(field)
                        : facets.getTopChildren(maxValues, field);
                    if (facetResult != null && facetResult.labelValues != null) {
                        for (LabelAndValue lv : facetResult.labelValues) {
                            if (minCount <= 0 || lv.value.longValue() >= minCount) {
                                fieldFacets.add(new FacetValue(lv.label, lv.value.longValue()));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.debug("No facet data for field '{}': {}", field, e.getMessage());
                }
                result.put(field, fieldFacets);
            }
        } catch (IOException ex) {
            throw new TextIndexException("getFacetCountsWithCql", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(queryString, ex.getMessage());
        }

        return result;
    }

    public long countQueryWithCql(String queryString, CqlExpression cqlFilter) {
        try (IndexReader indexReader = DirectoryReader.open(getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            if (queryString != null && !queryString.isEmpty()) {
                bq.add(parseQuery(queryString, getQueryAnalyzer()), BooleanClause.Occur.MUST);
            }
            if (cqlFilter != null) {
                CqlToLuceneCompiler compiler = new CqlToLuceneCompiler(shaclMapping);
                CqlToLuceneCompiler.CompileResult cr = compiler.compile(cqlFilter);
                if (cr.pushed() != null) {
                    bq.add(cr.pushed(), BooleanClause.Occur.MUST);
                }
                if (cr.residual() != null) {
                    log.warn("CQL filter has residual expressions that cannot be pushed to Lucene and will be ignored: {}",
                        cr.residual().toCanonical());
                }
            }
            BooleanQuery query = bq.build();
            if (query.clauses().isEmpty()) {
                return indexReader.numDocs();
            }
            return searcher.count(query);
        } catch (IOException ex) {
            throw new TextIndexException("countQueryWithCql", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(queryString, ex.getMessage());
        }
    }

    // ---- Sort ----

    public Sort buildLuceneSort(List<SortSpec> sortSpecs) {
        if (sortSpecs == null || sortSpecs.isEmpty()) {
            return null;
        }

        SortField[] fields = new SortField[sortSpecs.size()];
        for (int i = 0; i < sortSpecs.size(); i++) {
            SortSpec spec = sortSpecs.get(i);
            SortField.Type sortType = SortField.Type.STRING; // default

            ShaclIndexMapping.FieldDef fd = shaclMapping.findField(spec.field());
            if (fd != null) {
                sortType = switch (fd.getFieldType()) {
                    case KEYWORD -> SortField.Type.STRING;
                    case INT -> SortField.Type.INT;
                    case LONG -> SortField.Type.LONG;
                    case DOUBLE -> SortField.Type.DOUBLE;
                    case TEXT -> throw new TextIndexException(
                        "Cannot sort on TEXT field '" + spec.field() + "'. Use KEYWORD for sortable fields.");
                    case LATLON -> throw new TextIndexException(
                        "Cannot sort on LATLON field '" + spec.field() + "'.");
                };
            }

            fields[i] = new SortField(spec.field(), sortType, spec.descending());
        }
        return new Sort(fields);
    }
}
