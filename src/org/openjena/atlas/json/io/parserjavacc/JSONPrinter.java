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

package org.openjena.atlas.json.io.parserjavacc;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.json.io.JSONHandler ;

/** Parser debugging */
public class JSONPrinter implements JSONHandler
{

    IndentedWriter out ;  
    
    //@Override
    public void startParse()
    {
        out = new IndentedWriter(System.out, true) ; 
        //out.setPadString("> ") ;
    }

    //@Override
    public void finishParse()
    {
        out.flush() ;
    }

    //@Override
    public void startObject()
    { 
        out.println(">>Object") ;
        out.incIndent() ;
    }

    //@Override
    public void finishObject()
    {
        out.decIndent() ;
        out.println("<<Object") ;
    
    }
    
    //@Override
    public void startPair()
    { 
        out.println(">Pair") ;
        out.incIndent() ;
    }

    //@Override
    public void keyPair()
    {}

    //@Override
    public void finishPair()
    { 
        out.decIndent() ;
        out.println("<Pair") ;
    }
    
    //@Override
    public void startArray()
    {
        out.println(">>Array") ;
        out.incIndent() ;
    }

    //@Override
    public void element()
    {}

    //@Override
    public void finishArray()
    {
        out.decIndent() ;
        out.println("<<Array") ;
    }

    //@Override
    public void valueBoolean(boolean b)
    {
        out.println("Boolean: "+b) ;
    }

    //@Override
    public void valueDecimal(String image)
    {
        out.println("Decimal: "+image) ;
    }

    //@Override
    public void valueDouble(String image)
    {
        out.println("Double: "+image) ;
    }

    //@Override
    public void valueInteger(String image)
    {
        out.println("Integer: "+image) ;
    }

    //@Override
    public void valueNull()
    {
        out.println("Null") ;
    }

    //@Override
    public void valueString(String image)
    {
        out.println("String: "+image) ;
    }

}
