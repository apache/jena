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

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;
import java.util.Iterator ;
import java.util.List ;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat ;
import org.apache.commons.csv.CSVRecord ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;

/** 
 *  Wrapper for Commons CSV parser.
 */
public class CSVParser implements Iterable<List<String>>
{
    private final org.apache.commons.csv.CSVParser parser;
    private final Iterator<CSVRecord> iterator ;
    
    public static CSVParser create(String filename) {
        InputStream input = IO.openFile(filename) ;
        return create(input) ;
    }

    public static CSVParser create(InputStream input) {
        CSVParser parser = new CSVParser(IO.asBufferedUTF8(input)) ;
        return parser ; 
    }

    /** Be careful about charsets */
    public static CSVParser create(Reader input) {
        CSVParser parser = new CSVParser(input) ;
        return parser ; 
    }

    private CSVParser(Reader input) {
        try {
            this.parser = CSVFormat.RFC4180.parse(input);
            this.iterator = parser.iterator() ;
        } catch (IOException e) {
            throw new CSVParseException("Failed to create the CSV parser: " + e.getMessage(), e);
        }
    }
    
    private static Function<CSVRecord, List<String>> transform = rec -> recordToList(rec) ;
    
    @Override
    public Iterator<List<String>> iterator() {
        return Iter.map(iterator, transform) ;
    }

    public List<String> parse1() {
        if (iterator.hasNext())
             return recordToList(iterator.next()) ;
        return null;
    }

    private static List<String> recordToList(CSVRecord record) {
        return Iter.toList(record.iterator()) ;
    }
}
