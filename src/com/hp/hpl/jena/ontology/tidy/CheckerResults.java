/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: CheckerResults.java,v 1.1 2004-01-27 15:45:24 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

import java.util.Iterator;

/**
 * 
 * This interface provides the shared operations
 * on the in-memory {@link Checker} and the
 * {@link StreamingChecker}. Most of these are
 * to do with getting the results out of the syntax checker.
 * The ways of the invoking the two checkers diverge.
 * @author jjc
 *
 * 
 */
public interface CheckerResults {
	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors found by the syntax checker.
	 * If the liteFlag was set in the constructor then
	 * all OWL DL and OWL Full constructs are errors.
	 * If the liteFlag was not set, then only OWL Full constructs
	 * are errors.
	 */
	public abstract Iterator getErrors();
	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors or warnings found by the syntax checker.
	 */
	public abstract Iterator getProblems();
	/**
	 * Which subLanguage is this document in.
	 * @return "Lite", "DL" or "Full".
	 */
	public abstract String getSubLanguage();
	/**
	 * If set, the syntax checker will conserve space
	 * at the expense of the quality of the
	 * {@link SyntaxProblem#longDescription} of errors.
	 * @param big
	 */
	public abstract void setOptimizeMemory(boolean big);
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