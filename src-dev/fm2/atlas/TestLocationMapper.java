/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package fm2.atlas;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;

import fm2.TestFileManager ;

/** TestLocationMapper
 * 
 * @author Andy Seaborne
 * @version $Id: TestLocationMapper.java,v 1.1 2009/06/29 18:42:05 andy_seaborne Exp $
 */

public class TestLocationMapper extends TestCase
{
    static Logger log = LoggerFactory.getLogger(TestLocationMapper.class) ;
    static final String testingDir = TestFileManager.testingDir ;
    static final String filename1 = "file:test" ; 
    static final String notFilename = "zzzz" ;
    static final String filename2 = "file:"+testingDir+"/location-mapping-test-file" ;
    public static final String mapping = "location-mapping-test.n3;"+
                                  testingDir+"/location-mapping-test.n3" ;

    
    public TestLocationMapper( String name )
    {
        super(name);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite( TestLocationMapper.class );
    }

    public void testLocationMapper()
    {
        LocationMapper locMap = new LocationMapper(mapping) ;
        String alt = locMap.altMapping(filename1) ;
        assertNotNull(alt) ;
        assertEquals(alt, filename2) ;
    }

    public void testLocationMapperMiss()
    {
        LocationMapper locMap = new LocationMapper(mapping) ;
        String alt = locMap.altMapping(notFilename) ;
        assertNotNull(alt) ;
        assertEquals(alt, notFilename) ;
    }

    public void testLocationMapperURLtoFile()
    {
        LocationMapper locMap = new LocationMapper(mapping) ;
        String alt = locMap.altMapping("http://example.org/file") ;
        assertNotNull(alt) ;
        assertEquals(alt, "file:"+testingDir+"/location-mapping-test-file") ;
    }
    
    public void testLocationMapperFromModel()
    {
        Model model = FileManager.get().loadModel(testingDir+"/location-mapping-test.n3") ;
        LocationMapper loc = new LocationMapper(model) ; 
        
        // Light test that the two location mappers are "the same"
        LocationMapper locMap = new LocationMapper(mapping) ;
        for ( Iterator<String> iter = loc.listAltEntries() ; iter.hasNext() ; )
        {
            String e = iter.next() ;
            String v1 = locMap.getAltEntry(e) ;
            String v2 = loc.getAltEntry(e) ;
            assertEquals("Different entries", v1, v2) ;
        }
        for ( Iterator<String> iter = loc.listAltPrefixes() ; iter.hasNext() ; )
        {
            String e = iter.next() ;
            String v1 = locMap.getAltPrefix(e) ;
            String v2 = loc.getAltPrefix(e) ;
            assertEquals("Different entries", v1, v2) ;
        }
    }

    public void testLocationMapperClone1()
    {
        LocationMapper locMap1 = new LocationMapper(mapping) ;
        // See testLocationMapperURLtoFile
//        String alt = locMap.altMapping("http://example.org/file") ;
//        assertNotNull(alt) ;
//        assertEquals(alt, "file:"+testingDir+"/location-mapping-test-file") ;
        
        LocationMapper locMap2 = new LocationMapper(locMap1) ;
        // Remove from original
        locMap1.removeAltEntry("http://example.org/file") ;
        String alt = locMap2.altMapping("http://example.org/file") ;
        assertNotNull(alt) ;
        assertEquals(alt, "file:"+testingDir+"/location-mapping-test-file") ;
    }
    
    public void testLocationMapperClone2()
    {
        LocationMapper locMap1 = new LocationMapper(mapping) ;
        // See testLocationMapperURLtoFile
//        String alt = locMap.altMapping("http://example.org/file") ;
//        assertNotNull(alt) ;
//        assertEquals(alt, "file:"+testingDir+"/location-mapping-test-file") ;
        
        LocationMapper locMap2 = new LocationMapper(locMap1) ;

        // Change this one
        locMap2.addAltPrefix("http://example.org/OTHER", "file:OTHER") ;
        {
            String alt = locMap2.altMapping("http://example.org/OTHER/f") ;
            assertNotNull(alt) ;
            assertEquals(alt, "file:OTHER/f") ;
        }
        // Not the other
        {
            String alt = locMap1.altMapping("http://example.org/OTHER/f") ;
            assertNotNull(alt) ;
            // Did not change
            assertEquals(alt, "http://example.org/OTHER/f") ;
        }
    }

    public void testLocationMapperEquals1()
    {
        LocationMapper locMap1 = new LocationMapper(mapping) ;
        LocationMapper locMap2 = new LocationMapper(mapping) ;
        assertEquals(locMap1, locMap2) ;
        assertEquals(locMap1.hashCode(), locMap2.hashCode()) ;
    }

    public void testLocationMapperEquals2()
    {
        LocationMapper locMap1 = new LocationMapper(mapping) ;
        LocationMapper locMap2 = new LocationMapper(mapping) ;
        locMap2.addAltEntry("file:nowhere", "file:somewhere") ;
        assertFalse(locMap1.equals(locMap2)) ;
        assertFalse(locMap2.equals(locMap1)) ;
    }

    public void testLocationMapperToModel1()
    {
        LocationMapper locMap1 = new LocationMapper(mapping) ;
        LocationMapper locMap2 = new LocationMapper(locMap1.toModel()) ;
        assertEquals(locMap1, locMap2) ;
        assertEquals(locMap1.hashCode(), locMap2.hashCode()) ;
    }

    public void testLocationMapperToModel2()
    {
        LocationMapper locMap1 = new LocationMapper(mapping) ;
        LocationMapper locMap2 = new LocationMapper(mapping) ;
        locMap1 = new LocationMapper(locMap1.toModel()) ;
        locMap2.addAltEntry("file:nowhere", "file:somewhere") ;
        assertFalse(locMap1.equals(locMap2)) ;
        assertFalse(locMap2.equals(locMap1)) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */