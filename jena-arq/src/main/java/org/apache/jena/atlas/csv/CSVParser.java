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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.atlas.io.IO;

/** Written specifically to handle SPARQL results CSV files.
 *  Acts as a wrapper for Commons CSV parser.
 */
public class CSVParser implements Iterable<List<String>>
{
    
    private final org.apache.commons.csv.CSVParser parser;
    
    public static CSVParser create(String filename) {
        InputStream input = IO.openFile(filename) ;
        return create(input) ;
    }

    public static CSVParser create(InputStream input) {
        CSVParser parser = new CSVParser(new InputStreamReader(input)) ;
        return parser ; 
    }
    
    public static CSVParser create(Reader input) {
        CSVParser parser = new CSVParser(input) ;
        return parser ; 
    }

    public CSVParser(Reader input) {
        try {
            this.parser = CSVFormat.EXCEL.withQuote('\'').parse(input);
        } catch (IOException e) {
            throw new CSVParseException("Failed to create the CSV parser: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Iterator<List<String>> iterator() {
        List<List<String>> list = new ArrayList<>();
        for (CSVRecord record : parser) {
            List<String> row = new ArrayList<>();
            for (String columnValue : record) {
                row.add(columnValue);
            }
            list.add(row);
        }
        return list.iterator();
    }

    public List<String> parse1() {
        Iterator<List<String>> iterator = iterator();
        if (iterator.hasNext()) 
        {
            final List<String> firstRow = iterator.next();
            return firstRow;
        }
        return null;
    }

    static void exception(String msg, long line, long col) {
        if ( line >= 0 && col > 0 )
            msg = String.format("[%s, %s] %s", line, col, msg) ;
        throw new CSVParseException(msg) ;
    }
}
