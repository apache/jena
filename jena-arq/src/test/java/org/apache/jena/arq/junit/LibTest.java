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

package org.apache.jena.arq.junit;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;

public class LibTest {

    /** Print a multiline string, with line numbers. */
    public static void printString(String str) {
        System.out.printf("====          1         2         3         4\n");
        System.out.printf("====  12345789_123456789_123456789_123456789_\n");
        String[] x = str.split("\n");
        for ( int i = 0 ; i < x.length ; i++ ) {
            System.out.printf("%2d -- %s\n", i+1, x[i]);
        }
    }

    /** Print a (small) file, with line numbers. The file is read into memory. */
    public static void printFile(String filenameOrIRI) {
        String filename = IRILib.IRIToFilename(filenameOrIRI);
        String x = IO.readWholeFileAsUTF8(filename);
        printString(x) ;
    }
}
