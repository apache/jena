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

package org.apache.jena.fuseki.servlets;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is the data replicator of output
 * streams. These streams sit on top of an already existing output
 * stream (the <i>underlying</i> output stream) which it uses as its
 * basic sink of data, but possibly coping the data along the
 * way or providing additional functionality.
 * <p>
 */

public class ResponseOutputStream extends OutputStream {
    /**
     * The underlying output streams to be replicated.
     */
    protected OutputStream out1;
    protected OutputStream out2;
    public ResponseOutputStream(OutputStream out1, OutputStream out2){
        this.out1 = out1;
        this.out2 = out2;
    }
    /**
     * Writes the specified <code>byte</code> to these output streams.
     * <p>
     * The <code>write</code> method of <code>ResponseOutputStream</code>
     * calls the <code>write</code> method of its underlying output stream,
     * replicating output stream that is, it performs <tt>out1.write(b) </tt>and
     * <tt>out2.write(b)</tt>.
     * <p>
     * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(int b) throws IOException {
        out1.write(b);
        out2.write(b);
    }
    /**
     * Writes <code>b.length</code> bytes to these output streams.
     * <p>
     * The <code>write</code> method of <code>ResponseOutputStream</code>
     * call the one-argument <code>write</code> method of
     * its underlying stream with the single
     * argument <code>b</code>.
     *
     * @param      b   the data to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[]) throws IOException {
        if ( out1 != null ) out1.write(b) ;
        if ( out2 != null ) out2.write(b) ;
    }
}
