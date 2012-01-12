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

package org.openjena.atlas.io;

import java.io.IOException ;
import java.io.Writer ;

import org.openjena.atlas.lib.Closeable ;

/** A Writer, without the checked exceptions. */

public final class Writer2 implements Closeable
{
    private final Writer writer ;

    public static Writer2 wrap(Writer writer) { return new Writer2(writer) ; }
    
    public Writer2(Writer writer) { this.writer = writer ; }
    

    public void output(int ch)
    { try { writer.write(ch) ; } catch (IOException ex) { IO.exception(ex) ; } }

    public void output(String string)
    { try { writer.write(string) ; } catch (IOException ex) { IO.exception(ex) ; } }

    public void output(String string, int off, int len) throws IOException
    { try { writer.write(string, off, len) ; } catch (IOException ex) { IO.exception(ex) ; } }
    
    public void output(char[] cbuf) throws IOException
    { try { writer.write(cbuf) ; } catch (IOException ex) { IO.exception(ex) ; } }

    public void output(char[] cbuf, int off, int len) throws IOException
    { try { writer.write(cbuf, off, len) ; } catch (IOException ex) { IO.exception(ex) ; } }
    
    public void flush()
    { try { writer.flush() ; } catch (IOException ex) { IO.exception(ex) ; } }

    @Override
    public void close()
    { try { writer.close() ; } catch (IOException ex) { IO.exception(ex) ; } }

}

