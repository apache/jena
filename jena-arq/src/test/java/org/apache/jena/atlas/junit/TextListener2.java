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

package org.apache.jena.atlas.junit;

import java.io.PrintStream ;

import org.junit.internal.TextListener ;
import org.junit.runner.Description ;
import org.junit.runner.notification.Failure ;

public class TextListener2 extends TextListener
{
    private PrintStream out ;
    int count = 0 ;

    public TextListener2(PrintStream writer)
    {
        super(writer) ;
        this.out = writer ;
    }
    
    @Override
    public void testRunStarted(Description description)
    {
        //count = 0 ;
    }
    
    @Override
    public void testStarted(Description description) {
        newline() ;
        out.append('.');
    }

    private void newline()
    {
        if ( count != 0 && count%50 == 0 )
            out.println();
        count++ ;
    }

    @Override
    public void testFailure(Failure failure) {
        newline() ;
        out.append('E');
    }

    @Override
    public void testIgnored(Description description) {
        newline() ;
        out.append('I');
    }
    
}
