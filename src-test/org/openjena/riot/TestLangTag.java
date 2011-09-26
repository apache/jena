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
