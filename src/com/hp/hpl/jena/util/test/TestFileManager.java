/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util.test;

import junit.framework.*;
import java.io.* ;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;

import org.apache.commons.logging.*;

/** com.hp.hpl.jena.brql.util.test.TestFileManager
 * 
 * @author Andy Seaborne
 * @version $Id: TestFileManager.java,v 1.3 2005-02-21 12:19:21 andy_seaborne Exp $
 */

public class TestFileManager extends TestCase
{
    static Log log = LogFactory.getLog(TestFileManager.class) ;
    static final String testingDir = "testing/FileManager" ;
    static final String filename = "fmgr-test-file" ; 
    static final String filenameNonExistent = "fmgr-test-file-1421" ;
    static final String zipname = testingDir+"/fmgr-test.zip" ;
    
    public TestFileManager( String name )
    {
        super(name);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite( TestFileManager.class );
    }

    public void testFileManagerFileLocator()
    {
        FileManager fileManager = new FileManager() ;
        fileManager.addLocatorFile() ;
        InputStream in = fileManager.open(testingDir+"/"+filename) ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    public void testFileManagerFileLocatorWithDir()
    {
        FileManager fileManager = new FileManager() ;
        fileManager.addLocatorFile(testingDir) ;
        InputStream in = fileManager.open(filename) ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }


    public void testFileManagerNoFile()
    {
        FileManager fileManager = new FileManager() ;
        fileManager.addLocatorFile() ;
        InputStream in = fileManager.open(filenameNonExistent) ;
        assertNull(in) ;
        closeInputStream(in) ;
    }
    
    public void testFileManagerLocatorClassLoader()
    {
        FileManager fileManager = new FileManager() ;
        fileManager.addLocatorSystemClassLoader() ;
        InputStream in = fileManager.open("java/lang/String.class") ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    public void testFileManagerLocatorClassLoaderNotFound()
    {
        FileManager fileManager = new FileManager() ;
        fileManager.addLocatorSystemClassLoader() ;
        InputStream in = fileManager.open("not/java/lang/String.class") ;
        assertNull(in) ;
        closeInputStream(in) ;
    }

    public void testFileManagerLocatorZip()
    {
        FileManager fileManager = new FileManager() ;
        try {
            fileManager.addLocatorZip(zipname) ;
        } catch (Exception ex)
        {
           fail("Failed to create a filemanager and add a zip locator") ;
        }
        InputStream in = fileManager.open(filename) ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    public void testFileManagerLocatorZipNonFound()
    {
        FileManager fileManager = new FileManager() ;
        try {
            fileManager.addLocatorZip(zipname) ;
        } catch (Exception ex)
        {
           fail("Failed to create a filemanager and add a zip locator") ;
        }
        InputStream in = fileManager.open(filenameNonExistent) ;
        assertNull(in) ;
        closeInputStream(in) ;
    }
    
    
    public void testLocationMappingURLtoFileOpen()
    {
        LocationMapper locMap = new LocationMapper(TestLocationMapper.mapping) ;
        FileManager fileManager = new FileManager(locMap) ;
        fileManager.addLocatorFile() ;
        InputStream in = fileManager.open("http://example.org/file") ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    public void testLocationMappingURLtoFileOpenNotFound()
    {
        LocationMapper locMap = new LocationMapper(TestLocationMapper.mapping) ;
        FileManager fileManager = new FileManager(locMap) ;
        fileManager.addLocatorSystemClassLoader() ;
        InputStream in = fileManager.open("http://example.org/file") ;
        assertNull(in) ;
        closeInputStream(in) ;
    }

    




//    public void testFileManagerLocatorURL()
//    {
//        FileManager fileManager = new FileManager() ;
//        fileManager.addLocatorURL() ;
//        InputStream in = fileManager.open("http:///www.bbc.co.uk/") ;
//        //assertNotNull(in) ;
//        // Proxies matter.
//        if ( in == null )
//            log.warn("Failed to contact http:///www.bbc.co.uk/: maybe due to proxy issues") ;
//        
//        try { if ( in != null ) in.close() ; }
//        catch (Exception ex) {}
//    }

    
    // -------- Helpers
    
    private void closeInputStream(InputStream in)
    {
      try {
          if ( in != null )
              in.close() ;
      }
      catch (Exception ex) {}
    }
}

/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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