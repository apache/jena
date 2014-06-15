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

import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.sparql.util.Timer ;

public class CmdTokens
{
    
    public static void tokens(final boolean print, final boolean timing, String...args)
    {
        // Turn the node cache off.
        //com.hp.hpl.jena.graph.Node.cache(false) ;

        if ( args.length == 0 )
            args = new String[] {"-"} ;

        String arg = args[0] ;

        if ( arg.equals("--help") || arg.equals("-help") || arg.equals("-h") || arg.equals("--h") ) 
        {
            System.err.println("Usage: stdin | FILE ...") ;
            System.exit(1) ;
        }
        for ( String filename : args )
        {
            InputStream in = IO.openFile(filename) ;
            Tokenizer tokenize = TokenizerFactory.makeTokenizerUTF8(in) ;
            Timer timer = new Timer() ;
            long count = 0 ; 
            timer.startTimer() ;
            for ( ; tokenize.hasNext() ; )
            {
                Token t = tokenize.next() ;
                if ( print )
                    System.out.println(t) ;
                count++ ;
            }
            tokenize.close();
            long millis = timer.endTimer() ;
            if ( timing )
            {
                if ( millis == 0 )
                    System.out.printf("Tokens=%,d : Time=0.00s\n", count) ;
                else
                {
                    double seconds = millis/1000.0 ;
                    System.out.printf("Tokens=%,d : Time=%,.2fs : Rate=%,.2f\n", count, seconds, count/seconds) ;
                }
            }
        }
    }
}
