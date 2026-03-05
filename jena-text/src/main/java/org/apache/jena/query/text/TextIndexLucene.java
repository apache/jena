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

package org.apache.jena.query.text ;

import java.io.IOException ;
import java.util.*;
import java.util.Map.Entry ;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.text.analyzer.IndexingMultilingualAnalyzer;
import org.apache.jena.query.text.analyzer.MultilingualAnalyzer;
import org.apache.jena.query.text.analyzer.QueryMultilingualAnalyzer;
import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer ;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper ;
import org.apache.lucene.analysis.standard.StandardAnalyzer ;
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
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException ;
import org.apache.lucene.queryparser.classic.QueryParser ;
import org.apache.lucene.queryparser.classic.QueryParserBase ;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser ;
import org.apache.lucene.queryparser.surround.query.BasicQueryFactory;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.jena.query.text.cql.CqlExpression;
import org.apache.jena.query.text.cql.CqlToLuceneCompiler;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.util.BytesRef ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TextIndexLucene implements TextIndex {
    private static Logger          log      = LoggerFactory.getLogger(TextIndexLucene.class) ;

    private static int             MAX_N    = 10000 ;
    // prefix for storing datatype URIs in the index, to distinguish them from language tags
    private static final String    DATATYPE_PREFIX = "^^";

    private static final String    RIGHT_ARROW = "\u21a6";
    private static final String    LEFT_ARROW  = "\u21a4";
    private static final String    DIVIDES  =    "\u2223";
    private static final String    Z_MORE_SEPS = "([\\p{Z}\u0f0b\0f0c\0f0d\180e]*?)";

    public static final FieldType  ftIRI ;
    static {
        ftIRI = new FieldType() ;
        ftIRI.setTokenized(false) ;
        ftIRI.setStored(true) ;
        ftIRI.setIndexOptions(IndexOptions.DOCS);
        ftIRI.freeze() ;
    }
    public static final FieldType  ftString = StringField.TYPE_NOT_STORED ;

    private final EntityDefinition docDef ;
    private final Directory        directory ;
    private final Analyzer         indexAnalyzer ;
    private       Analyzer         defaultAnalyzer ;
    private       Map<String, Analyzer> analyzerPerField;
    private final Analyzer         queryAnalyzer ;
    private final String           queryParserType ;
    private final FieldType        ftText ;
    private final FieldType        ftTextNotStored ;      // used for lang derived fields
    private final FieldType        ftTextStoredNoIndex ;  // used for lang derived fields
    private final boolean          isMultilingual ;
    private final int              maxBasicQueries ;
    private final boolean          ignoreIndexErrors ;
    private final int              maxFacetHits ;

    private final ShaclIndexMapping shaclMapping; // null if triple model
    private Map<String, Analyzer> multilingualQueryAnalyzers = new HashMap<>();
    private final List<String> facetFields;
    private final FacetsConfig facetsConfig;

    // The IndexWriter can't be final because we may have to recreate it if rollback() is called.
    // However, it needs to be volatile in case the next write transaction is on a different thread,
    // but we do not need locking because we are assuming that there can only be one writer
    // at a time (enforced elsewhere).
    private volatile IndexWriter   indexWriter ;

    /**
     * Constructs a new TextIndexLucene.
     *
     * @param directory The Lucene Directory for the index
     * @param config The config definition for the index instantiation.
     */
    public TextIndexLucene(Directory directory, TextIndexConfig config) {
        this.directory = directory ;
        this.docDef = config.getEntDef() ;

        this.maxBasicQueries = config.getMaxBasicQueries();

        this.isMultilingual = config.isMultilingualSupport();
        if (this.isMultilingual &&  config.getEntDef().getLangField() == null) {
            //multilingual index cannot work without lang field
            docDef.setLangField("lang");
        }

        this.ignoreIndexErrors = config.ignoreIndexErrors ;
        this.shaclMapping = config.getShaclMapping();
        this.maxFacetHits = config.getMaxFacetHits();

        // create the analyzer as a wrapper that uses KeywordAnalyzer for
        // entity and graph fields and the configured analyzer(s) for all other
        analyzerPerField = new HashMap<>() ;
        analyzerPerField.put(docDef.getEntityField(), new KeywordAnalyzer()) ;
        if ( docDef.getGraphField() != null )
            analyzerPerField.put(docDef.getGraphField(), new KeywordAnalyzer()) ;
        if ( docDef.getLangField() != null )
            analyzerPerField.put(docDef.getLangField(), new KeywordAnalyzer()) ;

        for (String field : docDef.fields()) {
            Analyzer _analyzer = docDef.getAnalyzer(field);
            if (_analyzer != null) {
                analyzerPerField.put(field, _analyzer);
            }
        }

        defaultAnalyzer = (null != config.getAnalyzer()) ? config.getAnalyzer() : new StandardAnalyzer();
        Analyzer indexDefault = defaultAnalyzer;
        Analyzer queryDefault = defaultAnalyzer;
        if (this.isMultilingual) {
            queryDefault = new MultilingualAnalyzer(defaultAnalyzer);
            indexDefault = Util.usingIndexAnalyzers() ? new IndexingMultilingualAnalyzer(defaultAnalyzer) : queryDefault;
        }
        this.indexAnalyzer = new PerFieldAnalyzerWrapper(indexDefault, analyzerPerField) ;
        this.queryAnalyzer = (null != config.getQueryAnalyzer()) ? config.getQueryAnalyzer() : new PerFieldAnalyzerWrapper(queryDefault, analyzerPerField) ;
        this.queryParserType = config.getQueryParser() ;
        log.debug("TextIndexLucene defaultAnalyzer: {}, indexAnalyzer: {}, queryAnalyzer: {}, queryParserType: {}", defaultAnalyzer, indexAnalyzer, queryAnalyzer, queryParserType);
        this.ftText = config.isValueStored() ? TextField.TYPE_STORED : TextField.TYPE_NOT_STORED ;
        // the following is used for lang derived fields
        this.ftTextNotStored = TextField.TYPE_NOT_STORED ;
        this.ftTextStoredNoIndex = new FieldType();
        this.ftTextStoredNoIndex.setIndexOptions(IndexOptions.NONE);
        this.ftTextStoredNoIndex.setStored(true);
        this.ftTextStoredNoIndex.freeze();
        if (config.isValueStored() && docDef.getLangField() == null)
            log.warn("Values stored but langField not set. Returned values will not have language tag or datatype.");

        // Initialize facet fields configuration (SHACL mode only)
        if (this.shaclMapping != null) {
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
        } else {
            this.facetFields = Collections.emptyList();
            this.facetsConfig = new FacetsConfig();
        }

        openIndexWriter();
    }

    private void openIndexWriter() {
        IndexWriterConfig wConfig = new IndexWriterConfig(indexAnalyzer) ;
        try
        {
            indexWriter = new IndexWriter(directory, wConfig) ;
            // Force a commit to create the index, otherwise querying before writing will cause an exception
            indexWriter.commit();
        }
        catch (IndexFormatTooOldException e) {
            throw new TextIndexException("jena-text/Lucene cannot use indexes created before Jena 3.3.0. "
                + "Please rebuild your text index using jena.textindexer from Jena 3.3.0 or above.", e);
        }
        catch (IOException e)
        {
            throw new TextIndexException("openIndexWriter", e) ;
        }
    }

    public Directory getDirectory() {
        return directory ;
    }

    public Analyzer getAnalyzer() {
        return indexAnalyzer ;
    }

    public Analyzer getQueryAnalyzer() {
        return queryAnalyzer ;
    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    @Override
    public void prepareCommit() {
        try {
            indexWriter.prepareCommit();
        }
        catch (IOException e) {
            throw new TextIndexException("prepareCommit", e);
        }
    }

    @Override
    public void commit() {
        try {
            indexWriter.commit();
        }
        catch (IOException e) {
            throw new TextIndexException("commit", e);
        }
    }

    @Override
    public void rollback() {
        IndexWriter idx = indexWriter;
        indexWriter = null;
        try {
            idx.rollback();
        }
        catch (IOException e) {
            throw new TextIndexException("rollback", e);
        }

        // The rollback will close the indexWriter, so we need to reopen it
        openIndexWriter();
    }

    @Override
    public void close() {
        try {
            indexWriter.close() ;
        }
        catch (IOException ex) {
            throw new TextIndexException("close", ex) ;
        }
    }

    @Override public void updateEntity(Entity entity) {
        if ( log.isDebugEnabled() )
            if (log.isTraceEnabled() && entity != null)
                log.trace("Update entity: " + entity.toStringDetail()) ;
            else
                log.debug("Update entity: " + entity) ;
        try {
            updateDocument(entity);
        } catch (IOException e) {
            throw new TextIndexException("updateEntity", e) ;
        }
    }

    protected void updateDocument(Entity entity) throws IOException {
        Document doc = doc(entity);
        Term term = new Term(docDef.getEntityField(), entity.getId());
        try {
            indexWriter.updateDocument(term, doc) ;
        } catch (Exception ex) {
            log.error("Error updating {} with term: {} message: {}", doc, term, ex.getMessage());
            if (ignoreIndexErrors) {
                return;
            } else {
                throw ex; // the original behavior
            }
        }
        log.trace("updated: {}", doc) ;
    }

    @Override
    public void addEntity(Entity entity) {
        if ( log.isDebugEnabled() )
            if (log.isTraceEnabled() && entity != null)
                log.trace("Add entity: " + entity.toStringDetail()) ;
            else
                log.debug("Add entity: " + entity) ;
        try {
            addDocument(entity);
        }
        catch (IOException e) {
            throw new TextIndexException("addEntity", e) ;
        }
    }

    protected void addDocument(Entity entity) throws IOException {
        Document doc = doc(entity) ;
        try {
            indexWriter.addDocument(doc) ;
        } catch (Exception ex) {
            log.error("Error adding {} message: {}", doc, ex.getMessage());
            if (ignoreIndexErrors) {
                return;
            } else {
                throw ex; // the original behavior
            }
        }
        log.trace("added: {}", doc) ;
    }

    @Override
    public void deleteEntity(Entity entity) {
        if (docDef.getUidField() == null)
            return;

        if ( log.isDebugEnabled() )
            if (log.isTraceEnabled() && entity != null)
                log.trace("Delete entity: " + entity.toStringDetail()) ;
            else
                log.debug("Delete entity: "+entity) ;
        try {
            Map<String, Object> map = entity.getMap();
            String property = map.keySet().iterator().next();
            String value = (String)map.get(property);
            String hash = entity.getChecksum(property, value);
            Term uid = new Term(docDef.getUidField(), hash);
            indexWriter.deleteDocuments(uid);

        } catch (Exception e) {
            throw new TextIndexException("deleteEntity", e) ;
        }
    }

    protected Document doc(Entity entity) {
        Document doc = new Document() ;
        Field entField = new Field(docDef.getEntityField(), entity.getId(), ftIRI) ;
        doc.add(entField) ;

        String graphField = docDef.getGraphField() ;
        if ( graphField != null ) {
            Field gField = new Field(graphField, entity.getGraph(), ftIRI) ;
            doc.add(gField) ;
        }

        String langField = docDef.getLangField() ;
        String uidField = docDef.getUidField() ;

        for ( Entry<String, Object> e : entity.getMap().entrySet() ) {
            String field = e.getKey();
            String value = (String) e.getValue();
            FieldType ft = (docDef.getNoIndex(field)) ? ftTextStoredNoIndex : ftText ;
            doc.add( new Field(field, value, ft) );
            if (langField != null) {
                String lang = entity.getLanguage();
                RDFDatatype datatype = entity.getDatatype();
                if (lang != null && !"".equals(lang)) {
                    doc.add(new Field(langField, lang, StringField.TYPE_STORED));
                    if (this.isMultilingual) {
                        // add a field that uses a language-specific analyzer via MultilingualAnalyzer
                        doc.add(new Field(field + "_" + lang, value, ftTextNotStored));
                        // add fields for any defined auxiliary indexes
                        List<String> auxIndexes = Util.getAuxIndexes(lang);
                        if (auxIndexes != null) {
                            for (String auxTag : auxIndexes) {
                                doc.add(new Field(field + "_" + auxTag, value, ftTextNotStored));
                            }
                        }
                    }
                } else if (datatype != null && !datatype.equals(XSDDatatype.XSDstring)) {
                    // for non-string and non-langString datatypes, store the datatype in langField
                    doc.add(new Field(langField, DATATYPE_PREFIX + datatype.getURI(), StringField.TYPE_STORED));
                }
            }
            if (uidField != null) {
                String hash = entity.getChecksum(field, value);
                doc.add(new Field(uidField, hash, StringField.TYPE_STORED));
            }
        }
        return doc ;
    }

    @Override
    public Map<String, Node> get(String uri) {
        try {
            IndexReader indexReader = DirectoryReader.open(directory);
            List<Map<String, Node>> x = get$(indexReader, uri) ;
            if ( x.size() == 0 )
                return null ;
            // if ( x.size() > 1)
            // throw new TextIndexException("Multiple entires for "+uri) ;
            return x.get(0) ;
        }
        catch (Exception ex) {
            throw new TextIndexException("get", ex) ;
        }
    }

    private Query parseQuery(String queryString, Analyzer analyzer) throws ParseException {
        // Treat "*" as match-all rather than an expensive WildcardQuery
        if ("*".equals(queryString)) {
            return new MatchAllDocsQuery();
        }

        Query query = null;
        QueryParser qp = null;

        switch(queryParserType) {
            case "QueryParser":
                // Drop to default
                break;
            case "SurroundQueryParser":
                try {
                    query = org.apache.lucene.queryparser.surround.parser.QueryParser.parse(queryString).makeLuceneQueryField(docDef.getPrimaryField(), new BasicQueryFactory(this.maxBasicQueries));
                } catch(org.apache.lucene.queryparser.surround.parser.ParseException e) {
                    throw new ParseException(e.getMessage());
                }
                return query;
            case "ComplexPhraseQueryParser":
                qp = new ComplexPhraseQueryParser(docDef.getPrimaryField(), analyzer);
                break;
            case "AnalyzingQueryParser": // since Lucene 7 analyzing is done by QueryParser
                log.warn("Deprecated query parser type 'AnalyzingQueryParser'. Defaulting to standard QueryParser");
                break;
            default:
                log.warn("Unknown query parser type '" + queryParserType + "'. Defaulting to standard QueryParser");
        }

        if (qp == null) {
            qp = new QueryParser(docDef.getPrimaryField(), analyzer);
        }
        qp.setAllowLeadingWildcard(true);

        // Some analyzers are not thread safe, at least when used via ConfigurableAnalyzer
        // Could be more selective here about when to synchronize but the suspect analyzer
        // can appear at several places in the wrapped nest of analyzers so being conservative
        synchronized (this) {
            query = qp.parse(queryString);
            return query;
        }
    }

    private List<Map<String, Node>> get$(IndexReader indexReader, String uri) throws ParseException, IOException {
        String escaped = QueryParserBase.escape(uri) ;
        String qs = docDef.getEntityField() + ":" + escaped ;
        Query query = parseQuery(qs, queryAnalyzer) ;
        IndexSearcher indexSearcher = new IndexSearcher(indexReader) ;
        ScoreDoc[] sDocs = indexSearcher.search(query, 1).scoreDocs ;
        List<Map<String, Node>> records = new ArrayList<>() ;
        StoredFields sFields = indexSearcher.storedFields();

        for ( ScoreDoc sd : sDocs ) {
            Document doc = sFields.document(sd.doc);

            String[] x = doc.getValues(docDef.getEntityField()) ;
            if ( x.length != 1 ) {}
            String uriStr = x[0] ;
            Map<String, Node> record = new HashMap<>() ;
            Node entity = NodeFactory.createURI(uriStr) ;
            record.put(docDef.getEntityField(), entity) ;

            for ( String f : docDef.fields() ) {
                // log.info("Field: "+f) ;
                String[] values = doc.getValues(f) ;
                for ( String v : values ) {
                    Node n = entryToNode(v) ;
                    record.put(f, n) ;
                }
                records.add(record) ;
            }
        }
        return records ;
    }

    @Override
    public List<TextHit> query(Node property, String qs, String graphURI, String lang) {
        return query(property, qs, graphURI, lang, MAX_N) ;
    }

    @Override
    public List<TextHit> query(Node property, String qs, String graphURI, String lang, int limit) {
        return query(property, qs, graphURI, lang, limit, null) ;
    }

    @Override
    public List<TextHit> query(Node propNode, String qs, String graphURI, String lang, int limit, String highlight) {
        List<Resource> props = new ArrayList<>();
        if (propNode != null) {
            props.add(ResourceFactory.createProperty(propNode.getURI()));
        }
        return query((String) null, props, qs, graphURI, lang, limit, highlight);    }

    @Override
    public List<TextHit> query(String subjectUri, Node propNode, String qs, String graphURI, String lang, int limit, String highlight) {
        List<Resource> props = new ArrayList<>();
        if (propNode != null) {
            props.add(ResourceFactory.createProperty(propNode.getURI()));
        }
        return query(subjectUri, props, qs, graphURI, lang, limit, highlight);
    }

    @Override
    public List<TextHit> query(List<Resource> props, String qs, String graphURI, String lang, int limit, String highlight) {
        return query((String) null, props, qs, graphURI, lang, limit, highlight);
    }

    @Override
    public List<TextHit> query(Node subj, List<Resource> props, String qs, String graphURI, String lang, int limit, String highlight) {
        String subjectUri = subj == null || Var.isVar(subj) || !subj.isURI() ? null : subj.getURI();
        return query(subjectUri, props, qs, graphURI, lang, limit, highlight);
    }

    @Override
    public List<TextHit> query(String subjectUri, List<Resource> props, String qs, String graphURI, String lang, int limit, String highlight) {
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            return query$(indexReader, props, qs, addUriPredicate(subjectUri), graphURI, lang, limit, highlight) ;
        }
        catch (ParseException ex) {
            throw new TextIndexParseException(qs, ex.getMessage()) ;
        }
        catch (Exception ex) {
            throw new TextIndexException("query", ex) ;
        }
    }

    //In case of making text search query for concrete subject
    //adding uri predicate will make query much more efficient
    private UnaryOperator<Query> addUriPredicate(String subjectUri) {
        if (subjectUri != null) {
            return (Query textQuery) -> {
                String uriField = docDef.getEntityField();
                return new BooleanQuery.Builder()
                        .add(textQuery, BooleanClause.Occur.MUST)
                        .add(new TermQuery(new Term(uriField, subjectUri)), BooleanClause.Occur.FILTER)
                        .build();
            };
        } else {
            return  UnaryOperator.identity();
        }
    }

    private String getDocField(Document doc, List<String> fields) {
        for (String field : fields) {
            if (doc.get(field) != null) {
                return field;
            }
        }

        return null;
    }

    private List<TextHit> simpleResults(ScoreDoc[] sDocs, IndexSearcher indexSearcher, Query query, List<String> fields)
            throws IOException
    {
        List<TextHit> results = new ArrayList<>() ;
        StoredFields sFields = indexSearcher.storedFields();

        for ( ScoreDoc sd : sDocs ) {
            Document doc = sFields.document(sd.doc);
            log.trace("simpleResults[{}]: fields: {} doc: {}", sd.doc, fields, doc) ;
            String entity = doc.get(docDef.getEntityField()) ;

            Node literal = null;

            String field = getDocField(doc, fields) ;
            String lexical = doc.get(field);
            Collection<Node> props = docDef.getPredicates(field);
            Node prop = props.isEmpty() ? null : props.iterator().next();

            if (lexical != null) {
                String doclang = doc.get(docDef.getLangField()) ;
                if (doclang != null) {
                    if (doclang.startsWith(DATATYPE_PREFIX)) {
                        String datatype = doclang.substring(DATATYPE_PREFIX.length());
                        TypeMapper tmap = TypeMapper.getInstance();
                        literal = NodeFactory.createLiteralDT(lexical, tmap.getSafeTypeByName(datatype));
                    } else {
                        literal = NodeFactory.createLiteralLang(lexical, doclang);
                    }
                } else {
                    literal = NodeFactory.createLiteralString(lexical);
                }
            }

            String graf = docDef.getGraphField() != null ? doc.get(docDef.getGraphField()) : null ;
            Node graph = graf != null ? TextQueryFuncs.stringToNode(graf) : null;

            Node subject = TextQueryFuncs.stringToNode(entity) ;
            TextHit hit = new TextHit(subject, sd.score, literal, graph, prop);
            results.add(hit) ;
        }

        return results ;
    }

    class HighlightOpts {
        int maxFrags = 3;
        int fragSize = 128;
        String start = RIGHT_ARROW;
        String end = LEFT_ARROW;
        String fragSep = DIVIDES;
        String patternExpr = null;
        boolean joinHi = true;
        boolean joinFrags = true;

        public HighlightOpts(String optStr) {
            String[] opts = optStr.trim().split("\\|");
            for (String opt : opts) {
                opt = opt.trim();
                if (opt.startsWith("m:")) {
                    try {
                        maxFrags = Integer.parseInt(opt.substring(2));
                    } catch (Exception ex) { }
                } else if (opt.startsWith("z:")) {
                    try {
                        fragSize = Integer.parseInt(opt.substring(2));
                    } catch (Exception ex) { }
                } else if (opt.startsWith("s:")) {
                    start = opt.substring(2);
                } else if (opt.startsWith("e:")) {
                    end = opt.substring(2);
                } else if (opt.startsWith("f:")) {
                    fragSep = opt.substring(2);
                } else if (opt.startsWith("jh:")) {
                    String v = opt.substring(3);
                    if ("n".equals(v)) {
                        joinHi = false;
                    }
                } else if (opt.startsWith("jf:")) {
                    String v = opt.substring(3);
                    if ("n".equals(v)) {
                        joinFrags = false;
                    }
                }
            }
            patternExpr = end+Z_MORE_SEPS+start;
        }
    }

    private String frags2string(final TextFragment[] frags, final HighlightOpts opts) {
    	final StringBuilder sb = new StringBuilder();
    	String sep = "";

        for (final TextFragment f : frags) {
        	final String fragStr = f.toString();
        	log.trace("found fragment {}", f);
        	sb.append(sep);
            sb.append(opts.joinHi ? fragStr.replaceAll(opts.patternExpr, "$1") : fragStr);
            sep = opts.fragSep;
        }

        return sb.toString();
    }

    private List<TextHit> highlightResults(ScoreDoc[] sDocs, IndexSearcher indexSearcher, Query query, List<String> fields, String highlight, String queryLang)
            throws IOException, InvalidTokenOffsetsException {
        List<TextHit> results = new ArrayList<>() ;

        HighlightOpts opts = new HighlightOpts(highlight);

        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(opts.start, opts.end);
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
        highlighter.setTextFragmenter(new SimpleFragmenter(opts.fragSize));
        StoredFields sFields = indexSearcher.storedFields();

        for ( ScoreDoc sd : sDocs ) {
            Document doc = sFields.document(sd.doc);
            String entity = doc.get(docDef.getEntityField()) ;

            Node literal = null;
            String field = getDocField(doc, fields) ;
            String lexical = doc.get(field);
            Collection<Node> props = docDef.getPredicates(field);
            Node prop = props.isEmpty() ? null : props.iterator().next(); // pick one - should be only one normally

            String docLang = doc.get(docDef.getLangField()) ;
            String effectiveField = queryLang != null ? field + "_" + Util.getEffectiveLang(docLang, queryLang) : field;
            log.trace("highlightResults[{}]: {}, field: {}, lexical: {}, docLang: {}, effectiveField: {}", sd.doc, doc, field, lexical, docLang, effectiveField) ;
            if (lexical != null) {
                TokenStream tokenStream = indexAnalyzer.tokenStream(effectiveField, lexical);
                log.trace("tokenStream: {}", tokenStream.toString());
                TextFragment[] frags = highlighter.getBestTextFragments(tokenStream, lexical, opts.joinFrags, opts.maxFrags);
                String rez = frags2string(frags, opts);
                log.trace("result: {}, #frags: {}", rez, frags.length) ;
                literal = NodeFactory.createLiteralLang(rez, docLang);
            }

            String graf = docDef.getGraphField() != null ? doc.get(docDef.getGraphField()) : null ;
            Node graph = graf != null ? TextQueryFuncs.stringToNode(graf) : null;

            Node subject = TextQueryFuncs.stringToNode(entity) ;
            TextHit hit = new TextHit(subject, sd.score, literal, graph, prop);
            results.add(hit) ;
        }
        return results ;
    }

    private Analyzer getQueryAnalyzer(boolean usingSearchFor, String lang) {
        if (usingSearchFor) {
            Analyzer qa = multilingualQueryAnalyzers.get(lang);
            if (qa == null) {
                qa = new PerFieldAnalyzerWrapper(new QueryMultilingualAnalyzer(defaultAnalyzer, lang), analyzerPerField);
                multilingualQueryAnalyzers.put(lang, qa);
            }
            return qa;
        } else {
            return queryAnalyzer;
        }
    }

    private String composeQField(String qs, String textField, String lang, boolean usingSearchFor, List<String> searchForTags) {
        String textClause = "";
        String fieldGroupingQueryString = "(" + qs + ")";

        if (usingSearchFor) {
            for (String tag : searchForTags) {
                String tf = textField + "_" + tag;
                textClause += tf + ": " + fieldGroupingQueryString + " ";
            }
        } else {
            if (this.isMultilingual && StringUtils.isNotEmpty(lang) && !lang.equals("none")) {
                textField += "_" + lang;
            }
            textClause = textField + ": " + fieldGroupingQueryString + " ";
        }

        return textClause;
    }

    private List<TextHit> query$(IndexReader indexReader, List<Resource> props, String qs, UnaryOperator<Query> textQueryExtender, String graphURI, String lang, int limit, String highlight)
            throws ParseException, IOException, InvalidTokenOffsetsException
    {
        List<String> textFields = new ArrayList<>();
        String qString = "";
        String langField = getDocDef().getLangField();

        // the list will have at least the lang as an element
        // UNLESS lang is not present in the original PF call
        List<String> searchForTags = Util.getSearchForTags(lang);
        boolean usingSearchFor = Util.usingSearchFor(lang);

        // The set will reduce lucene text field expressions
        // to unique expressions
        Set<String> uniquePropListQueryStrings = new LinkedHashSet<>();

        if (props.isEmpty()) {
            // we got here via
            //    ?s text:query "some query string"
            // or
            //    ?s text:query ( "some query string" ... )
            // so we just need the qs and process additional args below
            // the qs may be a mutli-field query or just a simple query
            qString = qs + " ";
            log.trace("query$ processed EMPTY LIST of properties: {}; Lucene queryString: {}; textFields: {}", props, qString, textFields) ;
        } else {
            // otherwise there are one or more properties to search over
            // possibly with a searchFor list for each property
            // we are guaranteed that the props are all indexed by the way things are called from
            // TextQueryPF which is the only way into this code - DatasetGraphText isn't used anywhere
            // or documented as far as I can tell
            for (Resource prop : props) {
                String textField = docDef.getField(prop.asNode());
                textFields.add(textField);
            }
        }

        log.trace("query$ PROCESSING LIST of properties: {}; Lucene queryString: {}; textFields: {} ", props, qString, textFields) ;
        for (String textField : textFields) {
            uniquePropListQueryStrings.add(composeQField(qs, textField, lang, usingSearchFor, searchForTags));
        }
        qString += String.join("", uniquePropListQueryStrings);

        // we need to check whether there was a lang arg either on the query string
        // or explicitly as an input arg and add it to the qString; otherwise, Lucene
        // won't be able to properly process the query
        if (textFields.isEmpty() && lang != null) {
            qString += composeQField(qs, docDef.getPrimaryField(), lang, usingSearchFor, searchForTags);
        }

        log.trace("query$ PROCESSED LIST of properties: {} with resulting qString: {} ", props, qString) ;

        // add a clause for the lang if not usingSearchFor and there is a defined langFIeld in the config
        if (!usingSearchFor && langField != null && StringUtils.isNotBlank(lang)) {
            qString = "(" + qString + ") AND " + (!lang.equals("none") ? langField+":"+lang : "-"+langField+":*");
            log.trace("query$ ADDING LANG qString: {} ", qString) ;
        }

        if (graphURI != null) {
            String escaped = QueryParserBase.escape(graphURI) ;
            qString = "(" + qString + ") AND " + getDocDef().getGraphField() + ":" + escaped ;
        }

        Analyzer qa = getQueryAnalyzer(usingSearchFor, lang);
        Query textQuery = parseQuery(qString, qa);
        Query query = textQueryExtender.apply(textQuery);

        if ( limit <= 0 )
            limit = MAX_N ;

        log.debug("query$ with LIST: {}; INPUT qString: {}; with queryParserType: {}; parseQuery with {} YIELDS: {}; parsed query: {}; limit: {}", props, qString, queryParserType, qa, textQuery, query, limit) ;

        IndexSearcher indexSearcher = new IndexSearcher(indexReader) ;

        ScoreDoc[] sDocs = indexSearcher.search(query, limit).scoreDocs ;

        // if there were no explicit textFields supplied then Lucene used
        // the default field if defined otherwise Lucene simply interpreted the qs
        // as presented - perhaps with multiple fields indexed on a separate system.
        // In order to handle the results we need to supply the default field to
        // complete the processing in TextQueryPF
        if (textFields.isEmpty()) {
            textFields.add(docDef.getPrimaryField());
        }

        if (highlight != null) {
            return highlightResults(sDocs, indexSearcher, query, textFields, highlight, lang);
        } else {
            return simpleResults(sDocs, indexSearcher, query, textFields);
        }
    }

    @Override
    public EntityDefinition getDocDef() {
        return docDef ;
    }

    public ShaclIndexMapping getShaclMapping() {
        return shaclMapping;
    }

    public boolean isShaclMode() {
        return shaclMapping != null;
    }

    /**
     * Build a Lucene Document from an Entity using SHACL field definitions.
     * Handles TEXT, KEYWORD, INT, LONG, DOUBLE field types with appropriate Lucene field classes.
     */
    protected Document docFromMapping(Entity entity, ShaclIndexMapping.IndexProfile profile) {
        Document doc = new Document();

        // Entity URI field (always stored and indexed for lookup)
        String docIdField = profile.getDocIdField();
        doc.add(new Field(docIdField, entity.getId(), ftIRI));

        // Discriminator field — first target class local name
        String discriminatorField = profile.getDiscriminatorField();
        if (discriminatorField != null && !profile.getTargetClasses().isEmpty()) {
            Node firstClass = profile.getTargetClasses().iterator().next();
            String localName = firstClass.getLocalName();
            if (localName != null && !localName.isEmpty()) {
                doc.add(new StringField(discriminatorField, localName, org.apache.lucene.document.Field.Store.YES));
            }
        }

        // Process each field
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
        org.apache.lucene.document.Field.Store store =
            fieldDef.isStored() ? org.apache.lucene.document.Field.Store.YES : org.apache.lucene.document.Field.Store.NO;

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

    /**
     * Update (or create) the Lucene document for a specific entity and profile.
     * Uses a composite term (entity URI + discriminator) to identify the document.
     */
    public void updateEntityForProfile(Entity entity, ShaclIndexMapping.IndexProfile profile) {
        try {
            Document doc = docFromMapping(entity, profile);
            Document indexDoc = facetFields.isEmpty() ? doc : facetsConfig.build(doc);

            // Delete by composite query: uri + docType
            String docIdField = profile.getDocIdField();
            String discriminatorField = profile.getDiscriminatorField();
            Node firstClass = profile.getTargetClasses().iterator().next();
            String localName = firstClass.getLocalName();

            BooleanQuery deleteQuery = new BooleanQuery.Builder()
                .add(new TermQuery(new Term(docIdField, entity.getId())), BooleanClause.Occur.MUST)
                .add(new TermQuery(new Term(discriminatorField, localName)), BooleanClause.Occur.MUST)
                .build();

            indexWriter.deleteDocuments(deleteQuery);
            indexWriter.addDocument(indexDoc);
            log.trace("updateEntityForProfile: {} profile={}", entity.getId(), profile.getShapeNode());
        } catch (IOException e) {
            throw new TextIndexException("updateEntityForProfile", e);
        }
    }

    /**
     * Delete all Lucene documents for a given entity URI (across all profiles).
     */
    public void deleteEntityByUri(String entityUri) {
        try {
            // In SHACL mode, the docIdField may vary by profile, but we use a reasonable approach:
            // delete by any docIdField found in the mapping
            Set<String> docIdFields = new HashSet<>();
            if (shaclMapping != null) {
                for (ShaclIndexMapping.IndexProfile profile : shaclMapping.getProfiles()) {
                    docIdFields.add(profile.getDocIdField());
                }
            }
            if (docIdFields.isEmpty()) {
                docIdFields.add(docDef.getEntityField());
            }
            for (String field : docIdFields) {
                indexWriter.deleteDocuments(new Term(field, entityUri));
            }
            log.trace("deleteEntityByUri: {}", entityUri);
        } catch (IOException e) {
            throw new TextIndexException("deleteEntityByUri", e);
        }
    }

    private Node entryToNode(String v) {
        return NodeFactory.createLiteralString(v) ;
    }

    /**
     * Get facet counts using native Lucene SortedSetDocValues faceting.
     * This is efficient O(1) facet counting that doesn't iterate through documents.
     *
     * @param facetFieldsToQuery List of field names to get facet counts for
     * @param maxValues Maximum number of facet values to return per field
     * @return Map of field name to list of FacetValue (value + count)
     */
    public Map<String, List<FacetValue>> getFacetCounts(List<String> facetFieldsToQuery, int maxValues) {
        return getFacetCounts(null, facetFieldsToQuery, maxValues);
    }

    /**
     * Get facet counts using native Lucene SortedSetDocValues faceting,
     * optionally filtered by a search query.
     *
     * @param queryString Optional search query to filter documents (null for all documents)
     * @param facetFieldsToQuery List of field names to get facet counts for
     * @param maxValues Maximum number of facet values to return per field
     * @return Map of field name to list of FacetValue (value + count)
     */
    public Map<String, List<FacetValue>> getFacetCounts(String queryString, List<String> facetFieldsToQuery, int maxValues) {
        return getFacetCounts(queryString, facetFieldsToQuery, maxValues, 0);
    }

    /**
     * Get facet counts using native Lucene SortedSetDocValues faceting,
     * optionally filtered by a search query, with minCount threshold.
     *
     * @param queryString Optional search query to filter documents (null for all documents)
     * @param facetFieldsToQuery List of field names to get facet counts for
     * @param maxValues Maximum number of facet values per field (0 or negative for all)
     * @param minCount Minimum count threshold; values below this are excluded (0 for no threshold)
     * @return Map of field name to list of FacetValue (value + count)
     */
    public Map<String, List<FacetValue>> getFacetCounts(String queryString, List<String> facetFieldsToQuery, int maxValues, int minCount) {
        Map<String, List<FacetValue>> result = new HashMap<>();

        if (facetFieldsToQuery == null || facetFieldsToQuery.isEmpty()) {
            return result;
        }

        try {
            IndexReader indexReader = DirectoryReader.open(directory);
            try {
                IndexSearcher searcher = new IndexSearcher(indexReader);

                // Create reader state for SortedSetDocValues faceting
                // Lucene 10 requires FacetsConfig parameter
                SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(indexReader, facetsConfig);

                Facets facets;
                if (queryString == null || queryString.isEmpty()) {
                    // For "open facets" (no search query), use the simpler constructor
                    // This is equivalent to MatchAllDocsQuery but faster
                    facets = new SortedSetDocValuesFacetCounts(state);
                } else if (isShaclMode()) {
                    // In entity-per-document (SHACL) mode, each Lucene document is a
                    // complete entity with all facet DocValues fields. We can collect
                    // facets directly from the query results without extracting URIs.
                    Query query = parseQuery(queryString, queryAnalyzer);
                    FacetsCollector fc = new FacetsCollector();
                    searcher.search(query, fc);
                    facets = new SortedSetDocValuesFacetCounts(state, fc);
                } else {
                    // In triple-per-document mode, each triple creates a separate document.
                    // The search may match documents that don't have facet DocValues
                    // (e.g., label documents vs category documents). To fix this, we:
                    // 1. Execute search to find matching entity URIs
                    // 2. Build a query that matches those URIs
                    // 3. Collect facets from documents with those URIs (which includes facet docs)
                    Query query = parseQuery(queryString, queryAnalyzer);
                    TopDocs topDocs = searcher.search(query, facetSearchLimit());

                    // Extract unique entity URIs from search results
                    String entityField = docDef.getEntityField();
                    Set<String> matchedUris = new HashSet<>();
                    StoredFields storedFields = searcher.storedFields();
                    for (ScoreDoc sd : topDocs.scoreDocs) {
                        Document doc = storedFields.document(sd.doc);
                        String uri = doc.get(entityField);
                        if (uri != null) {
                            matchedUris.add(uri);
                        }
                    }

                    // Build a query that matches all documents for the matched entities
                    // This will include documents with facet fields for those entities
                    // Uses TermInSetQuery instead of BooleanQuery to avoid TooManyClauses
                    List<BytesRef> uriRefs = new ArrayList<>(matchedUris.size());
                    for (String uri : matchedUris) {
                        uriRefs.add(new BytesRef(uri));
                    }
                    Query uriQuery = new TermInSetQuery(entityField, uriRefs);

                    // Now collect facets from all documents for matched entities
                    FacetsCollector fc = new FacetsCollector();
                    searcher.search(uriQuery, fc);
                    facets = new SortedSetDocValuesFacetCounts(state, fc);
                }

                // Extract results for each requested field
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
                        // Field not found or no facet data - return empty list
                        log.debug("No facet data for field '{}': {}", field, e.getMessage());
                    }
                    result.put(field, fieldFacets);
                }
            } finally {
                indexReader.close();
            }
        } catch (IOException ex) {
            throw new TextIndexException("getFacetCounts", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(queryString, ex.getMessage());
        }

        return result;
    }

    /**
     * Check if faceting is enabled for any fields.
     * @return true if at least one field is configured for faceting
     */
    public boolean isFacetingEnabled() {
        return !facetFields.isEmpty();
    }

    private int facetSearchLimit() {
        return maxFacetHits > 0 ? maxFacetHits : Integer.MAX_VALUE;
    }

    /**
     * Get the list of fields configured for faceting.
     * @return unmodifiable list of facet field names
     */
    public List<String> getFacetFields() {
        return Collections.unmodifiableList(facetFields);
    }

    /**
     * Query with structured filters applied.
     * <p>
     * Uses a two-pass approach to handle the triple-based document model:
     * 1. Execute text query to get matching entity URIs
     * 2. Find entities whose filter-field documents match the filter criteria
     * 3. Return text hits only for entities that pass both text and filter criteria
     *
     * @param props RDF properties to search
     * @param qs Query string
     * @param filters Map of field name to list of values (OR'd within field, AND'd across fields)
     * @param graphURI Graph URI filter (optional)
     * @param lang Language filter (optional)
     * @param limit Maximum number of hits
     * @param highlight Highlight options (optional)
     * @return List of matching TextHit results
     */
    public List<TextHit> queryWithFilters(List<Resource> props, String qs,
            Map<String, List<String>> filters, String graphURI, String lang,
            int limit, String highlight) {

        if (filters == null || filters.isEmpty()) {
            return query(props, qs, graphURI, lang, limit, highlight);
        }

        if (isShaclMode()) {
            // In entity-per-document mode, all fields live on the same document.
            // Build a single combined Lucene query: text + filter terms.
            return queryWithFiltersDirect(props, qs, filters, graphURI, lang, limit, highlight);
        }

        // Triple-per-document mode: two-step approach
        // Step 1: Execute the text query to get all matching hits
        List<TextHit> allHits = query(props, qs, graphURI, lang, limit > 0 ? limit * 10 : MAX_N, highlight);

        if (allHits.isEmpty()) {
            return allHits;
        }

        // Step 2: Find which entity URIs pass the filter criteria
        Set<String> filteredUris = findFilteredEntityUris(allHits, filters);

        // Step 3: Filter the original hits
        List<TextHit> filtered = new ArrayList<>();
        for (TextHit hit : allHits) {
            String uri = TextQueryFuncs.subjectToString(hit.getNode());
            if (filteredUris.contains(uri)) {
                filtered.add(hit);
                if (limit > 0 && filtered.size() >= limit) {
                    break;
                }
            }
        }
        return filtered;
    }

    /**
     * SHACL entity-per-document: build a single Lucene query combining text + filters.
     * No stored-field extraction needed since all fields are on the same document.
     */
    private List<TextHit> queryWithFiltersDirect(List<Resource> props, String qs,
            Map<String, List<String>> filters, String graphURI, String lang,
            int limit, String highlight) {
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);

            BooleanQuery.Builder combined = new BooleanQuery.Builder();

            // Text query
            if (qs != null && !qs.isEmpty()) {
                combined.add(parseQuery(qs, queryAnalyzer), BooleanClause.Occur.MUST);
            }

            // Filter terms
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
            String entityField = docDef.getEntityField();
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
            throw new TextIndexException("queryWithFiltersDirect", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(qs, ex.getMessage());
        }
    }

    /**
     * Count total matching documents for a query with optional filters.
     * Uses {@code IndexSearcher.count()} which is very efficient.
     */
    public long countQuery(String queryString, Map<String, List<String>> filters) {
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            if (queryString != null && !queryString.isEmpty()) {
                bq.add(parseQuery(queryString, queryAnalyzer), BooleanClause.Occur.MUST);
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

    /**
     * Find entity URIs from the given hits that also match the filter criteria.
     * Queries the index for documents with matching entity URIs and filter field values.
     */
    private Set<String> findFilteredEntityUris(List<TextHit> hits, Map<String, List<String>> filters) {
        // Collect unique entity URIs from hits
        Set<String> hitUris = new LinkedHashSet<>();
        for (TextHit hit : hits) {
            hitUris.add(TextQueryFuncs.subjectToString(hit.getNode()));
        }

        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            String entityField = docDef.getEntityField();

            // For each filter field, find which entity URIs have matching values
            // Start with all hit URIs and intersect with each filter's matches
            Set<String> remainingUris = new HashSet<>(hitUris);

            for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                String field = entry.getKey();
                List<String> values = entry.getValue();

                // Build a query: entityField IN (hitUris) AND field IN (values)
                List<BytesRef> uriRefs = new ArrayList<>(remainingUris.size());
                for (String uri : remainingUris) {
                    uriRefs.add(new BytesRef(uri));
                }
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                builder.add(new TermInSetQuery(entityField, uriRefs), BooleanClause.Occur.MUST);

                if (values.size() == 1) {
                    builder.add(new TermQuery(new Term(field, values.get(0))),
                        BooleanClause.Occur.MUST);
                } else {
                    List<BytesRef> valRefs = new ArrayList<>(values.size());
                    for (String v : values) {
                        valRefs.add(new BytesRef(v));
                    }
                    builder.add(new TermInSetQuery(field, valRefs),
                        BooleanClause.Occur.MUST);
                }

                TopDocs topDocs = searcher.search(builder.build(), facetSearchLimit());
                Set<String> matchedUris = new HashSet<>();
                StoredFields storedFields = searcher.storedFields();
                for (ScoreDoc sd : topDocs.scoreDocs) {
                    Document doc = storedFields.document(sd.doc);
                    String uri = doc.get(entityField);
                    if (uri != null) {
                        matchedUris.add(uri);
                    }
                }

                // Intersect: only keep URIs that matched this filter field
                remainingUris.retainAll(matchedUris);
                if (remainingUris.isEmpty()) {
                    break;
                }
            }

            return remainingUris;
        } catch (IOException ex) {
            throw new TextIndexException("findFilteredEntityUris", ex);
        }
    }

    /**
     * Get facet counts with structured filters applied.
     * <p>
     * Uses the triple-based document model approach:
     * 1. Execute text query to find matching entity URIs
     * 2. Apply filter criteria to narrow the entity set
     * 3. Collect facets from all documents for the remaining entities
     *
     * @param queryString Optional text query to filter documents
     * @param facetFieldsToQuery Fields to get facet counts for
     * @param filters Map of field name to list of values for filtering
     * @param maxValues Maximum facet values per field
     * @return Map of field name to list of FacetValue
     */
    public Map<String, List<FacetValue>> getFacetCountsWithFilters(
            String queryString, List<String> facetFieldsToQuery,
            Map<String, List<String>> filters, int maxValues) {
        return getFacetCountsWithFilters(queryString, facetFieldsToQuery, filters, maxValues, 0);
    }

    /**
     * Get facet counts with structured filters applied and minCount threshold.
     *
     * @param queryString Optional text query to filter documents
     * @param facetFieldsToQuery Fields to get facet counts for
     * @param filters Map of field name to list of values for filtering
     * @param maxValues Maximum facet values per field (0 or negative for all)
     * @param minCount Minimum count threshold; values below this are excluded (0 for no threshold)
     * @return Map of field name to list of FacetValue
     */
    public Map<String, List<FacetValue>> getFacetCountsWithFilters(
            String queryString, List<String> facetFieldsToQuery,
            Map<String, List<String>> filters, int maxValues, int minCount) {

        log.debug("getFacetCountsWithFilters: query='{}' filters={} shaclMode={}", queryString, filters, isShaclMode());
        Map<String, List<FacetValue>> result = new HashMap<>();

        if (facetFieldsToQuery == null || facetFieldsToQuery.isEmpty()) {
            return result;
        }

        try {
            IndexReader indexReader = DirectoryReader.open(directory);
            try {
                IndexSearcher searcher = new IndexSearcher(indexReader);
                String entityField = docDef.getEntityField();

                SortedSetDocValuesReaderState state =
                    new DefaultSortedSetDocValuesReaderState(indexReader, facetsConfig);
                Facets facets;

                if (isShaclMode()) {
                    // In entity-per-document (SHACL) mode, all fields live on the same
                    // document. Build a single combined query and collect facets directly
                    // without extracting URIs via stored fields.
                    BooleanQuery.Builder combined = new BooleanQuery.Builder();

                    if (queryString != null && !queryString.isEmpty()) {
                        combined.add(parseQuery(queryString, queryAnalyzer), BooleanClause.Occur.MUST);
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

                    BooleanQuery bq = combined.build();
                    if (bq.clauses().isEmpty()) {
                        facets = new SortedSetDocValuesFacetCounts(state);
                    } else {
                        FacetsCollector fc = new FacetsCollector();
                        searcher.search(bq, fc);
                        facets = new SortedSetDocValuesFacetCounts(state, fc);
                    }
                } else {
                // Triple-per-document mode: extract URIs via stored fields

                // Step 1: Find entity URIs matching the text query
                Set<String> matchedUris;
                if (queryString == null || queryString.isEmpty()) {
                    // No text query: start with all entities
                    matchedUris = null; // will use MatchAllDocs later
                } else {
                    Query query = parseQuery(queryString, queryAnalyzer);
                    TopDocs topDocs = searcher.search(query, facetSearchLimit());
                    matchedUris = new HashSet<>();
                    StoredFields storedFields = searcher.storedFields();
                    for (ScoreDoc sd : topDocs.scoreDocs) {
                        Document doc = storedFields.document(sd.doc);
                        String uri = doc.get(entityField);
                        if (uri != null) {
                            matchedUris.add(uri);
                        }
                    }

                    if (matchedUris.isEmpty()) {
                        return result;
                    }
                }

                // Step 2: Apply filter criteria to narrow entity URIs
                if (filters != null && !filters.isEmpty()) {
                    Set<String> remainingUris = matchedUris != null
                        ? new HashSet<>(matchedUris) : null;

                    for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                        String field = entry.getKey();
                        List<String> values = entry.getValue();

                        BooleanQuery.Builder filterBuilder = new BooleanQuery.Builder();
                        // Constrain to current remaining URIs if we have them
                        if (remainingUris != null) {
                            List<BytesRef> uriRefs = new ArrayList<>(remainingUris.size());
                            for (String uri : remainingUris) {
                                uriRefs.add(new BytesRef(uri));
                            }
                            filterBuilder.add(new TermInSetQuery(entityField, uriRefs),
                                BooleanClause.Occur.MUST);
                        }

                        // Add filter value constraint
                        if (values.size() == 1) {
                            filterBuilder.add(new TermQuery(new Term(field, values.get(0))),
                                BooleanClause.Occur.MUST);
                        } else {
                            List<BytesRef> valRefs = new ArrayList<>(values.size());
                            for (String v : values) {
                                valRefs.add(new BytesRef(v));
                            }
                            filterBuilder.add(new TermInSetQuery(field, valRefs),
                                BooleanClause.Occur.MUST);
                        }

                        TopDocs filterDocs = searcher.search(filterBuilder.build(), facetSearchLimit());
                        Set<String> filterMatchedUris = new HashSet<>();
                        StoredFields sf = searcher.storedFields();
                        for (ScoreDoc sd : filterDocs.scoreDocs) {
                            Document doc = sf.document(sd.doc);
                            String uri = doc.get(entityField);
                            if (uri != null) {
                                filterMatchedUris.add(uri);
                            }
                        }

                        if (remainingUris != null) {
                            remainingUris.retainAll(filterMatchedUris);
                        } else {
                            remainingUris = filterMatchedUris;
                        }

                        if (remainingUris.isEmpty()) {
                            return result;
                        }
                    }

                    matchedUris = remainingUris;
                }

                if (matchedUris == null) {
                    facets = new SortedSetDocValuesFacetCounts(state);
                } else {
                    List<BytesRef> uriRefs = new ArrayList<>(matchedUris.size());
                    for (String uri : matchedUris) {
                        uriRefs.add(new BytesRef(uri));
                    }
                    Query uriQuery = new TermInSetQuery(entityField, uriRefs);
                    FacetsCollector fc = new FacetsCollector();
                    searcher.search(uriQuery, fc);
                    facets = new SortedSetDocValuesFacetCounts(state, fc);
                }
                } // end triple-per-document else

                // Extract results
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
            } finally {
                indexReader.close();
            }
        } catch (IOException ex) {
            throw new TextIndexException("getFacetCountsWithFilters", ex);
        } catch (ParseException ex) {
            throw new TextIndexParseException(queryString, ex.getMessage());
        }

        return result;
    }

    // ---- CQL-based query methods ----

    /**
     * Query with CQL filter expression, optional sort, in entity-per-document (SHACL) mode.
     * Compiles the CQL expression to a Lucene query and combines with the text query.
     */
    public List<TextHit> queryWithCql(List<Resource> props, String qs,
            CqlExpression cqlFilter, List<SortSpec> sortSpecs,
            String graphURI, String lang, int limit, String highlight) {

        if (cqlFilter == null && (sortSpecs == null || sortSpecs.isEmpty())) {
            return query(props, qs, graphURI, lang, limit, highlight);
        }

        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);

            BooleanQuery.Builder combined = new BooleanQuery.Builder();

            // Text query
            if (qs != null && !qs.isEmpty()) {
                combined.add(parseQuery(qs, queryAnalyzer), BooleanClause.Occur.MUST);
            }

            // CQL filter
            if (cqlFilter != null && shaclMapping != null) {
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
            String entityField = docDef.getEntityField();
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

    /**
     * Get facet counts with CQL filter expression in entity-per-document (SHACL) mode.
     */
    public Map<String, List<FacetValue>> getFacetCountsWithCql(
            String queryString, List<String> facetFieldsToQuery,
            CqlExpression cqlFilter, int maxValues, int minCount) {

        Map<String, List<FacetValue>> result = new HashMap<>();

        if (facetFieldsToQuery == null || facetFieldsToQuery.isEmpty()) {
            return result;
        }

        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            SortedSetDocValuesReaderState state =
                new DefaultSortedSetDocValuesReaderState(indexReader, facetsConfig);

            BooleanQuery.Builder combined = new BooleanQuery.Builder();

            if (queryString != null && !queryString.isEmpty()) {
                combined.add(parseQuery(queryString, queryAnalyzer), BooleanClause.Occur.MUST);
            }

            if (cqlFilter != null && shaclMapping != null) {
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

    /**
     * Count total matching documents for a query with CQL filter.
     */
    public long countQueryWithCql(String queryString, CqlExpression cqlFilter) {
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            if (queryString != null && !queryString.isEmpty()) {
                bq.add(parseQuery(queryString, queryAnalyzer), BooleanClause.Occur.MUST);
            }
            if (cqlFilter != null && shaclMapping != null) {
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

    /**
     * Build a Lucene {@link Sort} from sort specifications using the SHACL mapping
     * to determine field types.
     */
    public Sort buildLuceneSort(List<SortSpec> sortSpecs) {
        if (sortSpecs == null || sortSpecs.isEmpty()) {
            return null;
        }

        SortField[] fields = new SortField[sortSpecs.size()];
        for (int i = 0; i < sortSpecs.size(); i++) {
            SortSpec spec = sortSpecs.get(i);
            SortField.Type sortType = SortField.Type.STRING; // default

            if (shaclMapping != null) {
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
            }

            fields[i] = new SortField(spec.field(), sortType, spec.descending());
        }
        return new Sort(fields);
    }
}
