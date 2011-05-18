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

package org.apache.jena.larq;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.JUnit4TestAdapter;

import org.apache.jena.larq.examples.ExLucene1;
import org.apache.jena.larq.examples.ExLucene2;
import org.apache.jena.larq.examples.ExLucene3;
import org.apache.jena.larq.examples.ExLucene4;
import org.apache.jena.larq.examples.ExLucene5;
import org.junit.Test;


public class TestLARQ_Examples
{
    // Check the LARQ examples at least run without problems.
    
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestLARQ_Examples.class) ;
    }

    
    @Test public void larq_example_1() throws Exception
    { 
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene1.main(null) ;
        } finally { System.setOut(pOut) ; }
    }

    @Test public void larq_example_2() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene2.main(null) ;
        } finally { System.setOut(pOut) ; }
    }

    @Test public void larq_example_3() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene3.main(null) ;
        } finally { System.setOut(pOut) ; }
    }

    @Test public void larq_example_4() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene4.main(null) ;
        } finally { System.setOut(pOut) ; }
    }
    @Test public void larq_example_5() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene5.main(null) ;
        } finally { System.setOut(pOut) ; }
    }
}
