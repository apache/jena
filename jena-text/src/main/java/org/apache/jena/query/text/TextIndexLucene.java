/**
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

package org.apache.jena.query.text ;

import java.io.IOException ;
import java.util.* ;
import java.util.Map.Entry ;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.core.KeywordAnalyzer ;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper ;
import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.apache.lucene.document.* ;
import org.apache.lucene.index.* ;
import org.apache.lucene.queryparser.classic.ParseException ;
import org.apache.lucene.queryparser.classic.QueryParser ;
import org.apache.lucene.queryparser.classic.QueryParserBase ;
import org.apache.lucene.search.IndexSearcher ;
import org.apache.lucene.search.Query ;
import org.apache.lucene.search.ScoreDoc ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.util.Version ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TextIndexLucene implements TextIndex {
    private static Logger          log      = LoggerFactory.getLogger(TextIndexLucene.class) ;

    private static int             MAX_N    = 10000 ;
    public static final Version    VER      = Version.LUCENE_4_9 ;
    // prefix for storing datatype URIs in the index, to distinguish them from language tags
    private static final String    DATATYPE_PREFIX = "^^";

    public static final FieldType  ftIRI ;
    static {
        ftIRI = new FieldType() ;
        ftIRI.setTokenized(false) ;
        ftIRI.setStored(true) ;
        ftIRI.setIndexed(true) ;
        ftIRI.freeze() ;
    }
    public static final FieldType  ftString = StringField.TYPE_NOT_STORED ;

    private final EntityDefinition docDef ;
    private final Directory        directory ;
    private final Analyzer         analyzer ;
    private final Analyzer         queryAnalyzer ;
    private final FieldType        ftText ;

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

        // create the analyzer as a wrapper that uses KeywordAnalyzer for
        // entity and graph fields and StandardAnalyzer for all other
        Map<String, Analyzer> analyzerPerField = new HashMap<>() ;
        analyzerPerField.put(docDef.getEntityField(), new KeywordAnalyzer()) ;
        if ( docDef.getGraphField() != null )
            analyzerPerField.put(docDef.getGraphField(), new KeywordAnalyzer()) ;

        for (String field : docDef.fields()) {
            Analyzer _analyzer = docDef.getAnalyzer(field);
            if (_analyzer != null) {
                analyzerPerField.put(field, _analyzer);
            }
        }

        this.analyzer = new PerFieldAnalyzerWrapper(
                (null != config.getAnalyzer()) ? config.getAnalyzer() : new StandardAnalyzer(VER), analyzerPerField) ;
        this.queryAnalyzer = (null != config.getQueryAnalyzer()) ? config.getQueryAnalyzer() : this.analyzer ;
        this.ftText = config.isValueStored() ? TextField.TYPE_STORED : TextField.TYPE_NOT_STORED ;
        if (config.isValueStored() && docDef.getLangField() == null)
            log.warn("Values stored but langField not set. Returned values will not have language tag or datatype.");

        openIndexWriter();
    }

    private void openIndexWriter() {
        IndexWriterConfig wConfig = new IndexWriterConfig(VER, analyzer) ;
        try
        {
            indexWriter = new IndexWriter(directory, wConfig) ;
            // Force a commit to create the index, otherwise querying before writing will cause an exception
            indexWriter.commit();
        }
        catch (IOException e)
        {
            throw new TextIndexException(e) ;
        }
    }

    public Directory getDirectory() {
        return directory ;
    }

    public Analyzer getAnalyzer() {
        return analyzer ;
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
            throw new TextIndexException(e);
        }
    }

    @Override
    public void commit() {
        try {
            indexWriter.commit();
        }
        catch (IOException e) {
            throw new TextIndexException(e);
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
            throw new TextIndexException(e);
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
            throw new TextIndexException(ex) ;
        }
    }

    @Override public void updateEntity(Entity entity) {
        if ( log.isDebugEnabled() )
            log.debug("Update entity: " + entity) ;
        try {
            updateDocument(entity);
        } catch (IOException e) {
            throw new TextIndexException(e) ;
        }
    }

    protected void updateDocument(Entity entity) throws IOException {
        Document doc = doc(entity);
        Term term = new Term(docDef.getEntityField(), entity.getId());
        indexWriter.updateDocument(term, doc);
    }

    @Override
    public void addEntity(Entity entity) {
        if ( log.isDebugEnabled() )
            log.debug("Add entity: " + entity) ;
        try {
            addDocument(entity);
        }
        catch (IOException e) {
            throw new TextIndexException(e) ;
        }
    }

    protected void addDocument(Entity entity) throws IOException {
        Document doc = doc(entity) ;
        indexWriter.addDocument(doc) ;
    }

    @Override
    public void deleteEntity(Entity entity) {
        if (docDef.getUidField() == null)
            return;

        if ( log.isDebugEnabled() )
            log.debug("Delete entity: "+entity) ;
        try {
            Map<String, Object> map = entity.getMap();
            String property = map.keySet().iterator().next();
            String value = (String)map.get(property);
            String hash = entity.getChecksum(property, value);
            Term uid = new Term(docDef.getUidField(), hash);
            indexWriter.deleteDocuments(uid);

        } catch (Exception e) {
            throw new TextIndexException(e) ;
        }
    }

    protected Document doc(Entity entity) {
        Document doc = new Document() ;
        Field entField = new Field(docDef.getEntityField(), entity.getId(), ftIRI) ;
        doc.add(entField) ;

        String graphField = docDef.getGraphField() ;
        if ( graphField != null ) {
            Field gField = new Field(graphField, entity.getGraph(), ftString) ;
            doc.add(gField) ;
        }

        String langField = docDef.getLangField() ;
        String uidField = docDef.getUidField() ;

        for ( Entry<String, Object> e : entity.getMap().entrySet() ) {
            doc.add( new Field(e.getKey(), (String) e.getValue(), ftText) );
            if (langField != null) {
                String lang = entity.getLanguage();
                RDFDatatype datatype = entity.getDatatype();
                if (lang != null && !"".equals(lang)) {
                    doc.add(new Field(langField, lang, StringField.TYPE_STORED));
                } else if (datatype != null && !datatype.equals(XSDDatatype.XSDstring)) {
                    // for non-string and non-langString datatypes, store the datatype in langField
                    doc.add(new Field(langField, DATATYPE_PREFIX + datatype.getURI(), StringField.TYPE_STORED));
                }
            }
            if (uidField != null) {
                String hash = entity.getChecksum(e.getKey(), (String) e.getValue());
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
            throw new TextIndexException(ex) ;
        }
    }

    private static Query parseQuery(String queryString, String primaryField, Analyzer analyzer) throws ParseException {
        QueryParser queryParser = new QueryParser(VER, primaryField, analyzer) ;
        queryParser.setAllowLeadingWildcard(true) ;
        Query query = queryParser.parse(queryString) ;
        return query ;
    }
    
    protected Query preParseQuery(String queryString, String primaryField, Analyzer analyzer) throws ParseException {
        return parseQuery(queryString, primaryField, analyzer);
    }

    private List<Map<String, Node>> get$(IndexReader indexReader, String uri) throws ParseException, IOException {
        String escaped = QueryParserBase.escape(uri) ;
        String qs = docDef.getEntityField() + ":" + escaped ;
        Query query = preParseQuery(qs, docDef.getPrimaryField(), queryAnalyzer) ;
        IndexSearcher indexSearcher = new IndexSearcher(indexReader) ;
        ScoreDoc[] sDocs = indexSearcher.search(query, 1).scoreDocs ;
        List<Map<String, Node>> records = new ArrayList<Map<String, Node>>() ;

        // Align and DRY with Solr.
        for ( ScoreDoc sd : sDocs ) {
            Document doc = indexSearcher.doc(sd.doc) ;
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
    public List<TextHit> query(Node property, String qs) {
        return query(property, qs, MAX_N) ;
    }

    @Override
    public List<TextHit> query(Node property, String qs, int limit) {
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            return query$(indexReader, property, qs, limit) ;
        }
        catch (ParseException ex) {
            throw new TextIndexParseException(qs, ex.getMessage()) ;
        }
        catch (Exception ex) {
            throw new TextIndexException(ex) ;
        }
    }

    private List<TextHit> query$(IndexReader indexReader, Node property, String qs, int limit) throws ParseException, IOException {
        IndexSearcher indexSearcher = new IndexSearcher(indexReader) ;
        Query query = preParseQuery(qs, docDef.getPrimaryField(), queryAnalyzer) ;
        if ( limit <= 0 )
            limit = MAX_N ;
        ScoreDoc[] sDocs = indexSearcher.search(query, limit).scoreDocs ;

        List<TextHit> results = new ArrayList<>() ;

        // Align and DRY with Solr.
        for ( ScoreDoc sd : sDocs ) {
            Document doc = indexSearcher.doc(sd.doc) ;
            String[] values = doc.getValues(docDef.getEntityField()) ;

            Node literal = null;
            String field = (property != null) ? docDef.getField(property) : docDef.getPrimaryField();
            String[] lexicals = doc.getValues(field) ;
            if (lexicals.length > 0) {
                String lexical = lexicals[0];
                String[] langs = doc.getValues(docDef.getLangField()) ;
                if (langs.length > 0) {
                    String lang = langs[0];
                    if (lang.startsWith(DATATYPE_PREFIX)) {
                        String datatype = lang.substring(DATATYPE_PREFIX.length());
                        TypeMapper tmap = TypeMapper.getInstance();
                        literal = NodeFactory.createLiteral(lexical, tmap.getSafeTypeByName(datatype));
                    } else {
                        literal = NodeFactory.createLiteral(lexical, lang);
                    }
                } else {
                    literal = NodeFactory.createLiteral(lexical);
                }
            }

            for ( String v : values ) {
                Node n = TextQueryFuncs.stringToNode(v) ;
                TextHit hit = new TextHit(n, sd.score, literal);
                results.add(hit) ;
            }
        }
        return results ;
    }

    @Override
    public EntityDefinition getDocDef() {
        return docDef ;
    }

    private Node entryToNode(String v) {
        // TEMP
        return NodeFactoryExtra.createLiteralNode(v, null, null) ;
    }
}
