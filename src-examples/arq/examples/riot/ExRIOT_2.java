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

package arq.examples.riot;

import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.InputStream ;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.atlas.lib.SinkNull ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.SysRIOT ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.system.RiotLib ;

import com.hp.hpl.jena.sparql.core.Quad ;

/** Example of using RIOT directly.
 */
public class ExRIOT_2
{
    public static void main(String...argv) throws FileNotFoundException
    {
        // Ensure RIOT loaded.
        SysRIOT.wireIntoJena() ;

        Sink<Quad> noWhere = new SinkNull<Quad>() ;

        // ---- Parse to a Sink.
        // RIOT controls the conversion from bytes to java chars.
        InputStream in = new FileInputStream("data.trig") ;
        
        RiotReader.parseQuads(in, Lang.TRIG, "http://example/base", noWhere) ;
        
        
        // --- Or create a parser and do the parsing as separate steps.
        String baseURI = "http://example/base" ;
            
        in = new FileInputStream("data.trig") ;
        LangRIOT parser = RiotReader.createParserQuads(in, Lang.TRIG, "http://example/base", noWhere) ;
        
        // Parser to first error or warning.
        ErrorHandler errHandler = ErrorHandlerFactory.errorHandlerStrict ;

        // Now enable stricter checking, even N-TRIPLES must have absolute URIs. 
        ParserProfile profile = RiotLib.profile(baseURI, true, true, errHandler) ;

        // Just set the error handler.
        parser.getProfile().setHandler(errHandler) ;
        
        // Or replave the whole parser profile.
        parser.setProfile(profile) ;

        // Do the work.
        parser.parse() ;
    }
}
