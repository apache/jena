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

package jena.text ;

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.text.* ;
import org.apache.jena.sparql.core.Quad ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import jena.cmd.ArgDecl ;
import jena.cmd.CmdException ;
import arq.cmdline.CmdARQ ;

/**
 * Text indexer application that will read a dataset and index its triples in
 * its text index.
 */
public class textindexer extends CmdARQ {

    private static Logger      log          = LoggerFactory.getLogger(textindexer.class) ;

    public static final ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
    
    protected DatasetGraphText dataset      = null ;
    protected TextIndex        textIndex    = null ;
    protected EntityDefinition entityDefinition ;
    protected ProgressMonitor  progressMonitor ;

    static public void main(String... argv) {
        new textindexer(argv).mainRun() ;
    }

    static public void testMain(String... argv) {
        new textindexer(argv).mainMethod() ;
    }

    protected textindexer(String[] argv) {
        super(argv) ;
        super.add(assemblerDescDecl, "--desc=", "Assembler description file") ;
        progressMonitor = new ProgressMonitor("properties indexed") ;
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs() ;
        // Two forms : with and without arg.
        // Maximises similarity with other tools.
        String file ;
        
        if ( ! super.contains(assemblerDescDecl) && getNumPositional() == 0 )
            throw new CmdException("No assembler description given") ;
        
        if ( super.contains(assemblerDescDecl) ) {
            if ( getValues(assemblerDescDecl).size() != 1 )
                throw new CmdException("Multiple assembler descriptions given via --desc") ;
            if ( getPositional().size() != 0 )
                throw new CmdException("Additional assembler descriptions given") ; 
            file = getValue(assemblerDescDecl) ;
        } else {
            if ( getNumPositional() != 1 )
                throw new CmdException("Multiple assembler descriptions given as positional arguments") ;
            file = getPositionalArg(0) ;
        }
        
        if (file == null)
            throw new CmdException("No dataset specified") ;
        // Assumes a single test dataset description in the assembler file.
        Dataset ds = TextDatasetFactory.create(file) ;
        if (ds == null)
            throw new CmdException("No dataset description found") ;
        // get index.
        dataset = (DatasetGraphText)(ds.asDatasetGraph()) ;
        textIndex = dataset.getTextIndex() ;
        if (textIndex == null)
            throw new CmdException("Dataset has no text index") ;
        entityDefinition = textIndex.getDocDef() ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " assemblerFile" ;
    }

    @Override
    protected void exec() {
        try {
            // JENA-1486 Make sure to use transactions if supported
            // The supportsTransactions() check should be strictly unecessary as we should always be using a
            // DatasetGraphText which is transactional but just for future proofing we check anyway
            if (dataset.supportsTransactions()) {
                dataset.begin(ReadWrite.READ);
            }
            
            Set<Node> properties = getIndexedProperties() ;
    
            // there are various strategies possible here
            // what is implemented is a first cut simple approach
            // currently - for each indexed property
            // list and index triples with that property
            // that way only process triples that will be indexed
            // but each entity may be updated several times
    
            for ( Node property : properties )
            {
                Iterator<Quad> quadIter = dataset.find( Node.ANY, Node.ANY, property, Node.ANY );
                for (; quadIter.hasNext(); )
                {
                    Quad quad = quadIter.next();
                    if ( Quad.isDefaultGraph(quad.getGraph()) ) {
                        // Need to use urn:x-arq:DefaultGraphNode for text indexing (JENA-1133)
                        quad = Quad.create(Quad.defaultGraphNodeGenerated,
                            quad.getSubject(), quad.getPredicate(), quad.getObject());
                    }
                    Entity entity = TextQueryFuncs.entityFromQuad( entityDefinition, quad );
                    if ( entity != null )
                    {
                        textIndex.addEntity( entity );
                        progressMonitor.progressByOne();
                    }
                }
            }
            
            textIndex.commit();
            textIndex.close();
            
            if (dataset.supportsTransactions()) {
                dataset.commit();
            }
            dataset.close();
            
            progressMonitor.close() ;
        } finally {
            if (dataset.supportsTransactions()) {
                dataset.end();
            }
        }
    }

    private Set<Node> getIndexedProperties() {
        Set<Node> result = new HashSet<>() ;
        for (String f : entityDefinition.fields()) {
            for ( Node p : entityDefinition.getPredicates(f) )
                result.add(p) ;
        }
        return result ;
    }

    // TDBLoader has a similar progress monitor
    // Not used here to avoid making ARQ dependent on TDB
    // So potential to rationalise and put progress monitor in a common
    // utility class
    private static class ProgressMonitor {
        String progressMessage ;
        long   startTime ;
        long   progressCount ;
        long   intervalStartTime ;
        long   progressAtStartOfInterval ;
        long   reportingInterval = 10000 ; // milliseconds

        ProgressMonitor(String progressMessage) {
            this.progressMessage = progressMessage ;
            start() ; // in case start not called
        }

        void start() {
            startTime = System.currentTimeMillis() ;
            progressCount = 0L ;
            startInterval() ;
        }

        private void startInterval() {
            intervalStartTime = System.currentTimeMillis() ;
            progressAtStartOfInterval = progressCount ;
        }

        void progressByOne() {
            progressCount++ ;
            long now = System.currentTimeMillis() ;
            if (reportDue(now)) {
                report(now) ;
                startInterval() ;
            }
        }

        boolean reportDue(long now) {
            return now - intervalStartTime >= reportingInterval ;
        }

        private void report(long now) {
            long progressThisInterval = progressCount - progressAtStartOfInterval ;
            long intervalDuration = now - intervalStartTime ;
            long overallDuration = now - startTime ;
            String message = progressCount + " (" + progressThisInterval / (intervalDuration / 1000) + " per second)"
                             + progressMessage + " (" + progressCount / Math.max(overallDuration / 1000, 1)
                             + " per second overall)" ;
            log.info(message) ;
        }

        void close() {
            long overallDuration = System.currentTimeMillis() - startTime ;
            String message = progressCount + " (" + progressCount / Math.max(overallDuration / 1000, 1)
                             + " per second) " + progressMessage ;
            log.info(message) ;
        }
    }
}
