/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package dev.optimizer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

//import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;


public class inference 
{	
	private static String inGraphFileName = null ;
	private static String inSchemaFileName = null ;
	private static String outGraphFileName = null ;
	
	public static void main(String[] args) 
	{
		readCmdParams(args) ;
		
		OntModel schema = ModelFactory.createOntologyModel() ;
		schema.read(inSchemaFileName) ;
		
		Model model = ModelFactory.createDefaultModel() ;
		model.read(inGraphFileName) ;
		
		//InfModel infModel = ModelFactory.createInfModel(PelletReasonerFactory.theInstance().create(), schema, model) ;
		InfModel infModel = ModelFactory.createInfModel(ReasonerRegistry.getRDFSReasoner(), schema, model) ;
		
		try
		{
			infModel.write(new FileOutputStream(outGraphFileName)) ;
		} catch (FileNotFoundException e) { e.printStackTrace() ; }
	}

	private static void readCmdParams(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--graph"))
				inGraphFileName = args[i+1] ;
			else if (args[i].equals("--schema"))
				inSchemaFileName = args[i+1] ;
			else if (args[i].equals("--out"))
				outGraphFileName = args[i+1] ;
			else if (args[i].equals("--help"))
				usage() ;
		}

		if (inGraphFileName == null)
			usage() ;
		if (inSchemaFileName == null)
			usage() ;
		if (outGraphFileName == null)
			usage() ;
	}
	
	// Print the usage of the main program
	private static void usage()
	{
		String usage = "inference [options]\n" ;
		usage += "--graph\n" ;
		usage += "--schema\n" ;
		usage += "--out\n" ;
		
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