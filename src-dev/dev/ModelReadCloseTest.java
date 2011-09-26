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

package dev ;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This class tests to make sure that Model.read(InputStream) does not call
 * close() on the InputStream handed to it when it is finished.  The creator
 * of the InputStream should be the one to manage its lifecycle.  If the
 * Model does call close(), this can break InputStreams that you can still
 * read more data from after -1 is returned from read().
 * 
 * ZipInputStream is an example of a class with this behavior.  Below is
 * a code snippet that will fail for all Model.read() calls except the
 * Jena N-Triples parser.
 * 
 * ZipInputStream zin = new ZipInputStream(new FileInputStream("file.zip"));
 * ZipEntry ze = null;
 * while ((ze = zin.getNextEntry()) != null)
 * {
 *    String filename = ze.getName();
 *    Model m = ModelFactory.createDefaultModel();
 *    m.read(zin, null, "N-TRIPLES");
 *    
 *    // Closes the current ZIP entry and positions the stream for reading
 *    // the next entry.
 *    zin.closeEntry();
 * }
 * zin.close();
 * 
 * Test Results for Jena 2.6.3 and ARQ 2.8.5
 * -----------------------------------------
 * TestJenaReaderNTriples: PASSED
 *       TestJenaReaderN3: ERROR
 *   TestJenaReaderRDFXML: FAILED
 * TestRIOTReaderNTriples: FAILED
 *       TestRIOTReaderN3: FAILED
 *   TestRIOTReaderRDFXML: FAILED
 * 
 * NOTE: A work-around is to us an InputStream wrapper that delegates all
 *       calls except close(), which does nothing.
 */
public class ModelReadCloseTest
{
   @Test
   public void TestJenaReaderNTriples()
   {
      SysRIOT.resetJenaReaders();
      readNTriples();
   }
      
   @Test
   public void TestJenaReaderN3()
   {
      SysRIOT.resetJenaReaders();
      readN3();
   }
   
   @Test
   public void TestJenaReaderRDFXML()
   {
      SysRIOT.resetJenaReaders();
      readRDFXML();
   }
   
   @Test
   public void TestRIOTReaderNTriples()
   {
      SysRIOT.wireIntoJena();
      readNTriples();
   }
      
   @Test
   public void TestRIOTReaderN3()
   {
      SysRIOT.wireIntoJena();
      readN3();
   }
   
   @Test
   public void TestRIOTReaderRDFXML()
   {
      SysRIOT.wireIntoJena();
      readRDFXML();
   }
   
   
   void readNTriples()
   {
      performRead("<http://example.org/test> <http://www.w3.org/2000/01/rdf-schema#label> \"Test\" .", "N-TRIPLES");
   }
   
   void readN3()
   {
      performRead("<http://example.org/test> <http://www.w3.org/2000/01/rdf-schema#label> \"Test\" .", "N3");
   }
   
   void readRDFXML()
   {
      performRead(
         "<?xml version=\"1.0\"?>" +
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
            "<rdf:Description rdf:about=\"http://example.org/test\" rdfs:label=\"Test\">" +
            "</rdf:Description>" +
            "</rdf:RDF>",
         "RDF/XML");
   }
   
   void performRead(String triples, String lang)
   {
      // Create an InputStream that doesn't like to be closed
      InputStream in = new ByteArrayInputStream(triples.getBytes())
      {
         /**
          * @throws IOException
          * @see java.io.InputStream#close()
          */
         @Override
         public void close() throws IOException
         {
            Assert.fail("Close should not be called.");
         }
      };
      
      Model m = ModelFactory.createDefaultModel();
      m.read(in, null, lang);
   }
}
