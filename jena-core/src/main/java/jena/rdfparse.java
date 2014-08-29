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

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration ;

import java.lang.reflect.Constructor ;

import com.hp.hpl.jena.rdfxml.xmlinput.NTriple ;
import com.hp.hpl.jena.shared.Command ;

/** A command line interface into ARP.
 * Creates NTriple's or just error messages.
 * <pre>
 * java &lt;class-path&gt; jena.rdfparse ( [ -[xstfu]][ -b xmlBase -[eiw] NNN[,NNN...] ] [ file ] [ url ] )...
 * 
 * java &lt;class-path&gt; jena.rdfparse --test
 * 
 * java &lt;class-path&gt; jena.rdfparse --internal-test
 * </pre>
 * 
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
 * -f    </dt><dd>    All errors are.error - report first one only.
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
 */

public class rdfparse {

    static { setLog4jConfiguration() ; }

    /** Either start an RDF/XML to NTriple converter, or run test suite.
	 * @param args The command-line arguments.
	 */
	public static void main( String... args ) throws Exception {
		if (args.length == 1 && (args[0].equals( "--test" ) || args[0].equals( "--internal-test" ))) 
            runTests( args[0].equals( "--test" ) );
        else
		    NTriple.main( args );
	}

    /**
         wrapped this way so JUnit not a compile-time requirement.
    */
    protected static void runTests( boolean internetTest ) throws Exception { 
        Class<?> rdfparse = Class.forName( "jena.test.rdfparse" );
        Constructor<?> constructor = rdfparse.getConstructor( new Class[] {boolean.class} );
        Command c = (Command) constructor.newInstance( new Object[] { internetTest } );
        c.execute();
//        ARPTests.internet = internetTest;
//        TestRunner.main( new String[] { "-noloading", ARPTests.class.getName()});
        }
}
