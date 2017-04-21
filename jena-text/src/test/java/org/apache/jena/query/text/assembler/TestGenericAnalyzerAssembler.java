package org.apache.jena.query.text.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextIndexLucene;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGenericAnalyzerAssembler {
    
//    // Suppress warnings
//    @BeforeClass public static void beforeClass() { LogCtl.setError(EntityDefinitionAssembler.class); }
//    @AfterClass  public static void afterClass()  { LogCtl.setInfo(EntityDefinitionAssembler.class); }

    private static final String TESTBASE = "http://example.org/test/";
    private static final Resource spec1;
    private static final Resource spec2;
    private static final Resource spec3;
    
    @Test public void AnalyzerNullaryCtor() {
        GenericAnalyzerAssembler gaAssem = new GenericAnalyzerAssembler();
        Analyzer analyzer = gaAssem.open(null, spec1, null);
        assertEquals(SimpleAnalyzer.class, analyzer.getClass());
    }
    
    @Test public void AnalyzerNullaryCtor2() {
        GenericAnalyzerAssembler gaAssem = new GenericAnalyzerAssembler();
        Analyzer analyzer = gaAssem.open(null, spec2, null);
        assertEquals(FrenchAnalyzer.class, analyzer.getClass());
    }
    
    @Test public void AnalyzerCtorSet1() {
        GenericAnalyzerAssembler gaAssem = new GenericAnalyzerAssembler();
        Analyzer analyzer = gaAssem.open(null, spec3, null);
        assertEquals(FrenchAnalyzer.class, analyzer.getClass());
    }
    
    
    private static final String CLASS_SIMPLE = "org.apache.lucene.analysis.core.SimpleAnalyzer";
    private static final String CLASS_FRENCH = "org.apache.lucene.analysis.fr.FrenchAnalyzer";
    
    private static final String PARAM_TYPE_BOOL = "boolean";
    private static final String PARAM_TYPE_FILE = "file";
    private static final String PARAM_TYPE_INT = "int";
    private static final String PARAM_TYPE_SET = "set";
    private static final String PARAM_TYPE_STRING = "string";
    
    static {
        TextAssembler.init();
        Model model = ModelFactory.createDefaultModel();
        
        // analyzer spec w/ no params
                
        spec1 = model.createResource()
                     .addProperty(RDF.type, TextVocab.genericAnalyzer)
                     .addProperty(TextVocab.pClass, CLASS_SIMPLE)
                     ;
        
        // analyzer spec w/ empty params
                
        spec2 = model.createResource()
                     .addProperty(RDF.type, TextVocab.genericAnalyzer)
                     .addProperty(TextVocab.pClass, CLASS_FRENCH)
                     .addProperty(TextVocab.pParams,
                                  model.createList(
                                          new RDFNode[] { } )
                                  )
                     ;
        
        // analyzer spec w/ one set param
                
        spec3 = model.createResource()
                     .addProperty(RDF.type, TextVocab.genericAnalyzer)
                     .addProperty(TextVocab.pClass, CLASS_FRENCH)
                     .addProperty(TextVocab.pParams,
                                  model.createList(
                                          new RDFNode[] { 
                                                  model.createResource()
                                                  .addProperty(TextVocab.pParamName, "stopWords")
                                                  .addProperty(TextVocab.pParamType, PARAM_TYPE_SET)
                                                  .addProperty(TextVocab.pParamValue, strs2list(model, "les le du"))
                                          }))
                     ;
    }
    
    private static Resource strs2list(Model model, String string) {
        String[] members = string.split("\\s");
        Resource current = RDF.nil;
        for (int i = members.length-1; i>=0; i--) {
            Resource previous = current;
            current = model.createResource();
            current.addProperty(RDF.rest, previous);
            current.addProperty(RDF.first, members[i]);            
        }
        return current;    
    }
}
