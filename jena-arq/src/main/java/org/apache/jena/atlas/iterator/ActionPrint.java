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

package org.apache.jena.atlas.iterator;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;

public class ActionPrint <T extends Printable> implements Action<T> 
{
    private boolean first = true ;
    private IndentedWriter out ;
    private String sep ; 
    
    public ActionPrint(IndentedWriter out, String sep) { this.out = out ; this.sep = sep ; }
    public ActionPrint(IndentedWriter out) { this(out, " ") ; }
    
    @Override
    public void apply(Printable item)
    {
        if ( ! first && sep != null )
            out.print(sep) ;
        first = false ;
        item.output(out) ;
    }
}
