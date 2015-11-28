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

import java.io.StringWriter ;

/** IndentLineBuffer is a buffer that records an indent level 
 *  and uses that to insert a prefix at each line.
 *  It can also insert line numbers at the beginning of lines.
 */

public class IndentedLineBuffer extends IndentedWriter
{
    StringWriter sw ;
    public IndentedLineBuffer() { this(false) ; }
    
    public IndentedLineBuffer(boolean withLineNumbers)
    {
        super(new StringWriter(), withLineNumbers) ;
        sw = (StringWriter)super.out ;
    }
    
    public StringBuffer getBuffer() { return sw.getBuffer(); }
    
    public String asString() { return sw.toString() ; }
    @Override
    public String toString() { return asString() ; }

    // Names more usually used for a buffer.
    public void append(String fmt, Object... args) { printf(fmt, args) ; }
    public void append(char ch)  { print(ch) ;}
    
    public void clear() { sw.getBuffer().setLength(0) ; }
}
