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

import java.io.IOException ;
import java.io.InputStream ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.InStreamUTF8 ;
import org.apache.jena.atlas.io.InputStreamBuffered ;

public class utf8
{
    /** Simple program to help hunt down bad UTF-8 encoded characters */
    public static void main(String[] args)
    {
        long INIT_LINE = 1 ; 
        long INIT_COL = 1 ; 

        if ( args.length == 0 )
            args = new String[] {"-"} ;
        
        String label = "" ;
        for ( String fn : args )
        {
            if ( args.length > 1 )
                label = fn+": " ;
            InputStream in = IO.openFile(fn) ;
            in = new InputStreamBuffered(in) ;
            
            long charCount = 0 ;
            long lineNum = INIT_LINE ;
            long colNum = INIT_COL ;
            
            InStreamUTF8 utf8 = null ;
            try {
                utf8 = new InStreamUTF8(in) ;
                for (;;) 
                {
                    int ch = utf8.read() ;
                    if ( ch == -1 )
                        break ;
                    charCount++ ;
                    if (ch == '\n')
                    {
                        lineNum++;
                        colNum = INIT_COL ;
                    } 
                    else
                        colNum++;
                    if ( ! Character.isDefined(ch) )
                        throw new AtlasException(String.format("No such codepoint: 0x%04X", ch)) ;
                }
                System.out.printf("%s: chars = %d , lines = %d\n", fn, charCount, lineNum) ;
            } catch (AtlasException ex)
            {
                System.out.printf(label+"[line=%d, col=%d] %s\n", lineNum, colNum, ex.getMessage()) ;
            }
            catch (IOException ex)
            {
                ex.printStackTrace(System.err) ;
            }
            finally { IO.close(utf8) ; }
        }
    }
}
