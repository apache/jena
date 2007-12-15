/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package dev.optimizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.engine.optimizer.Optimizer;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.IndexLevel;
import com.hp.hpl.jena.sparql.engine.optimizer.sampling.SamplingFactory;

/**
 * Helper class to create the specialized RDF index required
 * for the probabilistic index model.
 * 
 * @author Markus Stocker
 */

public class index 
{
	private static String inGraphFileName = null ;
	private static String inSchemaFileName = null ;
	private static String outIndexFileName = null ;
	private static String reasoner = "RDFS" ;
	private static int level = 1 ;
	private static double samplingFactor = 1.0 ;
	private static String inExcludeFileName = null ;
	private static boolean auto = false ;
	
	/**
	 * Main program
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		readCmdParams(args) ;
		Model infData = null ;
		Graph graph = null ;
		Set exProperty = new HashSet() ;
		Config config = new Config() ;
		String strLevel = "full" ;
		
		if (level == IndexLevel.LIGHTWEIGHT)
			strLevel = "lightweight" ;
		
		System.out.println("Data ontology: " + inGraphFileName) ;
		System.out.println("Index file name: " + outIndexFileName) ;
		System.out.println("Index level: " + strLevel) ;
		System.out.println("Sampling factor: " + samplingFactor) ;
		
		Model data = FileManager.get().loadModel(inGraphFileName) ;
		
		if (inSchemaFileName != null)
		{
			System.out.println("Schema ontology: " + inSchemaFileName) ;
			System.out.println("Reasoner: " + reasoner) ;
			
			Model schema = FileManager.get().loadModel(inSchemaFileName) ;
			
			if (reasoner.equals("RDFS"))
				infData = ModelFactory.createInfModel(ReasonerRegistry.getRDFSReasoner(), schema, data) ;
			else if (reasoner.equals("Transitive"))
				infData = ModelFactory.createInfModel(ReasonerRegistry.getTransitiveReasoner(), schema, data) ;
			else if (reasoner.equals("OWL"))
				infData = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), schema, data) ;
		}
		else
			infData = data ;
		
		if (inExcludeFileName != null)
		{
			System.out.println("Exclude properties:") ;
			
			 try 
			 {
				 BufferedReader bf = new BufferedReader(new FileReader(inExcludeFileName));
				 String line = bf.readLine() ;
				 
				 while (line != null)
				 {
					 exProperty.add(ResourceFactory.createProperty(line)) ;
					 line = bf.readLine() ;
				 }
				 
				 bf.close() ;
				 
			 } catch (FileNotFoundException e) {
				 e.printStackTrace() ;
			 } catch (IOException e) {
				 e.printStackTrace() ;
			 }
			 
			 for (Iterator iter = exProperty.iterator(); iter.hasNext(); )
			 {
				 System.out.println("\t" + ((Property)iter.next()).getURI()) ;
			 }
		}
		
		if (! auto)
		{
			System.out.print("Type 'yes' to start the process (yes/no)? ") ;
	
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
			try
			{
				String input = in.readLine();
				
				if (! input.equals("yes"))
				{
					System.out.println("Process interrupted") ;
					System.exit(0) ;
				}
			} catch (IOException e) { e.printStackTrace(); } 
		}
		
		System.out.println("Please wait until the process terminates ...") ;
		
		try
		{
			config.setIndexLevel(level) ;
			config.setExProperty(exProperty) ;
		
			graph = infData.getGraph() ;
			
			// Apply the sampling
			if (samplingFactor < 1.0)
				graph = SamplingFactory.defaultSamplingMethod(graph, samplingFactor) ;
			
			Optimizer.index(graph, config).write(new FileOutputStream(outIndexFileName)) ;
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		
		System.out.println("Done.") ;
	}
	
	// Read the command line params
	private static void readCmdParams(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--graph"))
				inGraphFileName = args[i+1] ;
			else if (args[i].equals("--schema"))
				inSchemaFileName = args[i+1] ;
			else if (args[i].equals("--index"))
				outIndexFileName = args[i+1] ;
			else if (args[i].equals("--reasoner"))
				reasoner = args[i+1] ;
			else if (args[i].equals("--exclude"))
				inExcludeFileName = args[i+1] ;
			else if (args[i].equals("--factor"))
				samplingFactor = new Double(args[i+1]).doubleValue() ;
			else if (args[i].equals("--auto"))
				auto = true ;
			else if (args[i].equals("--help"))
				usage() ;
		}

		if (inGraphFileName == null || outIndexFileName == null) 
			usage() ;
	}
	
	// Print the usage of the main program
	private static void usage()
	{
		String usage = "index [options]\n" ;
		usage += "--graph\n" ;
		usage += "--index\n" ;
		usage += "--level [0 lightweight, 1 full index (default)] (optional)\n" ;
		usage += "--schema (optional)\n" ;
		usage += "--reasoner [RDFS (default) | Transitive | OWL] (optional)\n" ;
		usage += "--exclude [file name to a list of property URIs to exclude] (optional)\n" ;
		usage += "--factor [0,1] (optional sampling factor)\n" ;
		usage += "--auto (optional)\n" ;
 		
		System.out.println(usage) ;
		
		System.exit(0) ;
	}

}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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