/**
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

package org.apache.jena.example;


// Imports
///////////////
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Base file for <em>Getting Started</em> examples. A place to put shared
 * implementation, constants, etc.</p>
 */
public abstract class Base
    implements Runnable
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** Relative path to the directory containing sample data */
    public static final String DATA_DIR = "./src/main/resources/data/";

    /** Relative path to the directory containing sample ontologies */
    public static final String ONTOLOGIES_DIR = "./src/main/resources/ontologies/";


    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( Base.class );

    /** Command line options */
    private static Options options;

    /***********************************/
    /* Instance variables              */
    /***********************************/

    private CommandLine commandLine;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Add a command line option for the application, which will be matched
     * against the command-line args at run time.
     * @param opt Short option or null, e.g. <code>-a</code>
     * @param longOpt Long option name or null, e.g. <code>--data</code>
     * @param hasArg If true, the option will take an argument, e.g. <code>--dataFile foo.txt</code>
     * @param description Argument description as it will appear in the usage message
     */
    public static void addOption( String opt, String longOpt, boolean hasArg, String description ) {
        if (Base.options == null) {
            Base.options = new Options();
        }
        options.addOption( opt, longOpt, hasArg, description );
    }

    /**
     * Return the list of options, ensuring that it is non-null
     * @return
     */
    public static Options getOptions() {
        if (Base.options == null) {
            Base.options = new Options();
        }
        return options;
    }

    /**
     * Set up the command line arguments according to the options. If the
     * arguments do not parse, print an error message and exit with status 1.
     * @param args
     */
    public Base setArgs( String[] args ) {
        if (args == null) {args = new String[] {};}

        try {
            commandLine = new GnuParser().parse( Base.getOptions(), args );
        }
        catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "", options );
            System.exit( 1 );
        }

        // allow method chaining
        return this;
    }

    /**
     * Return true if the given option is set in the given command line
     * @param opt Option to test for, e.g. <code>data</code>
     * @return
     */
    public boolean hasArg( String opt ) {
        if (commandLine == null) {
            System.err.println( "Command line arguments have not been set yet. See setArgs( String[] args )" );
            System.exit( 1 );
        }
        return commandLine.hasOption( opt );
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

