/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package dev.optimizer;

import java.util.*;
import java.io.*;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.engine.optimizer.Optimizer;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * Measure the performance of queries
 *  
 * @author Markus Stocker
 */

public class performance 
{
	private static String inGraphFileName = null ;
	private static String inSchemaFileName = null ;
	private static String inQueryFileName = null ;
	private static String inQueryString = null ;
	private static String inIndexFileName = null ;
	private static String optimizer = "on" ;
	private static String reasoner = null ;
	private static int tests = 5 ;
	private static int loops = 10 ;
	private static int ignore = 0 ;
	private static String heuristic = null ;
	private static Model model ;
	private static String log = null ;
	private static boolean limit = false ;
	private static String newline = System.getProperty("line.separator");
	
	/**
	 * Main program
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{			
		int resultSetSize = 0 ;
		List durations = new ArrayList() ; // List<Long>
		List runs = new ArrayList() ; // List<Double>
		
		try
		{
			readCmdParams(args) ;
			
			System.out.println("================================= TEST RUN =================================") ;
			System.out.println("Data: " + inGraphFileName) ;
			System.out.println("Schema: " + inSchemaFileName) ;
			System.out.println("Query: " + inQueryFileName) ;
			System.out.println("Index: " + inIndexFileName) ;
			System.out.println("Loops: " + loops) ;
			System.out.println("Tests: " + tests) ;
			System.out.println("Ignore: " + ignore) ;
			System.out.println("Reasoner: " + reasoner) ;
			System.out.println("Optimizer: " + optimizer) ;
			System.out.println("Heuristic: " + heuristic) ;
			System.out.println("Limit min probability: " + limit) ;
			System.out.println("Log: " + log) ;
			System.out.println("============================================================================") ;
			
			readInQueryFileName() ;
			loadModel() ;
			
			if (optimizer.equals("on"))
			{
				Config config = new Config() ;
				config.setBasicPatternHeuristic(heuristic) ;
				config.setLimitMinProbability(limit) ;
			
				if (inIndexFileName == null)
					Optimizer.enable(config) ;
				else
					Optimizer.enable(model, FileManager.get().loadModel(inIndexFileName), config) ;
			}
			else
				Optimizer.disable() ;
			
			Query query = QueryFactory.create(inQueryString) ;
			
			for (int j = 0; j <= loops + ignore; j++)
			{
				for (int i = 0; i <= tests; i++)
				{
					long start = System.nanoTime() ;
					
					QueryExecution exec = QueryExecutionFactory.create(query, model) ;
					
					try 
					{
			            ResultSet rs = exec.execSelect() ;
			            ResultSetFormatter.consume(rs) ;
			            //ResultSetFormatter.out(rs) ;
			            if (j == 0 && i == 0)
			            	resultSetSize = rs.getRowNumber() ;
			        }
			        finally { exec.close() ; }
			        
			        long end = System.nanoTime() ;
			        long duration = end - start ;
			       
			       	durations.add(new Long(duration)) ;
				}

				double duration = getAvgElapsedTimeInMillis(durations) ;
				System.out.println("Loop " + j + ": " + duration + " (ms)") ;
				
				if (j > ignore)
					runs.add(new Double(duration)) ;
			}
			
			if (log != null)
				writeLog(runs, resultSetSize) ;
		}
		catch (Exception e) { e.printStackTrace() ; }
	}
	
	private static void writeLog(List runs, int resultSetSize)
	{
		try 
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(log)) ;
		
			for (Iterator iter = runs.iterator(); iter.hasNext(); )
			{
				out.write(((Double)iter.next()).toString() + newline) ;
			}
		
			out.write("Timing in ms" + newline) ;
			out.write("Result set size: " + resultSetSize + newline) ;
			out.close() ;
		} 
		catch (FileNotFoundException e) { e.printStackTrace() ; } 
		catch (IOException e) { e.printStackTrace() ;  }
	}
	
	private static double getAvgElapsedTimeInMillis(List durations)
	{
		double ns = 0d ;
		
		for (Iterator iter = durations.iterator(); iter.hasNext(); )
		{
			double duration = ((Long)iter.next()).doubleValue() ;
			ns += duration ;
		}
		
		double ms = ns / 1000000 ;
		
		return ms / durations.size() ;
	}
	
	// Load the model with RDFS inferencing if schema is provided
	private static void loadModel()
	{
		System.out.println("Loading model ...") ;
		
		if (inSchemaFileName != null)
		{
			if (reasoner.equals("Transitive"))
				model = ModelFactory.createInfModel(ReasonerRegistry.getTransitiveReasoner(), 
											    	FileManager.get().loadModel(inSchemaFileName), 
											    	FileManager.get().loadModel(inGraphFileName)) ;
			else 
				model = ModelFactory.createInfModel(ReasonerRegistry.getRDFSReasoner(), 
				    								FileManager.get().loadModel(inSchemaFileName), 
				    								FileManager.get().loadModel(inGraphFileName)) ;
		}
		else
			model = FileManager.get().loadModel(inGraphFileName) ;	
	}
	
	// Read the query file into a string
	private static void readInQueryFileName()
	{
		String line = null ;
		BufferedReader br = null ;
 
	    try 
	    {
	    	br = new BufferedReader(new FileReader(inQueryFileName)) ;	    	
	    	inQueryString = new String() ;
	    	
	    	while ((line = br.readLine()) != null) {
	    		inQueryString += line + "\n" ;
	    	}
	    	
	    	br.close() ;
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace() ;
	    } catch (IOException e) {
	    	e.printStackTrace() ;
	    }

	}
	
	// Read the command line params
	private static void readCmdParams(String[] args) throws Exception
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--graph"))
				inGraphFileName = args[i+1] ;
			else if (args[i].equals("--schema"))
				inSchemaFileName = args[i+1] ;
			else if (args[i].equals("--query"))
				inQueryFileName = args[i+1] ;
			else if (args[i].equals("--index"))
				inIndexFileName = args[i+1] ;
			else if (args[i].equals("--tests"))
				tests = new Integer(args[i+1]).intValue() ;
			else if (args[i].equals("--loops"))
				loops = new Integer(args[i+1]).intValue() ;
			else if (args[i].equals("--ignore"))
				ignore = new Integer(args[i+1]).intValue() ;
			else if (args[i].equals("--reasoner"))
				reasoner = args[i+1] ;
			else if (args[i].equals("--optimizer"))
				optimizer = args[i+1] ;
			else if (args[i].equals("--heuristic"))
				heuristic = args[i+1] ;
			else if (args[i].equals("--log"))
				log = args[i+1] ;
			else if (args[i].equals("--limit"))
				limit = true ;
			else if (args[i].equals("--help"))
				usage() ;
		}

		if (inGraphFileName == null)
			usage() ;
		if (inQueryFileName == null)
			usage() ;
	}
	
	// Print the usage of the main program
	private static void usage()
	{
		String usage = "performance [options]\n" ;
		usage += "--graph\n" ;
		usage += "--schema (optional)\n" ;
		usage += "--query\n" ;
		usage += "--index\n" ;
		usage += "--loops [integer] (num of loops)\n" ;
		usage += "--ignore [integer] (num of loops to ignore)\n" ;
		usage += "--tests [integer] (num of tests per loop)\n" ;
		usage += "--optimizer [on (default) | off]\n" ;
		usage += "--reasoner [RDFS | Transitive]\n" ;
		usage += "--heuristic [one of HeuristicsRegistry.]\n" ;
		usage += "--limit (limit the min probability, optional)\n" ;
		usage += "--log [filename]\n" ;
		
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