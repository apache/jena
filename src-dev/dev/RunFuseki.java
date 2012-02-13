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

package dev;

import org.apache.jena.fuseki.FusekiCmd ;

public class RunFuseki
{
    public static void main(String[] args) throws Exception
    {
        FusekiCmd.main("--config=config.ttl") ; System.exit(0) ;
        main1() ;
    }
    
    private static void main1() throws Exception
    {
        FusekiCmd.main(
                    //"-v", 
                    //"--debug",
                    //"--update",
                    //"--timeout=1000,5000",
                    //"--set=arq:queryTimeout=1000",
                    //"--port=3030",
                    //"--mgtPort=3031",
                    //"--host=localhost",
                    //"--mem",
                    //"--loc=DB",
                    "--file=D.ttl",
                    //"--gzip=no",
                    //"--desc=desc.ttl",
                    //--pages=
                    "/ds"
                    ) ;
        System.exit(0) ;
    }

}
