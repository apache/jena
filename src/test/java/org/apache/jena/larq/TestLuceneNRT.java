package org.apache.jena.larq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLuceneNRT {

    Directory directory = null;
    IndexWriter writer = null;
    Document doc1 = new Document();
    Document doc2 = new Document();
    
    @Before public void setUp() throws IOException {
//        directory = FSDirectory.open(new File("/tmp/lucene"));
        directory = new RAMDirectory();
        writer = IndexWriterFactory.create(directory);
        writer.commit();
        doc1.add(new Field("foo", "bar1", Field.Store.NO, Field.Index.ANALYZED));
        doc2.add(new Field("foo", "bar2", Field.Store.NO, Field.Index.ANALYZED));
    }
    
    @After public void tearDown() {
        close(writer); writer = null;
        close(directory); directory = null;
        doc1 = null;
        doc2 = null;
    }
    
    @Test public void setUpDone() {
        assertNotNull(directory);
        assertNotNull(writer);
        assertNotNull(doc1);
        assertNotNull(doc2);
    }
    
    @Test public void indexIsEmpty() throws Exception {
        assertEquals(0, writer.numDocs());
        assertEquals(0, writer.numRamDocs());
        assertEquals(0, writer.maxDoc());
        assertFalse(writer.hasDeletions());
    }
    
    @Test public void addDocument() throws Exception {
        writer.addDocument(doc1);
        assertEquals(1, writer.numDocs());
        assertEquals(1, writer.numRamDocs());
        assertEquals(1, writer.maxDoc());
        assertFalse(writer.hasDeletions());
        
        writer.commit();
        assertEquals(1, writer.numDocs());
        assertEquals(0, writer.numRamDocs());
        assertEquals(1, writer.maxDoc());
        assertFalse(writer.hasDeletions());
    }
    
    @Test public void deleteAllDocuments() throws Exception {
        writer.addDocument(doc1);
        assertEquals(1, writer.numDocs());
        assertEquals(1, writer.numRamDocs());
        assertEquals(1, writer.maxDoc());
        assertFalse(writer.hasDeletions());
        
        writer.deleteAll();
        assertEquals(0, writer.numDocs());
        assertEquals(0, writer.numRamDocs());
        assertEquals(0, writer.maxDoc());
        assertFalse(writer.hasDeletions());
    }
    
    @Test public void indexReaderOpenDirectory() throws Exception {
        IndexReader reader = IndexReader.open(directory);
        assertNotNull(reader);
        assertSame(directory, reader.directory());
        assertEquals(1, reader.getRefCount());
        assertTrue(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        close(reader);
        assertEquals(0, reader.getRefCount());
    }        
    
    @Test public void indexReaderOpenDirectoryAddDocument() throws Exception {
        IndexReader reader = IndexReader.open(directory);
        writer.addDocument(doc1);
        assertTrue(reader.isCurrent());
        assertEquals(1, writer.maxDoc());
        assertEquals(0, reader.maxDoc()); 
    }

    @Test public void indexReaderOpenDirectoryAddDocumentAndCommit() throws Exception {
        IndexReader reader = IndexReader.open(directory);
        writer.addDocument(doc1);
        writer.commit();
        assertFalse(reader.isCurrent());
        assertEquals(1, writer.maxDoc());
        assertEquals(0, reader.maxDoc());
    }

    @Test public void indexReaderOpenDirectoryDeleteDocumentAndCommit() throws Exception {
        Term term = new Term ("foo", "bar1");
        IndexReader reader = IndexReader.open(directory);
        writer.addDocument(doc1);
        writer.addDocument(doc2);
        writer.commit();
        
        assertEquals(2, writer.maxDoc());
        assertFalse(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        assertEquals(0, count(reader, term));
        
        reader = TestLARQUtils.openIfChanged(reader);
        assertTrue(reader.isCurrent());
        assertEquals(1, count(reader, term));
        
        writer.deleteDocuments(term);
        assertEquals(2, writer.maxDoc());
        assertEquals(1, count(reader, term));

        reader = TestLARQUtils.openIfChanged(reader);
        assertEquals(1, count(reader, term));
        
        writer.commit();
        reader = TestLARQUtils.openIfChanged(reader);
        assertEquals(2, writer.maxDoc());
        assertEquals(0, count(reader, term));

        reader = TestLARQUtils.openIfChanged(reader);
        assertEquals(0, count(reader, term));
        
        writer.expungeDeletes();
        assertEquals(1, writer.maxDoc());
        assertEquals(0, count(reader, term));
    }
    
    @Test public void indexReaderOpenDirectoryReopen() throws Exception {
        IndexReader reader = IndexReader.open(directory);
        writer.addDocument(doc1);
        writer.commit();

        assertFalse(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        reader = TestLARQUtils.openIfChanged(reader);
        assertTrue(reader.isCurrent());
        assertEquals(1, reader.maxDoc());

        reader = IndexReader.open(directory);
        assertTrue(reader.isCurrent());
        assertEquals(1, reader.maxDoc());
    }
    
    @Test public void indexReaderOpenDirectoryNewInstance() throws Exception {
        IndexReader reader = IndexReader.open(directory);
        writer.addDocument(doc1);
        writer.commit();

        assertFalse(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        reader = IndexReader.open(directory);
        assertTrue(reader.isCurrent());
        assertEquals(1, reader.maxDoc());
    }
    
    @Test public void indexReaderOpenWriter() throws Exception {
        IndexReader reader = IndexReader.open(writer, true);
        assertNotNull(reader);
        assertSame(directory, reader.directory());
        assertEquals(1, reader.getRefCount());
        assertTrue(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        close(reader);
        assertEquals(0, reader.getRefCount());
    }
    
    @Test public void indexReaderOpenWriterAddDocument() throws Exception {
        IndexReader reader = IndexReader.open(writer, true);
        writer.addDocument(doc1);
        assertFalse(reader.isCurrent());
        assertEquals(1, writer.maxDoc());
        assertEquals(0, reader.maxDoc());
    }
    
    @Test public void indexReaderOpenWriterAddDocumentAndCommit() throws Exception {
        IndexReader reader = IndexReader.open(writer, true);
        writer.addDocument(doc1);
        writer.commit();
        assertFalse(reader.isCurrent());
        assertEquals(1, writer.maxDoc());
        assertEquals(0, reader.maxDoc());
    }
    
    @Test public void indexReaderOpenWriterReopen() throws Exception {
        IndexReader reader = IndexReader.open(writer, true);
        writer.addDocument(doc1);
        writer.commit();

        assertFalse(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        reader = TestLARQUtils.openIfChanged(reader);
        assertTrue(reader.isCurrent());
        assertEquals(1, reader.maxDoc());
    }
    
    @Test public void indexReaderOpenWriterNewInstance() throws Exception {
        IndexReader reader = IndexReader.open(writer, true);
        writer.addDocument(doc1);
        writer.commit();

        assertFalse(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        reader = IndexReader.open(writer, true);
        assertTrue(reader.isCurrent());
        assertEquals(1, reader.maxDoc());
    }
    
    @Test public void indexReaderOpenWriterDeleteDocumentAndCommit() throws Exception {
        Term term = new Term ("foo", "bar1");
        IndexReader reader = IndexReader.open(writer, false);
        writer.addDocument(doc1);
        writer.addDocument(doc2);
        writer.commit();
        
        assertEquals(2, writer.maxDoc());
        assertFalse(reader.isCurrent());
        assertEquals(0, reader.maxDoc());
        assertEquals(0, count(reader, term));
        
        reader = TestLARQUtils.openIfChanged(reader);
        assertTrue(reader.isCurrent());
        assertEquals(1, count(reader, term));
        
        writer.deleteDocuments(term);
        assertEquals(2, writer.maxDoc());
        assertEquals(1, count(reader, term));

        reader = TestLARQUtils.openIfChanged(reader);
        assertEquals(1, count(reader, term));
        
        writer.commit();
        reader = TestLARQUtils.openIfChanged(reader);
        assertEquals(2, writer.maxDoc());
        assertEquals(0, count(reader, term));

        reader = TestLARQUtils.openIfChanged(reader);
        assertEquals(0, count(reader, term));
        
        writer.expungeDeletes();
        assertEquals(1, writer.maxDoc());
        assertEquals(0, count(reader, term));
    }
    
    private int count (IndexReader reader, Term term) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs hits = searcher.search(new TermQuery(term), 10);
        return hits.totalHits;
    }
    
    private void close ( Closeable closeable ) {
        if ( closeable != null ) {
            try { closeable.close(); } 
            catch (IOException e) { e.printStackTrace(); }
        }
    }

}
