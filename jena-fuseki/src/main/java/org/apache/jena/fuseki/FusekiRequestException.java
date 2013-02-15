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

package org.apache.jena.fuseki;

import org.apache.jena.web.HttpSC ;


public class FusekiRequestException extends FusekiException
{
    public static FusekiRequestException create(int code, String msg)
    {
        if ( code == HttpSC.NOT_FOUND_404 )
            return new FusekiNotFoundException(msg) ;
        return new FusekiRequestException(code, msg) ;
    }
    
    private final int statusCode ;
    private final String responseMessage ;
    protected FusekiRequestException(int code, String msg)
    {
        super(msg) ;
        this.statusCode = code ;
        responseMessage = msg ;
    }
    
    public int getStatusCode()
    {
        return statusCode ;
    }

    public String getResponseMessage()
    {
        return responseMessage ;
    }

    @Override
    public String toString()
    {
        return "HTTP: "+statusCode+" "+getMessage() ;
    }
}
