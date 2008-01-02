/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package dev.optimizer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.sampling.SamplingFactory;


public class sampling 
{	
	private static String inGraphFileName = null ;
	private static double percent = 1 ;
	private static String outGraphFileName = null ;
	
	public static void main(String[] args) 
	{		
		readCmdParams(args) ;
		
		Graph graph = SamplingFactory.defaultSamplingMethod(FileManager.get().loadModel(inGraphFileName), percent) ;
		Model model = ModelFactory.createModelForGraph(graph) ;
		
		try
		{
			model.write(new FileOutputStream(outGraphFileName)) ;
		} catch (FileNotFoundException e) { e.printStackTrace() ; }
	}
	
	// Read the command line params
	private static void readCmdParams(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--graph"))
				inGraphFileName = args[i+1] ;
			else if (args[i].equals("--percent"))
				percent = new Double(args[i+1]).doubleValue() ;
			else if (args[i].equals("--out"))
				outGraphFileName = args[i+1] ;
			else if (args[i].equals("--help"))
				usage() ;
		}

		if (inGraphFileName == null || outGraphFileName == null)
			usage() ;
	}
	
	// Print the usage of the main program
	private static void usage()
	{
		String usage = "sampling [options]\n" ;
		usage += "--graph [filename]\n" ;
		usage += "--percent [double of the interval 0,1]\n" ;
		usage += "--out [filename]\n" ;
		
		System.out.println(usage) ;
		
		System.exit(0) ;
	}
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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