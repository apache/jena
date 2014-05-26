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

package org.apache.jena.atlas.csv ;

import java.io.InputStream ;
import java.io.Reader ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.iterator.PeekIterator ;

/** Written specifically to handle SPARQL results CSv files.
 *  Replace with a real parser (e.g. Apache Commons CSV when released)
 */
public class CSVParser implements Iterable<List<String>>
{
    public static CSVParser create(String filename) {
        InputStream input = IO.openFile(filename) ;
        return create(input) ;
    }

    public static CSVParser create(InputStream input) {
        CSVTokenIterator iter = new CSVTokenIterator(input) ;
        CSVParser parser = new CSVParser(iter) ;
        return parser ; 
    }
    
    public static CSVParser create(Reader input) {
        CSVTokenIterator iter = new CSVTokenIterator(input) ;
        CSVParser parser = new CSVParser(iter) ;
        return parser ; 
    }

    private final CSVTokenIterator iter ;
    private final PeekIterator<CSVToken> pIter ;

    public CSVParser(CSVTokenIterator iter) {
        this.iter = iter ;
        this.pIter = new PeekIterator<CSVToken>(iter) ;
    }
    
    @Override
    public Iterator<List<String>> iterator() {
        return new IteratorSlotted<List<String>>() {
            @Override
            protected List<String> moveToNext() {
                return CSVParser.this.parse1() ;
            }

            @Override
            protected boolean hasMore() {
                return true ;
            }};
    }

    public List<String> parse1() {
        // Get rid of switches. break problems.
        List<String> line = new ArrayList<String>(100) ;
        
        loop: while (pIter.hasNext()) {
            CSVToken t = pIter.next() ;
            switch (t.type) {
                case EOF :
                    return null ;
                case NL :
                    // Blank line = one or none?
                    line.add("") ;
                    return line ;
                case STRING :
                case QSTRING :
                    line.add(t.image) ;
                    break ;
                case COMMA :
                    // Immediate COMMA is an empty term.
                    line.add("") ;
                    continue loop ;
                default :
                    exception("Syntax error: expected a string or comma.", t) ;
            }
            // Expect COMMA or NL
            if ( !pIter.hasNext() ) {
                // File ends, no NL.
                return line ;
            }
            // Look at separateor or end
            CSVToken t2 = pIter.peek() ;
            switch (t2.type) {
                case COMMA :
                    pIter.next() ;
                    continue loop ;
                case NL :
                case EOF :
                    pIter.next() ;
                    return line ;
                default :
                    exception("Syntax error: expect comma or end of line.", t) ;
            }
        }
        return null ;
    }
    static void exception(String msg, CSVToken t) {
        if ( t != null && t.line >= 0 && t.col > 0 )
            msg = String.format("[%s, %s] %s", t.line, t.col, msg) ;
        throw new CSVParseException(msg) ;
    }

    static void exception(String msg, long line, long col) {
        if ( line >= 0 && col > 0 )
            msg = String.format("[%s, %s] %s", line, col, msg) ;
        throw new CSVParseException(msg) ;
    }
}
