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

package org.apache.jena.arq.junit.manifest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.jena.vocabulary.TestManifest ;
import org.apache.jena.vocabulary.TestManifestX ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "Manifest" is the includes and entries for a single file.
 */
public class Manifest
{
    private static Logger log = LoggerFactory.getLogger(Manifest.class) ;
    private Model manifest ;
    private String manifestName ;
    private String filename ;
    private List<String> includedFiles = new ArrayList<>() ;
    private List<ManifestEntry> entries = new ArrayList<>() ;
    private Resource manifestRes = null ;

    public static Manifest parse(String manifestFile) {
        Manifest manifest = new Manifest(manifestFile);
        return manifest;
    }

    public static void walk(Manifest manifest, Consumer<Manifest> actionManifest, Consumer<ManifestEntry> actionEntry) {
        actionManifest.accept(manifest);
        Iterator<String> sub = manifest.includedManifests();
        while(sub.hasNext() ) {
            String mf = sub.next();
            Manifest manifestSub = Manifest.parse(mf);
            walk(manifestSub, actionManifest, actionEntry);
        }
        for ( ManifestEntry entry : manifest.entries() ) {
            actionEntry.accept(entry);
        }
    }

    private Manifest(String fn) {
        filename = IRILib.filenameToIRI(fn) ;
        manifest = RDFDataMgr.loadModel(filename) ;
        parseManifest() ;
        parseIncludes() ;
        parseEntries() ;
    }

    public String getName()     { return manifestName ; }
    public String getFileName() { return filename ; }

    public Iterator<String> includedManifests() { return includedFiles.iterator() ; }

    public List<ManifestEntry> entries() { return entries ; }

    private void parseManifest()
    {
        StmtIterator manifestStmts = manifest.listStatements(null, RDF.type, TestManifest.Manifest);
        if ( !manifestStmts.hasNext() ) {
            log.warn("No manifest in manifest file: "+filename) ;
            return ;
        }

        Statement manifestItemStmt = manifestStmts.nextStatement();
        if ( manifestStmts.hasNext() ) {
            log.warn("Multiple manifests in manifest file: "+filename) ;
            return ;
        }

        manifestRes = manifestItemStmt.getSubject();
        manifestName = getLiteral(manifestRes, RDFS.label) ;
        if ( manifestName == null )
            manifestName = getLiteral(manifestRes, RDFS.comment) ;
        if ( manifestName == null )
            manifestName = getLiteral(manifestRes, TestManifest.name) ;
        manifestStmts.close();
    }

    private void parseEntries() {
        StmtIterator manifestStmts = manifest.listStatements(null, RDF.type, TestManifest.Manifest);

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
                    String testName = getLiteral(entry, TestManifest.name) ;
                    Resource action = getResource(entry, TestManifest.action) ;
                    Resource result = getResource(entry, TestManifest.result) ;
                    Resource testType = getResource(entry, RDF.type);
                    ManifestEntry manifestEntry = new ManifestEntry(this, entry, testName, testType, action, result);
                    entries.add(manifestEntry);
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
        StmtIterator includeStmts = manifest.listStatements(null, property, (RDFNode)null) ;

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

    public static Resource getResource(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        if ( ! r.hasProperty(p) )
            return null ;

        RDFNode n = r.getProperty(p).getObject() ;
        if ( n instanceof Resource )
            return (Resource)n ;

        throw new ExTestSetup("Manifest problem (not a Resource): "+n+" => "+p) ;
    }

    private static Collection<Resource> listResources(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        List<Resource> x = new ArrayList<>() ;
        StmtIterator sIter = r.listProperties(p) ;
        for ( ; sIter.hasNext() ; ) {
            RDFNode n = sIter.next().getObject() ;
            if ( ! ( n instanceof Resource ) )
                throw new ExTestSetup("Manifest problem (not a Resource): "+n+" => "+p) ;
            x.add((Resource)n) ;
        }
        return x ;
    }

    private static String getLiteral(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        if ( ! r.hasProperty(p) )
            return null ;

        RDFNode n = r.getProperty(p).getObject() ;
        if ( n instanceof Literal )
            return ((Literal)n).getLexicalForm() ;

        throw new ExTestSetup("Manifest problem (not a Literal): "+n+" => "+p) ;
    }

    private static String getLiteralOrURI(Resource r, Property p)
    {
        if ( r == null )
            return null ;

        if ( ! r.hasProperty(p) )
            return null ;

        RDFNode n = r.getProperty(p).getObject() ;
        if ( n instanceof Literal )
            return ((Literal)n).getLexicalForm() ;

        if ( n instanceof Resource )
        {
            Resource r2 = (Resource)n ;
            if ( ! r2.isAnon() )
                return r2.getURI() ;
        }

        throw new ExTestSetup("Manifest problem: "+n+" => "+p) ;
    }

    private static String safeName(String s)
    {
        // Safe from Eclipse
        s = s.replace('(','[') ;
        s = s.replace(')',']') ;
        return s ;
    }

    @Override
    public String toString() {
        return "manifest["+filename+"]";
    }
}
