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

package com.hp.hpl.jena.util.junit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.*;
//import com.hp.hpl.jena.sparql.vocabulary.TestManifest;
//import com.hp.hpl.jena.sparql.vocabulary.TestManifestX;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.TestManifest;
import com.hp.hpl.jena.vocabulary.TestManifestX;

/**
 * A test manifest for a single manifest file.
 */

public class Manifest
{
    // This class does not know about JUnit.
    private static Logger log = LoggerFactory.getLogger(Manifest.class) ;
    Model manifest ;
    String manifestName ;
    String filename ;
    List<String> includedFiles = new ArrayList<>() ;
    Resource manifestRes = null ;

    
    @SuppressWarnings("deprecation")
    public Manifest(String fn)
    {
        log.debug("Manifest = "+fn ) ;
        filename = com.hp.hpl.jena.n3.IRIResolver.resolveGlobal(fn) ;
        log.debug("         = "+filename ) ;
        manifest = FileManager.get().loadModel(filename) ;
        parseIncludes() ;
        parseManifest() ;
    }
    
    public String getName() { return manifestName ; } 
    
    public Iterator<String> includedManifests() { return includedFiles.iterator() ; }

    private void parseManifest()
    {
        StmtIterator manifestStmts =
            manifest.listStatements(null, RDF.type, TestManifest.Manifest);
        if ( !manifestStmts.hasNext() )
        {
            log.warn("No manifest in manifest file: "+filename) ;
            return ; 
        }

        Statement manifestItemStmt = manifestStmts.nextStatement();
        if ( manifestStmts.hasNext() )
        {
            log.warn("Multiple manifests in manifest file: "+filename) ;
            return ; 
        }
        
        manifestRes = manifestItemStmt.getSubject();
        manifestName = TestUtils.getLiteral(manifestRes, RDFS.label) ;
        if ( manifestName == null )
            manifestName = TestUtils.getLiteral(manifestRes, RDFS.comment) ;
        if ( manifestName == null )
            manifestName = TestUtils.getLiteral(manifestRes, TestManifest.name) ;
        manifestStmts.close();
    }
    
    // For every test item (does not recurse)
    public void apply(ManifestItemHandler gen)
    {
        
        StmtIterator manifestStmts =
            manifest.listStatements(null, RDF.type, TestManifest.Manifest);
        
        for (; manifestStmts.hasNext();)
        {
            Statement manifestItemStmt = manifestStmts.nextStatement();
            Resource manifestRes = manifestItemStmt.getSubject();
            
            // For each item in this manifest
            StmtIterator listIter = manifestRes.listProperties(TestManifest.entries);
            for (; listIter.hasNext();)
            {
                //List head
                Resource listItem = listIter.nextStatement().getResource();
                for (; !listItem.equals(RDF.nil);)
                {
                    Resource entry = listItem.getRequiredProperty(RDF.first).getResource();
                    String testName = TestUtils.getLiteral(entry, TestManifest.name) ;
                    Resource action = TestUtils.getResource(entry, TestManifest.action) ;
                    Resource result = TestUtils.getResource(entry, TestManifest.result) ;
                    gen.processManifestItem(manifestRes, entry, testName, action, result) ;
                    // Move to next list item
                    listItem = listItem.getRequiredProperty(RDF.rest).getResource();
                }
            }
            listIter.close();
        }
        manifestStmts.close();
    }

    // -------- included manifests
    private void parseIncludes()
    {
        parseIncludes(TestManifest.include) ;
        parseIncludes(TestManifestX.include) ;
    }
    
    private void parseIncludes(Property property)
    {
        StmtIterator includeStmts = 
            manifest.listStatements(null, property, (RDFNode)null) ;
        
        for (; includeStmts.hasNext(); )
        {
            Statement s = includeStmts.nextStatement() ;
            if ( ! ( s.getObject() instanceof Resource ) )
            {
                log.warn("Include: not a Resource"+s) ;
                continue ;
            }
            Resource r = s.getResource() ;
            parseOneIncludesList(r) ;
        }
        includeStmts.close() ;
    }
    
    private void parseOneIncludesList(Resource r)
    {
        if ( r == null )
            return ;
        
        if ( r.equals(RDF.nil) )
            return ;
       
        
        if ( ! r.isAnon() )
        {
            String uri = r.getURI() ;
            if ( includedFiles.contains(uri) )
                return ;
            includedFiles.add(r.getURI()) ;
            return ;
        }
        
        // BNnode => list
        Resource listItem = r ;
        while(!listItem.equals(RDF.nil))
        {
            r = listItem.getRequiredProperty(RDF.first).getResource();
            parseOneIncludesList(r) ;
            // Move on
            listItem = listItem.getRequiredProperty(RDF.rest).getResource();
        }
    }


}
