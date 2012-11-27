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

import java.io.IOException ;
import java.io.Reader ;

/** A PeekReaderSource that does no buffering - just wraps a reader. */
public final class CharStreamBasic extends CharStreamReader
{
    private Reader reader ;

    CharStreamBasic(Reader reader)
    {
        this.reader = reader ;
    }
    
    @Override
    public int advance()
    {
        try
        {
            return reader.read() ;
        } catch (IOException ex)
        {
            ex.printStackTrace();
            return -1 ;
        }
    }

    @Override
    public void closeStream()
    {
        try
        {
            reader.close() ;
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
