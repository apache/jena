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

package com.hp.hpl.jena.shared.wg;


import java.io.*;
import java.net.*;

/**
 *
 * In test cases we cannot open all the input files
 * while creating the test suite, but must defer the
 * opening until the test is actually run.
 */
class LazyURLInputStream extends LazyInputStream {

    private URL url;
    /** Creates new LazyZipEntryInputStream */
    LazyURLInputStream(URL url) {
      //  System.err.println(name);
        this.url = url;
    }
    
    @Override
    InputStream open() throws IOException {
    	URLConnection conn = url.openConnection();
    //	System.err.println(conn.getClass().getName());
    	
    	return conn.getInputStream();
    }
    
    

}
