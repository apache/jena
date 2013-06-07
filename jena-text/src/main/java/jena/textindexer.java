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

package jena;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.query.text.Entity;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmd.CmdException;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModDataset;
import arq.cmdline.ModDatasetAssembler;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.FmtUtils;


/**
 * Text indexer application that will read a dataset and index its triples in its text index.
 */
public class textindexer extends CmdARQ {
   
    private static Logger log = LoggerFactory.getLogger(textindexer.class) ;

    protected ModDataset       modDataset =  new ModDatasetAssembler() ;
    protected Dataset          dataset = null;
    protected TextIndex        textIndex = null;
    protected EntityDefinition entityDefinition;
    protected ProgressMonitor  progressMonitor;

    static public void main(String... argv)
    { 
        new textindexer(argv).mainRun() ;
    }
    
    static public void testMain(String... argv) {
    	new textindexer(argv).mainMethod();
    }
    
    // @@ TODO
    // check integrated properly with command line processing utilities
    protected textindexer(String[] argv)
    { 
        super(argv) ;
        super.addModule(modDataset);
        progressMonitor = new ProgressMonitor("properties indexed");
    }  

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        dataset = modDataset.createDataset();
        if (dataset == null)
        	throw new CmdException("No dataset specified") ; 
        textIndex = (TextIndex) dataset.getContext().get(TextQuery.textIndex);
        if (textIndex == null) {
        	throw new CmdException("Dataset has no text index");
        } 
        entityDefinition = textIndex.getDocDef();
    }
    
	@Override
	protected String getSummary() {
		return getCommandName()+" [--desc | --dataset] assemblerPath" ;
	}	
	
	@Override
	protected void exec() {	
		Set<Node> properties = getIndexedProperties();
		DatasetGraph dsg = dataset.asDatasetGraph();
		textIndex.startIndexing();
		
		// there are various strategies possible here
		// what is implemented is a first cut simple approach
		// currently - for each indexed property
		//                list and index triples with that property
		// that way only process triples that will be indexed
		// but each entity may be updated several times
		
		for (Iterator<Node> propIter = properties.iterator(); propIter.hasNext() ; ) {
		    Iterator<Quad> quadIter = dsg.find(Node.ANY, Node.ANY, propIter.next(), Node.ANY) ;
		    for ( ; quadIter.hasNext(); ) {
		    	Quad quad = quadIter.next();
		    	Entity entity = createEntity(quad) ;
		    	if (entity != null) {
		    	    textIndex.addEntity(entity);
		    	    progressMonitor.progressByOne();
		    	}
		    }
		}
		textIndex.finishIndexing();	
	    progressMonitor.close();
	}
	
	private Set<Node> getIndexedProperties() {
		Set<Node> result = new HashSet<Node>();
		for ( Iterator<String> iter = entityDefinition.fields().iterator(); iter.hasNext(); ) {
			result.add(entityDefinition.getPredicate(iter.next()));			
		}
		return result;
	}
	
	private Entity createEntity(Quad quad) {
		Node s = quad.getSubject();
		String x = (s.isURI() ) ? s.getURI() : s.getBlankNodeLabel() ;
		Entity result = new Entity(x);
        Node p = quad.getPredicate() ;
        String field = entityDefinition.getField(p) ;
        if ( field == null )
            return null ;
        Node o = quad.getObject() ;
        String val = null ;
        if ( o.isURI() )
            val = o.getURI() ;
        else if ( o.isLiteral() )
            val = o.getLiteralLexicalForm() ;
        else
        {
           log.warn("Not a literal value for mapped field-predicate: "+field+" :: "+FmtUtils.stringForString(field)) ;
           return null;
        }
        result.put(field, val) ;
        return result;
    }
	
	// TDBLoader has a similar progress monitor
	// Not used here to avoid making ARQ dependent on TDB
	// So potential to rationalise and put progress monitor in a common
	// utility class @@ TODO
	private static class ProgressMonitor {
		String progressMessage;
		long startTime;
		long progressCount;
		long intervalStartTime;
		long progressAtStartOfInterval;
		long reportingInterval = 10000; // milliseconds
		
		ProgressMonitor(String progressMessage) {
			this.progressMessage = progressMessage ;
			start();  // in case start not called
		}
		
		void start() {
			startTime = System.currentTimeMillis();
			progressCount = 0L;
			startInterval();
		}
		
		private void startInterval() {
			intervalStartTime = System.currentTimeMillis();
			progressAtStartOfInterval = progressCount;
		}
		
		void progressByOne() {
			progressCount++;
			long now = System.currentTimeMillis();
			if (reportDue(now)) {
				report(now);
				startInterval();
			}
		}
		
		boolean reportDue(long now) {
			return now - intervalStartTime >= reportingInterval;
		}
		
		private void report(long now) {
			long progressThisInterval = progressCount - progressAtStartOfInterval;
			long intervalDuration = now - intervalStartTime;
			long overallDuration = now - startTime;
			String message = 
				progressCount +
				" (" + progressThisInterval / (intervalDuration/1000) + " per second)" +
				progressMessage +
			    " (" + progressCount / Math.max(overallDuration /1000, 1) + " per second overall)";
			log.info(message);		
		}
		
		void close() {
			long overallDuration = System.currentTimeMillis() - startTime;
			String message =
				progressCount +
				" (" + progressCount / Math.max(overallDuration / 1000, 1) + " per second)" +
				progressMessage;
			log.info(message);
		}
	}
}
