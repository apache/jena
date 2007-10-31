/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package dev.optimizer;

import java.util.*;
import java.io.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.Optimizer;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternOptimizer;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternVisitor;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Probability;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.ProbabilityFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * Search the best execution plan, i.e. execute them all
 *  
 * @author Markus Stocker
 * @version $Id$
 */

public class search 
{
	// Based on http://www.geocities.com/permute_it/
	private String inGraphFileName = null ;
	private String inQueryFileName = null ;
	private String inIndexFileName = null ;
	private int tests = 5 ;
	private int loops = 10 ;
	private int ignore = 0 ;
	private Model model = null ;
	private Model index = null ;
	private String log = null ;
	private int permutations = 0 ;
	private String qstr = null ;
	private double time = Double.MAX_VALUE ;
	private int resultSetSize = 0 ;
	private Map timings = new HashMap() ;
	
	public static void main(String[] args) 
	{	
		search s = new search() ;
		s.run(args) ;
	}
	
	public void run(String[] args) 
	{		
		readCmdParams(args) ;
	    
		System.out.println("================================= TEST RUN =================================") ;
		System.out.println("Data: " + inGraphFileName) ;
		System.out.println("Query: " + inQueryFileName) ;
		System.out.println("Index: " + inIndexFileName) ;
		System.out.println("Loops: " + loops) ;
		System.out.println("Tests: " + tests) ;
		System.out.println("Ignore: " + ignore) ;
		System.out.println("Log: " + log) ;
		System.out.println("============================================================================") ;
		
		loadModel() ;
		loadIndex() ;
		Graph graph = model.getGraph() ;
	    Config config = new Config() ;
	    Context context = ARQ.getContext() ;
		config.setLimitMinProbability(true) ;
	    Probability probability = ProbabilityFactory.loadDefaultModel(model, index, config) ;
		context.set(Constants.PF, probability) ;
		
		Optimizer.disable() ;
		
		Query q = QueryFactory.read(inQueryFileName) ;
		
		BasicPatternVisitor visitor = new BasicPatternVisitor() ;
		Element el = q.getQueryPattern() ;
		Op op = Algebra.compile(el) ;
		OpWalker.walk(op, visitor) ;
	    List patterns = visitor.getPatterns() ; // List<BasicPattern>
	    // Get the BGP (this works only for queries with a single BGP!
	    BasicPattern bgp = (BasicPattern)patterns.get(0) ;
	    // Get the basic patterns for all possible optimizations
	    // BGP_VARIABLE_COUNTING
		BasicPattern bgpVC = getBGPForHeuristic("BGP_VARIABLE_COUNTING", true, context, graph, bgp, config) ;
		// BGP_PROBABILISTIC_FRAMEWORK
		BasicPattern bgpPF = getBGPForHeuristic("BGP_PROBABILISTIC_FRAMEWORK", true, context, graph, bgp, config) ;
		// BGP_PROBABILISTIC_FRAMEWORK_JOIN
		BasicPattern bgpPFJ = getBGPForHeuristic("BGP_PROBABILISTIC_FRAMEWORK_JOIN", true, context, graph, bgp, config) ;
		// BGP_GRAPH_STATISTICS_HANDLER
		BasicPattern bgpSH = getBGPForHeuristic("BGP_GRAPH_STATISTICS_HANDLER", true, context, graph, bgp, config) ;
		// BGP_VARIABLE_COUNTING_UNBOUND
		BasicPattern bgpVCU = getBGPForHeuristic("BGP_VARIABLE_COUNTING_UNBOUND", true, context, graph, bgp, config) ;
		// BGP_PROBABILISTIC_FRAMEWORK_NO_LIMIT
		BasicPattern bgpPFN = getBGPForHeuristic("BGP_PROBABILISTIC_FRAMEWORK", false, context, graph, bgp, config) ;
		
	    int size = bgp.size() ;
		Triple[] triples = new Triple[size] ;
	    int N = 0 ;
	    for (Iterator iter = bgp.iterator(); iter.hasNext(); )
	    {
	    	triples[N] = (Triple)iter.next() ;
	    	N++ ;
	    }
	    if (triples.length != size || N != size)
	    	throw new IllegalArgumentException("Size of the triples array is not equal to the size of the BGP") ;
	   
	    int factorial = factorial(N) ;
	    
	    evaluate(q, triples, bgp, bgpVC, bgpPF, bgpPFJ, bgpSH, bgpVCU, bgpPFN) ;
		
	    /*
	     * This is the permutations block
	     */
	    int[] p = new int[N+1] ;
	    
	    for (int i = 0; i < p.length; i++)
	    	p[i] = i ;
	  
	    int j = 0 ;
	    int i = 1 ;
	  
	    while (i < N)
	    {
	    	p[i] = p[i] - 1 ;
	    	
	    	if (i % 2 == 1) // i is odd
	    		j = p[i] ;
	    	else
	    		j = 0 ;
	    	
	    	// Swap(triples[j], triples[i])
	    	Triple triple = triples[i] ;
	    	triples[i] = triples[j] ;
	    	triples[j] = triple ;
			
	    	evaluate(q, triples, bgp, bgpVC, bgpPF, bgpPFJ, bgpSH, bgpVCU, bgpPFN) ;
	    	
	    	if (log != null)
				writeLog() ;
			
	    	i = 1 ;
	    	while (p[i] == 0)
	    	{
	    		p[i] = i ;
	    		i = i + 1 ;
	    	}
	    }
	    /*
	     * End permutations block
	     */
		
		if (permutations != factorial)
			throw new IllegalArgumentException("Not all permutations have been processed") ;
		
		if (log != null)
			writeLog() ;
	}
	
	private BasicPattern getBGPForHeuristic(String heuristic, boolean limit, Context context, Graph graph, BasicPattern bgp, Config config)
	{
		config.setLimitMinProbability(limit) ;
		config.setBasicPatternHeuristic(heuristic) ;
		BasicPatternOptimizer optimizer = new BasicPatternOptimizer(context, graph, bgp, config) ;

		return optimizer.optimize() ;
	}
	
	private void evaluate(Query q, Triple[] triples, BasicPattern bgpOFF, BasicPattern bgpVC, BasicPattern bgpPF, BasicPattern bgpPFJ, BasicPattern bgpSH, BasicPattern bgpVCU, BasicPattern bgpPFN)
	{		
		BasicPattern pattern = new BasicPattern() ;
		ElementTriplesBlock el = new ElementTriplesBlock() ;
	    List matchesHeuristic = new ArrayList() ;
	    
	    // Add the triples to the new bgp
	    for (int i = 0; i < triples.length; i++)
	    {
	    	Triple triple = (Triple)triples[i] ;
	    	
	    	el.addTriple(triple) ;
	    	pattern.add(triple) ;
	    }
	    
	    if (pattern.equals(bgpVC))
	    	matchesHeuristic.add("VC") ;
	    if (pattern.equals(bgpPF))
	    	matchesHeuristic.add("PF") ;
	    if (pattern.equals(bgpPFJ))
	    	matchesHeuristic.add("PFJ") ;
	    if (pattern.equals(bgpSH))
	    	matchesHeuristic.add("SH") ;
	    if (pattern.equals(bgpVCU))
	    	matchesHeuristic.add("VCU") ;
	    if (pattern.equals(bgpPFN))
	    	matchesHeuristic.add("PFN") ;
	    if (pattern.equals(bgpOFF))
	    	matchesHeuristic.add("OFF") ;
 	    
	    // Override the query pattern
	    q.setQueryPattern(el) ;
	    
		System.out.println("PERMUTATION: " + permutations) ;
		
		if (permutations > 247)
		{
		    Evaluate eval = new Evaluate(q, model, tests, loops, ignore) ;
		    eval.run() ;
			
			double t = eval.time() ;
			resultSetSize = eval.resultSetSize() ;
			
			if (t < time)
			{
				time = t ;
				qstr = q.toString() ;
			}
			
			timings.put(new Double(t), matchesHeuristic) ;
		}
		
		permutations++ ;
	}
	
	private int factorial(int size)
	{
		int factorial = size ;

		for (int i = size - 1; i > 0; i--)
			factorial *= i ;
		
		return factorial ;
	}
	
	private void writeLog()
	{
		try 
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(log)) ;
		
			out.write(qstr + "\n") ;
			out.write("Timing in ms: " + time + "\n") ;
			out.write("Result set size: " + resultSetSize + "\n") ;
			out.write("\n") ;
			
			for (Iterator iter = timings.keySet().iterator(); iter.hasNext(); )
			{
				Double time = (Double)iter.next() ;
				List heuristics = (List)timings.get(time) ;
				out.write(time.toString()) ;
				if (heuristics.size() > 0)
					out.write(" " + heuristics.toString()) ;
				out.write("\n") ;
			}
			
			out.close() ;
		} 
		catch (FileNotFoundException e) { e.printStackTrace() ; } 
		catch (IOException e) { e.printStackTrace() ;  }
	}
	
	// Load the model with RDFS inferencing if schema is provided
	private void loadModel()
	{
		System.out.println("Loading model ...") ;
	
		model = FileManager.get().loadModel(inGraphFileName) ;
		
		System.out.println("Number of triples: " + model.size()) ;
	}
	
	private void loadIndex()
	{
		if (inIndexFileName != null)
		{
			System.out.println("Loading index ...") ;
			index = FileManager.get().loadModel(inIndexFileName) ;
		}
	}
	
	// Read the command line params
	private void readCmdParams(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--graph"))
				inGraphFileName = args[i+1] ;
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
			else if (args[i].equals("--log"))
				log = args[i+1] ;
			else if (args[i].equals("--help"))
				usage() ;
		}

		if (inGraphFileName == null)
			usage() ;
		if (inQueryFileName == null)
			usage() ;
	}
	
	// Print the usage of the main program
	private void usage()
	{
		String usage = "search [options]\n" ;
		usage += "--graph\n" ;
		usage += "--query\n" ;
		usage += "--loops [integer] (num of loops)\n" ;
		usage += "--ignore [integer] (num of loops to ignore)\n" ;
		usage += "--tests [integer] (num of tests per loop)\n" ;
		usage += "--log [filename]\n" ;
		
		System.out.println(usage) ;
		
		System.exit(0) ;
	}
}

class Evaluate
{
	private Query query = null ;
	private double time = Double.MAX_VALUE ;
	private int resultSetSize = 0 ;
	private int tests = 0 ;
	private int loops = 0 ;
	private int ignore = 0 ;
	private Model model = null ;
	private String qstr = null ;
	
	public Evaluate(Query query, Model model, int tests, int loops, int ignore)
	{
		this.query = query ;
		this.model = model ;
		this.tests = tests ;
		this.loops = loops ;
		this.ignore = ignore ;
	}
	
	public void run()                  
    {        
		List durations = new ArrayList() ; // List<Long>
	    qstr = query.toString() ;
	    
		List runs = new ArrayList() ;
		
		System.out.println("======================================================================") ;
		System.out.println(qstr) ;
		
		for (int j = 0; j <= loops + ignore; j++)
		{
			for (int i = 0; i <= tests; i++)
			{
				long d = evaluate(j, i) ;	
		      	durations.add(new Long(d)) ;
			}

			double duration = getAvgElapsedTimeInMillis(durations) ;
			System.out.println("Loop " + j + ": " + duration + " (ms)") ;
			
			//if (j > ignore)
				runs.add(new Double(duration)) ;
		}
		
		time = getAvg(runs) ;
		System.out.println("AVG: " + time + " (ms)") ;
    }
	
	private long evaluate(int j, int i)
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
        
        return duration ;
	}
	
	public double time()
	{
		return time ;
	}
	
	public int resultSetSize()
	{
		return resultSetSize ;
	}
	
	private static double getAvgElapsedTimeInMillis(List durations)
	{
		double ns = 0d ;
		double duration ;
		
		for (Iterator iter = durations.iterator(); iter.hasNext(); )
		{
			duration = ((Long)iter.next()).doubleValue() ;
			ns += duration ;
		}
		
		double ms = ns / 1000000 ;
		
		return ms / durations.size() ;
	}
	
	private static double getAvg(List durations)
	{
		double duration = 0d ;
		
		for (Iterator iter = durations.iterator(); iter.hasNext(); )
		{
			duration += ((Double)iter.next()).doubleValue() ;
		}
		
		return duration / durations.size() ;
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