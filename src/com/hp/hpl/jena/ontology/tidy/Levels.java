/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Levels.java,v 1.5 2003-12-02 04:58:34 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

/**
 * The return values of {@link SyntaxProblem#getLevel}.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class Levels {
	/**
	 * The associated SyntaxProblem is a warning.
	 * Examples include bad names from the OWL, RDF and XSD namespaces.
	 */
	static public final int Warning = 0;   
	/**
	 * The associated SyntaxProblem is not legel in OWL Lite.
	 */
  static public final int Lite = 1;
  /**
   * The associated SyntaxProblem is not legal in OWL DL.
   */
  static public final int DL = 2;
  /**
   * Currently unused.
   */
  static public final int Full = 3;
  /**
   * Currently unused.
   */
  static public final int Other = 4; // Bad RDF doc
  static private String desc[] = {
  	"Warning", "Lite", "DL", "Full", "Other"
  	
  };
  /**
   * Gives a readable form of one of the Levels.
   * @param i Must be {@link #Warning}, {@link #Lite}, {@link #DL}, {@link #Full} or {@link #Other}.
   * @return The name of the level.
   */
  static public String toString(int i) {
  	return desc[i];
  }
}

/*
	(c) Copyright 2003 Hewlett-Packard Development Company, LP
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/