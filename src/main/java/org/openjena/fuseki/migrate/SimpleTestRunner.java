/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.migrate;

import java.util.ArrayList ;
import java.util.List ;

import org.junit.runner.Description ;
import org.junit.runner.JUnitCore ;
import org.junit.runner.Result ;
import org.junit.runner.notification.Failure ;
import org.openjena.fuseki.TestDatasetHTTP ;

public class SimpleTestRunner
{
    public static void main(String...argv)
    {
        //org.junit.runner.JUnitCore.
        JUnitCore core = new JUnitCore() ;

        argv = new String[] {TestDatasetHTTP.class.getName()} ;
        
        List<Class<?>> classes= new ArrayList<Class<?>>();
        
        
        List<Failure> missingClasses= new ArrayList<Failure>();
        for (String each : argv)
            try {
                classes.add(Class.forName(each));
            } catch (ClassNotFoundException e) {
                System.out.println("Could not find class: " + each);
                Description description= Description.createSuiteDescription(each);
                Failure failure= new Failure(description, e);
                missingClasses.add(failure);
            }
        
        core.addListener(new TextListenerOneLine(System.out)) ;
        Result result = core.run(classes.toArray(new Class<?>[0])) ;
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */