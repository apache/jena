package dev ;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.query.larq.ARQLuceneException;
import com.hp.hpl.jena.query.larq.IndexBuilderModel;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexBuilderSubject;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class Damian_2009_01 {

    private static IndexBuilderModel ib;
    
    public static void main(String[] args) throws Exception {
        File indexDir = new File("./test-larq");
        Statement s1 = ResourceFactory.createStatement(RDF.Alt, RDF.first, ResourceFactory.createPlainLiteral("one"));
        Statement s2 = ResourceFactory.createStatement(RDF.Bag, RDF.rest, ResourceFactory.createPlainLiteral("two"));
        
        ib = new IndexBuilderString(indexDir);
        ib.indexStatement(s1);
        ib.closeWriter();
        showSituation(ib.getIndex());
        
        IndexReader indexReader = IndexReader.open(indexDir) ;
        IndexLARQ index = new IndexLARQ(indexReader) ;
        showSituation(index);
    }
    
    private static int count = 1;
    
    public static void showSituation(IndexLARQ index) {
        System.err.printf("(%s)\t%s\t%s\n",
                count++,
                index.hasMatch("one"),
                index.hasMatch("two"));
    }

}
