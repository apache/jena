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

package org.apache.jena.example.helloworld;


// Imports
///////////////
import org.apache.jena.example.CheeseBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * <h2>Apache Jena Getting Started Guide - Step 1: Hello World</h2>
 * <p>
 * In this step, we illustrate the basic operations of getting some data into
 * a Java program, finding some data, and showing some output.
 * </p>
 */
public class HelloWorld
    extends CheeseBase
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( HelloWorld.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Main entry point for running this example. Since every sub-class
     * will be {@link Runnable}, we create an instance, stash the
     * command-line args where we can retrieve them later, and
     * invoke {@link #run}
     */
    public static void main( String[] args ) {
        new HelloWorld().setArgs( args ).run();
    }

    public void run() {
        // creates a new, empty in-memory model
        Model m = ModelFactory.createDefaultModel();

        // load some data into the model
        FileManager.get().readModel( m, CHEESE_DATA_FILE );

        // generate some output
        showModelSize( m );
        listCheeses( m );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Show the size of the model on stdout
     */
    protected void showModelSize( Model m ) {
        System.out.println( String.format( "The model contains %d triples", m.size() ) );
    }

    /**
     * List the names of cheeses to stdout
     */
    protected void listCheeses( Model m ) {
        Resource cheeseClass = m.getResource( CHEESE_SCHEMA + "Cheese" );

        StmtIterator i = m.listStatements( null, RDF.type, cheeseClass );

        while (i.hasNext()) {
            Resource cheese = i.next().getSubject();
            String label = getEnglishLabel( cheese );
            System.out.println( String.format( "Cheese %s has name: %s", cheese.getURI(), label ) );
        }
    }

    /**
     * Get the English-language label for a given resource. In general, a resource
     * may have zero, one or many labels. In this case, we happen to know that
     * the cheese resources have mutlilingual labels, so we pick out the English one
     * @param cheese
     * @return
     */
    protected String getEnglishLabel( Resource cheese ) {
        StmtIterator i = cheese.listProperties( RDFS.label );
        while (i.hasNext()) {
            Literal l = i.next().getLiteral();

            if (l.getLanguage() != null && l.getLanguage().equals( "en")) {
                // found the English language label
                return l.getLexicalForm();
            }
        }

        return "A Cheese with No Name!";
    }

    /**
     * Get the value of a property as a string, allowing for missing properties
     * @param r A resource
     * @param p The property whose value is wanted
     * @return The value of the <code>p</code> property of <code>r</code> as a string
     */
    protected String getValueAsString( Resource r, Property p ) {
        Statement s = r.getProperty( p );
        if (s == null) {
            return "";
        }
        else {
            return s.getObject().isResource() ? s.getResource().getURI() : s.getString();
        }
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

