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

package org.apache.jena.atlas.io ;

import java.io.IOException ;
import java.io.Reader ;

/** Machinary to add Reader functionality to a CharStream */
public abstract class CharStreamReader extends Reader implements CharStream {
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        for ( int i = 0 ; i < len ; i++ ) {
            int x = advance() ;
            if ( x == -1 )
                return (i == 0) ? -1 : i ;
            cbuf[i] = (char)x ;
        }
        return len ;
    }

    @Override
    public int read() throws IOException {
        return advance() ;
    }

    @Override
    public void close() throws IOException {
        closeStream() ;
    }

    @Override
    public abstract int advance() ;

    @Override
    public abstract void closeStream() ;
}
