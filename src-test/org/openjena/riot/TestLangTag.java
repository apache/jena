/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import java.util.Arrays;
import java.util.Collection;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openjena.riot.LangTag ;


@RunWith(Parameterized.class)

public class TestLangTag
{
    
    
    @Parameters public static Collection<Object[]> data()
    
    {
        return Arrays.asList(new Object[][] {
            // input, language, script, region, variant, extension 
            //{"en", new String[]{"en", "junk", null, null }},
            
            {"en",                  "en",               new String[]{"en", null, null, null, null}},
            {"en-uk",               "en-UK",            new String[]{"en", null, "UK", null, null}},
            {"es-419",              "es-419",           new String[]{"es", null, "419", null, null}},
            {"zh-Hant",             "zh-Hant",          new String[]{"zh", "Hant", null, null, null}},
            {"sr-Latn-CS",          "sr-Latn-CS",       new String[]{"sr", "Latn", "CS", null, null}},
            {"sl-nedis",            "sl-nedis",         new String[]{"sl", null, null, "nedis", null}},
            {"sl-IT-nedis",         "sl-IT-nedis",      new String[]{"sl", null, "IT", "nedis", null}},
            {"sl-Latn-IT-nedis",    "sl-Latn-IT-nedis", new String[]{"sl", "Latn", "IT", "nedis", null}},
            {"de-CH-x-Phonebk",     "de-CH-x-Phonebk",  new String[]{"de", null, "CH", null, "x-Phonebk"}},
            {"zh-cn-a-myExt-x-private", "zh-CN-a-myExt-x-private", new String[]{"zh", null, "CN", null, "a-myExt-x-private"}},

            {"12345", "12345", null},
//            
//            {"en", "en", new String[]{"en", null, null, null, null}},
//            {"en-uk", "en-UK", new String[]{"en", null, "UK", null, null}},
//            {"es-419", "es-419", new String[]{"es", null, "419", null, null}},
//            {"zh-hant", "zh-Hant", new String[]{"zh", "Hant", null, null, null}},
//            {"sr-latn-cs", "sr-Latn-CS", new String[]{"sr", "Latn", "CS", null, null}},
//            {"sl-nedis", "sl-nedis", new String[]{"sl", null, null, "nedis", null}},
//            {"sl-IT-nedis", "sl-IT-nedis", new String[]{"sl", null, "IT", "nedis", null}},
//            {"SL-latn-it-Nedis", "sl-Latn-IT-nedis", new String[]{"sl", "Latn", "IT", "nedis", null}},
//            {"12345", "12345", (String[])null},
            });
    }

    private String input ;
    private String[] parts ;
    private String output ;
    
    public TestLangTag(String input, String output, String[] parts)
    { 
        this.input = input ;
        this.output = output ;
        this.parts = parts ;
    }
   
    @Test
    public void verify()
    {
        //System.out.println(input+" ==> "+output) ;
        
        String[] parts = LangTag.parse(input) ;
        String output = LangTag.canonical(input) ;
        Assert.assertArrayEquals(this.parts, parts) ;
        Assert.assertEquals(this.output, output) ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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