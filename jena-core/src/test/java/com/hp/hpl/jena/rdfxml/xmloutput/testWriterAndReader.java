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

package com.hp.hpl.jena.rdfxml.xmloutput;

import java.io.* ;
import java.util.Random ;
import java.util.Vector ;

import junit.framework.Test ;
import junit.framework.TestSuite ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.vocabulary.RDFSyntax ;

/**
 * This will test any Writer and Reader pair.
 * It writes out a random model, and reads it back in.
 * The test fails if the models are not 'the same'.
 * Quite what 'the same' means is debatable.
 */
public class testWriterAndReader 
    extends ModelTestBase implements RDFErrorHandler {
	static private boolean showProgress = false;
	//static private boolean errorDetail = false;
	static private int firstTest = 4;
	static private int lastTest = 9;
	static private int repetitionsJ = 6;
    
  protected static Logger logger = LoggerFactory.getLogger( testWriterAndReader.class );
    
	final String lang;
   
	final int fileNumber;

    final int options;
    
    String test;

    testWriterAndReader( String name, String lang, int fileNumber ) 
        { this( name, lang, fileNumber, 0 ); }
    
	testWriterAndReader(String name, String lang, int fileNumber, int options) {
		super( name );
		this.lang = lang;
		this.fileNumber = fileNumber;
		this.options = options;
	}
    
	@Override
    public String toString() {
		return getName()
			+ " "
			+ lang
			+ " t"
			+ fileNumber
			+ "000.rdf"
			+ (options != 0 ? ("[" + options + "]") : "");
	}
    
	static Test suiteN_TRIPLE()
        { return baseSuite( "N-TRIPLE" ); }
    
    static TestSuite suiteXML()
        { 
        TestSuite baseTests = baseSuite( "RDF/XML" );
        baseTests.addTestSuite( TestXMLFeatures_XML.class );
        baseTests.addTest( addXMLtests( "RDF/XML", false ) );
        return baseTests; 
        }
    
    static Test suiteXML_ABBREV()
        { 
        TestSuite suite = baseSuite( "RDF/XML-ABBREV" );
        suite.addTestSuite( TestXMLFeatures_XML_ABBREV.class );
        suite.addTestSuite( TestXMLAbbrev.class );
        suite.addTest( addXMLtests( "RDF/XML-ABBREV", false ) );
        return suite; 
        }
    
    public static TestSuite repeatedAbbrevSuite()
        { 
        TestSuite suite = baseSuite( "RDF/XML-ABBREV" );
        suite.addTestSuite( TestXMLFeatures_XML_ABBREV.class );
        suite.addTestSuite( TestXMLAbbrev.class );
        suite.addTest( addXMLtests( "RDF/XML-ABBREV", true ) );
        return suite; 
        }

    static TestSuite baseSuite( String lang ) 
        {
        TestSuite langsuite = new TestSuite();
        langsuite.setName( lang );
        langsuite.addTest( new TestWriterInterface( "testWriting", lang ) );
        langsuite.addTest( new TestWriterInterface( "testLineSeparator", lang ) );
        return langsuite;
        }
    
    public static class TestXMLFeatures_XML extends TestXMLFeatures
        {
        public TestXMLFeatures_XML( String name )
            { super( name, "RDF/XML" ); }
        }
    
    public static class TestXMLFeatures_XML_ABBREV extends TestXMLFeatures
        {
        public TestXMLFeatures_XML_ABBREV( String name )
            { super( name, "RDF/XML-ABBREV" ); }
        }
    
	static private boolean nBits( int i, int [] ok ) 
        {
		int bitCount = 0;
		while (i > 0) 
            {
			if ((i & 1) == 1) bitCount += 1;
			i >>= 1;
            }
            for ( int anOk : ok )
            {
                if ( bitCount == anOk )
                {
                    return true;
                }
            }
		return false;
        }
    
    private static TestSuite addXMLtests( String lang, boolean lots )
        {
        TestSuite suite = new TestSuite();
        int optionLimit = (lang.equals( "RDF/XML-ABBREV" ) ? 1 << blockRules.length : 2);
        for (int fileNumber = firstTest; fileNumber <= lastTest; fileNumber++) 
            {
        	suite.addTest(new testWriterAndReader("testRandom", lang, fileNumber ) );
        	suite.addTest( new testWriterAndReader( "testLongId", lang, fileNumber ) );
            for (int optionMask = 1; optionMask < optionLimit; optionMask += 1) 
                {
        		if (lots || nBits( optionMask, new int[] { 1, /* 2,3,4,5, */ 6,7 } ))
        			suite.addTest( createTestOptions( lang, fileNumber, optionMask ) );
                }
            }
        return suite;
        }

    private static testWriterAndReader createTestOptions( String lang, int fileNumber, int optionMask )
        {
        return new testWriterAndReader( "testOptions " + fileNumber + " " + optionMask, lang, fileNumber, optionMask ) 
            {
            @Override
            public void runTest() throws IOException { testOptions(); }
            };
        }

	public void testRandom() throws IOException 
        {
		doTest( new String[] {}, new Object[] {} );
        }
    
	public void testLongId() throws IOException 
        {
		doTest( new String[] {"longId"}, new Object[] {Boolean.TRUE} );
        }
    
	static Resource [] blockRules =
		{
		RDFSyntax.parseTypeLiteralPropertyElt,
		RDFSyntax.parseTypeCollectionPropertyElt,
		RDFSyntax.propertyAttr,
		RDFSyntax.sectionReification,
		RDFSyntax.sectionListExpand,
		RDFSyntax.parseTypeResourcePropertyElt,
        };
    
	public void testOptions() throws IOException 
        {
		Vector<Resource> v = new Vector<>();
		for (int i = 0; i < blockRules.length; i += 1) 
            {
			if ((options & (1 << i)) != 0) v.add( blockRules[i] );
            }
		Resource blocked[] = new Resource[v.size()];
		v.copyInto( blocked );
		doTest( new String[] { "blockRules" }, new Resource[][] { blocked } );
        }
    
	public void doTest( String[] propNames, Object[] propVals ) throws IOException 
        {
		test( lang, 35, 1, propNames, propVals );
        }

	static final String baseUris[] =
		{
		"http://foo.com/Hello",
        };
            
    ByteArrayOutputStream tmpOut;
    
	/**
	 * @param rwLang Use Writer for this lang
	 * @param seed  A seed for the random number generator
	 * @param variationMax Number of random variations
	 * @param wopName  Property names to set on Writer
	 * @param wopVal   Property values to set on Writer
	 */
	public void test(
		String rwLang,
		int seed,
		int variationMax,
		String[] wopName,
		Object[] wopVal)
		throws IOException {

		Model m1 = createMemModel();
		test = "testWriterAndReader lang=" + rwLang + " seed=" + seed;
		String filebase = "testing/regression/testWriterAndReader/";
		if (showProgress)
			System.out.println("Beginning " + test);
		Random random = new Random(seed);

        RDFReader rdfRdr = m1.getReader( rwLang );
		RDFWriter rdfWtr = m1.getWriter( rwLang );

		setWriterOptionsAndHandlers( wopName, wopVal, rdfRdr, rdfWtr );
		for (int variationIndex = 0; variationIndex < variationMax; variationIndex++) 
			testVariation( filebase, random, rdfRdr, rdfWtr );
		if (showProgress)
			System.out.println("End of " + test);
	}
    
    /**
     	@param wopName
     	@param wopVal
     	@param rdfRdr
     	@param rdfWtr
    */
    private void setWriterOptionsAndHandlers( String[] wopName, Object[] wopVal, RDFReader rdfRdr, RDFWriter rdfWtr )
        {
        rdfRdr.setErrorHandler( this );
        rdfWtr.setErrorHandler( this );
		if (wopName != null)
			for (int i = 0; i < wopName.length; i++)
				rdfWtr.setProperty( wopName[i], wopVal[i] );
        }
    
    /**
     	@param filebase
     	@param random
     	@param rdfRdr
     	@param rdfWtr
     	@throws FileNotFoundException
     	@throws IOException
    */
    private void testVariation( String filebase, Random random, RDFReader rdfRdr, RDFWriter rdfWtr ) 
        throws FileNotFoundException, IOException
        {
        Model m1 = createMemModel();
        Model m2;
        String fileName = "t" + (fileNumber * 1000) + ".rdf";
        String baseUriRead;
        if (fileNumber < baseUris.length)
        	baseUriRead = baseUris[fileNumber];
        else
        	baseUriRead = "http://foo.com/Hello";
        try ( InputStream rdr = new FileInputStream( filebase + fileName ) ) {
            m1.read(rdr, baseUriRead);
        }
        for (int j = 0; j < repetitionsJ; j++) {

            String baseUriWrite =
        		j % 2 == 0 ? baseUriRead : "http://bar.com/irrelevant";
        	int cn = (int) m1.size();
        	if ((j % 2) == 0 && j > 0)
        		prune(m1, random, 1 + cn / 10);
        	if ((j % 2) == 0 && j > 0)
        		expand(m1, random, 1 + cn / 10);
            
            tmpOut = new ByteArrayOutputStream() ;
            rdfWtr.write(m1, tmpOut, baseUriWrite);
            tmpOut.flush() ;
            tmpOut.close() ;
        	m2 = createMemModel();
        	//empty(m2);
            
            try ( InputStream in = new ByteArrayInputStream( tmpOut.toByteArray() ) ) {
                rdfRdr.read(m2, in, baseUriWrite);
            }
        	Model s1 = m1;
        	Model s2 = m2;
        	/*
        	System.err.println("m1:");
        	m1.write(System.err,"N-TRIPLE");
        	System.err.println("m2:");
        	
        	m2.write(System.err,"N-TRIPLE");
        	System.err.println("=");
        	*/
//				assertTrue(
//                        "Comparison of file written out, and file read in.",
//                        s1.isIsomorphicWith(s2));
            assertIsoModels( "Comparison of file written out, and file read in.", s1, s2 );
            // Free resources explicitily.
            tmpOut.reset() ;
            tmpOut = null ;
        }
        if (showProgress) {
        	System.out.print("+");
        	System.out.flush();
        }
        }
    
  static boolean linuxFileDeleteErrorFlag = false;
  
	/**Deletes count edges from m chosen by random.
	 * @param count The number of statements to delete.
	 * @param m A model with more than count statements.
	 */
	private void prune(Model m, Random random, int count)  {
		//    System.out.println("Pruning from " + (int)m.size() + " by " + cnt );
		Statement toRemove[] = new Statement[count];
		int sz = (int) m.size();
		StmtIterator ss = m.listStatements();
		try {
			for (int i = 0; i < count; i++)
				toRemove[i] = ss.nextStatement();
			while (ss.hasNext()) {
				int ix = random.nextInt(sz);
				if (ix < count)
					toRemove[ix] = ss.nextStatement();
			}
		} finally {
			ss.close();
		}
		for (int i = 0; i < count; i++)
			m.remove( toRemove[i] );
		//    System.out.println("Reduced to " + (int)m.size()  );
	}
    
	/**
	 *  Adds count edges to m chosen by random.
	 *
	 * @param count The number of statements to add.
	 * @param m A model with more than cnt statements.
	 */
	private void expand(Model m, Random random, int count)  {
		// System.out.println("Expanding from " + (int)m.size() + " by " + cnt );
		Resource subject[] = new Resource[count];
		Property predicate[] = new Property[count];
		RDFNode object[] = new RDFNode[count];
		int sz = (int) m.size();
		StmtIterator ss = m.listStatements();
		try {
			for (int i = 0; i < count; i++) {
				Statement s = ss.nextStatement();
				subject[i] = s.getSubject();
				predicate[i] = s.getPredicate();
				object[i] = s.getObject();
			}
			while (ss.hasNext()) {
				Statement s = ss.nextStatement();
				Resource subj = s.getSubject();
				RDFNode obj = s.getObject();
				int ix = random.nextInt(sz);
				if (ix < count)
					subject[ix] = subj;
				ix = random.nextInt(sz);
				if (ix < count)
					object[ix] = subj;
				ix = random.nextInt(sz);
				if (ix < count)
					predicate[ix] = s.getPredicate();
				ix = random.nextInt(sz);
				if (ix < count)
					object[ix] = obj;
				if (obj instanceof Resource) {
					ix = random.nextInt(sz);
					if (ix < count)
						subject[ix] = (Resource) obj;
				}
			}
		} finally {
			ss.close();
		}
		for (int i = 0; i < count; i++)
			m.add(subject[i], predicate[i], object[i]);
		//   System.out.println("Expanded to " + (int)m.size()  );
	}

	/** report a warning
	 * @param e an exception representing the error
	 */
	@Override
    public void warning(Exception e) {
//		logger.warn( toString() + " " + e.getMessage(), e );
        System.out.println(new String(tmpOut.toString()));
        
		throw new JenaException( e );
	}
    
	@Override
    public void error(Exception e) {
		fail(e.getMessage());
	}

	@Override
    public void fatalError(Exception e) {
		error(e);
		throw new JenaException(e);
	}

}
