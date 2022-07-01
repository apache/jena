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

package org.apache.jena.atlas.io;

import java.io.IOException;
import java.io.Reader;

/** Machinery to add Reader functionality to a CharStream.
 * No {@code synchronized} is used for {@link #read()}.
 */
public abstract class CharStreamReader extends Reader implements CharStream {

    private boolean isClosed = false;

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if ( isClosed )
            return -1;
        for ( int i = off ; i < off + len ; i++ ) {
            int x = advance();
            if ( x == -1 ) {
                close();
                if ( i == off )
                    return -1;
                return (i - off);
            }
            cbuf[i] = (char)x;
        }
        return len;
    }

    @Override
    public int read() throws IOException {
        if ( isClosed )
            return -1;
        return advance();
    }

    @Override
    public void close() throws IOException {
        if ( isClosed )
            return;
        isClosed = true;
        closeStream();
    }

    @Override
    public boolean ready() throws IOException {
        return !isClosed;
    }

    @Override
    public abstract int advance();

    @Override
    public abstract void closeStream();
}
