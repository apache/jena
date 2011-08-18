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

package tx.api;

import org.openjena.atlas.lib.Closeable ;

import com.hp.hpl.jena.tdb.ReadWrite ;

/** Interface that encapulsated begin/abort|commit/close.
 * <p>The read lifcycle is:
 * <pre>  begin(READ) ... close()</pre>
 * <p>The write lifcycle is:
 * <pre>  begin(WRITE) ... abort() or commit() ... close</pre>
 * 
 */
public interface Transactional extends Closeable 
{
    public void begin(ReadWrite readWrite) ;
    public void commit() ;
    public void abort() ;
    @Override
    public void close() ;
}
