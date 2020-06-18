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

package org.apache.jena.riot.adapters;

import java.io.InputStream ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.ontology.Individual ;
import org.apache.jena.ontology.OntModel ;
import org.apache.jena.ontology.OntModelSpec ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.stream.TestLocationMapper;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.shared.NotFoundException ;
import org.apache.jena.util.FileManager ;
import org.apache.jena.util.LocationMapper ;
import org.junit.Test ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

@SuppressWarnings("deprecation")
public class TestFileManager extends BaseTest
{
    static Logger log = LoggerFactory.getLogger(TestFileManager.class) ;
    public static final String testingDir = "testing/RIOT/FileManager" ;
    static final String filename = "fmgr-test-file" ; 
    static final String filenameNonExistent = "fmgr-test-file-1421" ;
    static final String fileModel = "foo.ttl" ;
    static final String zipname = testingDir+"/fmgr-test.zip" ;
    
    @Test public void testFileManagerFileLocator() {
        FileManager fileManager = FileManager.create();
        fileManager.addLocatorFile() ;
        InputStream in = fileManager.open(testingDir+"/"+filename) ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    @Test public void testFileManagerFileLocatorWithDir() {
        FileManager fileManager = FileManager.create() ;
        fileManager.addLocatorFile(testingDir) ;
        InputStream in = fileManager.open(filename) ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    @Test public void testFileManagerNoFile() {
        FileManager fileManager = FileManager.create() ;
        fileManager.addLocatorFile() ;
        try {
            // Tests either way round - exception or a null return.
            InputStream in = fileManager.open(filenameNonExistent) ;
            closeInputStream(in) ;
            assertNull("Found non-existant file: "+filenameNonExistent, in) ;
        } catch (NotFoundException ex) {}
    }
    
    @Test(expected = NotFoundException.class)
    public void testFileManagerNoFile2() {
        FileManager fileManager = FileManager.create() ;
        fileManager.addLocatorFile() ;
        fileManager.readModelInternal(ModelFactory.createDefaultModel(), filenameNonExistent);
    }
    
    @Test(expected = NotFoundException.class)
    public void testFileManagerNoFile3() {
        FileManager fileManager = new AdapterFileManager(new StreamManager(), new org.apache.jena.riot.system.stream.LocationMapper()) ;
        fileManager.addLocatorFile() ;
        fileManager.readModelInternal(ModelFactory.createDefaultModel(), filenameNonExistent);
    }
    
    @Test public void testFileManagerLocatorClassLoader() {
        FileManager fileManager = FileManager.create() ;
        fileManager.addLocatorClassLoader(fileManager.getClass().getClassLoader()) ;
        InputStream in = fileManager.open("java/lang/String.class") ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    @Test public void testFileManagerLocatorClassLoaderNotFound() {
        FileManager fileManager = FileManager.create() ;
        fileManager.addLocatorClassLoader(fileManager.getClass().getClassLoader()) ;
        try {
            InputStream in = fileManager.open("not/java/lang/String.class") ;
            closeInputStream(in) ;
            assertNull("Found non-existant class", in) ;
        } catch (NotFoundException ex) {}
    }

    @Test public void testFileManagerLocatorZip() {
        FileManager fileManager = FileManager.create() ;
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

    @Test public void testFileManagerLocatorZipNonFound() {
        FileManager fileManager = FileManager.create() ;
        try {
            fileManager.addLocatorZip(zipname) ;
        } catch (Exception ex)
        { fail("Failed to create a filemanager and add a zip locator") ; }
        try {
            InputStream in = fileManager.open(filenameNonExistent) ;
            closeInputStream(in) ;
            assertNull("Found non-existant zip file member", in) ;
        } catch (NotFoundException ex) {}
    }
    
    @Test public void testFileManagerClone() {
        FileManager fileManager1 = FileManager.create() ;
        FileManager fileManager2 = fileManager1.clone() ;
        
        // Should not affect fileManager2
        fileManager1.addLocatorFile() ;
        {
            InputStream in = fileManager1.open(testingDir+"/"+filename) ;
            assertNotNull(in) ;
            closeInputStream(in) ;
        }
        // Should not work.
        try {
            InputStream in = fileManager2.open(testingDir+"/"+filename) ;
            closeInputStream(in) ;
            assertNull("Found file via wrong FileManager", in) ;
        } catch (NotFoundException ex) {}
    }
    
    @Test public void testFileManagerReadOntModel() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM) ;
        FileManager.getInternal().readModelInternal(model, testingDir+"/data.ttl") ;
        // Check
        Individual ind = model.getIndividual("http://example.com/individual") ;
        String t = ind.getOntClass().getURI() ;
        assertEquals("http://example.com/T", t) ;
        long c1 = model.size() ;
        
        model.loadImports();

        long c2 = model.size() ;
        assertEquals(c1,c2) ;
    }

    
    @Test public void testLocationMappingURLtoFileOpen() {
        LocationMapper locMap = new LocationMapper(TestLocationMapper.mapping) ;
        FileManager fileManager = FileManager.create(locMap) ;
        fileManager.addLocatorFile() ;
        InputStream in = fileManager.open("http://example.org/file") ;
        assertNotNull(in) ;
        closeInputStream(in) ;
    }

    @Test public void testLocationMappingURLtoFileOpenNotFound() {
        LocationMapper locMap = new LocationMapper(TestLocationMapper.mapping) ;
        FileManager fileManager = FileManager.create(locMap) ;
        fileManager.addLocatorClassLoader(fileManager.getClass().getClassLoader()) ;
        try {
            InputStream in = fileManager.open("http://example.org/file") ;
            closeInputStream(in) ;
            assertNull("Found nont-existant URL", null) ;
        } catch (NotFoundException ex) {}
    }

    @Test public void testCache1() {
        FileManager fileManager = FileManager.create() ;
        fileManager.addLocatorFile(testingDir) ;
        Model m1 = fileManager.loadModelInternal(fileModel) ;
        Model m2 = fileManager.loadModelInternal(fileModel) ;
        assertNotSame(m1, m2) ;
    }
    
    @Test public void testCache2() {
        FileManager.setGlobalFileManager(AdapterFileManager.get()) ;
        
        FileManager fileManager = FileManager.getInternal() ;
        fileManager.addLocatorFile(testingDir) ;
        fileManager.setModelCaching(true) ;
        Model m1 = fileManager.loadModelInternal(fileModel) ;
        Model m2 = fileManager.loadModelInternal(fileModel) ;
        assertSame(m1, m2) ;
    }
    
    @Test public void testCache3() {
        FileManager fileManager = FileManager.getInternal() ;
        fileManager.addLocatorFile(testingDir) ;
        fileManager.setModelCaching(true) ;
        Model m1 = fileManager.loadModelInternal(fileModel) ;
        Model m2 = fileManager.loadModelInternal(fileModel) ;
        assertSame(m1, m2) ;
        
        fileManager.removeCacheModel(fileModel) ;
        Model m3 = fileManager.loadModelInternal(fileModel) ;
        assertNotSame(m1, m3) ;
        
        fileManager.resetCache() ;
        Model m4 = fileManager.loadModelInternal(fileModel) ;
        Model m5 = fileManager.loadModelInternal(fileModel) ;

        assertSame(m4, m5) ;
        assertNotSame(m1, m4) ;
        assertNotSame(m3, m4) ;
    }
    
    // -------- Helpers
    
    private void closeInputStream(InputStream in) {
        try {
            if ( in != null )
                in.close();
        }
        catch (Exception ex) {}
    }
}
