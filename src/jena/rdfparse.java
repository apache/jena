/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: rdfparse.java,v 1.6 2005-04-06 08:12:43 chris-dollin Exp $
*/

package jena;

import java.lang.reflect.Constructor;

import com.hp.hpl.jena.rdf.arp.NTriple;
import com.hp.hpl.jena.shared.Command;

/** A command line interface into ARP.
 * Creates NTriple's or just error messages.
 * <pre>
 * java &lt;class-path&gt; jena.rdfparse ( [ -[xstfu]][ -b xmlBase -[eiw] NNN[,NNN...] ] [ file ] [ url ] )...
 * 
 * java &lt;class-path&gt; jena.rdfparse --test
 * 
 * java &lt;class-path&gt; jena.rdfparse --internal-test
 * </pre>
 * <p>
 * &lt;class-path&gt; should contain <code>jena.jar</code>, <code>xerces.jar</code>, <code>junit.jar</code>,  and <code>icu4j.jar</code> or equivalents.
 * </p>
 * <p>
 * The last two forms are for testing. <code>--test</code> runs ARP
 * against the RDF Core Working Group tests found at w3.org.
 * <code>--internal-test</code> uses a cached copy from within the jena.jar.
 * </p>
 * All options, files and URLs can be intemingled in any order.
 * They are processed from left-to-right.
 * <dl>
 * file    </dt><dd>  Converts (embedded) RDF in XML file into N-triples
 * </dd><dt>
 * url  </dt><dd>     Converts (embedded) RDF from URL into N-triples
 * </dd><dt>
 * -b uri </dt><dd>   Sets XML Base to the absolute URI.
 * </dd><dt>
 * -r    </dt><dd>    Content is RDF (no embedding, rdf:RDF tag may be omitted).
 * </dd><dt>
 * -t  </dt><dd>      No n-triple output, error checking only.
 * </dd><dt>
 * -x   </dt><dd>     Lax mode - warnings are suppressed.
 * </dd><dt>
 * -s    </dt><dd>    Strict mode - most warnings are errors.
 * </dd><dt>
 * -u     </dt><dd>   Allow unqualified attributes (defaults to warning).
 * </dd><dt>
 * -f    </dt><dd>    All errors are fatal - report first one only.
 * </dd><dt>
 * -b url </dt><dd>   Sets XML Base to the absolute url.
 * </dd><dt>
 * -e NNN[,NNN...]</dt><dd>
 * Treats numbered warning conditions as errrors.
 * </dd><dt>
 * -w NNN[,NNN...]</dt><dd>
 * Treats numbered error conditions as warnings.
 * </dd><dt>
 * -i NNN[,NNN...]
 * </dt><dd>
 * Ignores numbered error/warning conditions.
 * </dl>
 * @author jjc
 */

public class rdfparse {

	/** Either start an RDF/XML to NTriple converter, or run test suite.
	 * @param args The command-line arguments.
	 */
	public static void main( String[] args ) throws Exception {
		if (args.length == 1 && (args[0].equals( "--test" ) || args[0].equals( "--internal-test" ))) 
            runTests( args[0].equals( "--test" ) );
        else
		    NTriple.main( args );
	}

    /**
         wrapped this way so JUnit not a compile-time requirement.
    */
    protected static void runTests( boolean internetTest ) throws Exception { 
        Class rdfparse = Class.forName( "jena.test.rdfparse" );
        Constructor constructor = rdfparse.getConstructor( new Class[] {boolean.class} );
        Command c = (Command) constructor.newInstance( new Object[] {new Boolean( internetTest ) } );
        c.execute();
//        ARPTests.internet = internetTest;
//        TestRunner.main( new String[] { "-noloading", ARPTests.class.getName()});
        }
}

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
