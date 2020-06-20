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

package org.apache.jena.atlas.csv;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestCSVParser
{
    String[] row1 = {} ;
    String[] row2 = { "" } ;
    String[] row3 = { "a", "b" } ;
    String[] row4 = { "123", "\"aa\"", "'bb'", "\"''\"Z", "A'\"\"'" } ;
    
    @Test public void csv_parse_01() { csv("\n", new String[][] {{""}}) ; }
    @Test public void csv_parse_02() { csv("a\n", new String[][] {{"a"}}) ; }
    @Test public void csv_parse_03() { csv("a,b\n", new String[][] {{"a", "b"}}) ; }
    @Test public void csv_parse_04() { csv(",b\n", new String[][] {{"", "b"}}) ; }
    @Test public void csv_parse_05() { csv("a,\n", new String[][] {{"a", ""}}) ; }
    @Test public void csv_parse_06() { csv(",\n", new String[][] {{"", ""}}) ; }
    @Test public void csv_parse_07() { csv(",,\n", new String[][] {{"", "", ""}}) ; }
    
    @Test public void csv_parse_10() { csv("\n\n", new String[][] { {""}, {""} }) ; }
    @Test public void csv_parse_11() { csv("'aa'\naa\n", new String[][] { {"'aa'"}, {"aa"} }) ; }
    @Test public void csv_parse_12() { csv("\naa", new String[][] { {""}, {"aa"} }) ; }
    @Test public void csv_parse_13() { csv("a,b\nc,d", new String[][] { {"a", "b"}, {"c", "d"} }) ; }
    @Test public void csv_parse_14() { csv("a,b\rc,d", new String[][] { {"a", "b"}, {"c", "d"} }) ; }
    
    
    private void csv(String input, String[][] strings)
    {
        List<List<String>> x = new ArrayList<>() ;
        for ( String[] a : strings )
        {
            List<String> y = new ArrayList<>() ;
            for ( String b : a )
                y.add(b) ;
            x.add(y) ;
        }
        csv(input, x) ;
    }
    
    private static void csv(String input, List<List<String>> answers)
    {
        List<List<String>> x = new ArrayList<>() ;
        CSVParser parser = CSVParser.create(new StringReader(input)) ;
        for (List<String> row : parser) {
            x.add(row) ;
        }
        assertEquals(answers, x) ;
    }
}

