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

package riotcmd;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonParseException ;
import org.apache.jena.atlas.json.JsonValue ;

/** Command to read and print JSON */
public class json
{
    public static void main(String... args)
    {
        if ( args.length == 0 )
            args = new String[] {"-"} ;

        try {
            for ( String fn : args )
            {
                JsonValue json =null ;
                try {
                 json = JSON.readAny(fn) ;
                } catch (JsonParseException ex)
                {
                    String name = fn.equals("-") ? "<stdin>" : fn ; 
                    System.err.println(name+": "+JsonParseException.formatMessage(ex.getMessage(), ex.getLine(), ex.getColumn())) ;
                    continue ;
                }
                JSON.write(IndentedWriter.stdout, json) ;
                IndentedWriter.stdout.ensureStartOfLine() ;
            }
        } finally { IndentedWriter.stdout.flush() ; }
    }

}
