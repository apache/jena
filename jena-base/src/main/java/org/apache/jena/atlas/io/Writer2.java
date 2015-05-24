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

package org.apache.jena.atlas.io;

import java.io.BufferedWriter ;
import java.io.IOException ;
import java.io.Writer ;

import org.apache.jena.atlas.lib.Closeable ;

/** A Writer, without the checked exceptions. */

public class Writer2 extends AWriterBase implements AWriter, Closeable
{
    protected final Writer writer ;

    public static Writer2 wrap(Writer writer)
    {
        if ( writer instanceof BufferedWriter )
            return new Writer2(writer) ;
        if ( writer instanceof BufferingWriter )
            return new Writer2(writer) ;
        
        writer = new BufferingWriter(writer) ;
        return new Writer2(writer) ;
    }
    
    protected Writer2(Writer writer) { this.writer = writer ; }

    @Override
    public void print(char ch)
    { 
        try { writer.write(ch) ; } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void print(String string)
    { 
        try { writer.write(string) ; } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void print(char[] cbuf)
    {
        try { writer.write(cbuf) ; } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void flush()
    {
        try { writer.flush() ; } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void close()
    {
        try { writer.close() ; } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void printf(String fmt, Object... args)
    {
        print(String.format(fmt, args)) ;
    }

    @Override
    public void println(String obj)
    {
        print(obj) ; print("\n") ;
    }

    @Override
    public void println()
    {
        print("\n") ;
    }
    
    @Override
    public String toString() { return writer.toString() ; }
}

