/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import java.util.regex.Matcher ;
import java.util.regex.Pattern ;


/**
 * Language tags: support for parsing and canonicalization of case. 
 * Grandfathered forms ("i-") are left untouched.
 * Unsupported or syntactically illegal forms are handled in
 * canonicalization by doing nothing.
 * <ul>
 * <li>Language tags syntax: <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a></li>
 * <li>Matching Language tags: <a href="http://www.ietf.org/rfc/rfc4647.txt">RFC 4647</a></li>
 * <li>Language tags syntax: <a href="http://www.ietf.org/rfc/rfc5646.txt">RFC 5646</a></li>
 * </ul>
 */
  
public class LangTag
{
    // See also http://tools.ietf.org/html/rfc5646 - irregular lang tags
    
    /** Index of the language part */
    public static final int idxLanguage     = 0 ;
    /** Index of the script part */ 
    public static final int idxScript       = 1 ;
    /** Index of the region part */
    public static final int idxRegion       = 2 ;
    /** Index of the variant part */
    public static final int idxVariant      = 3 ;
    /** Index of all extensions */
    public static final int idxExtension    = 4 ;
    
    private static final int partsLength    = 5 ;
    
    private LangTag(){}
    
    // ABNF is defined in http://www.ietf.org/rfc/rfc4234.txt

    /*
    In this format, all non-initial two-letter subtags are uppercase, all
    non-initial four-letter subtags are titlecase, and all other subtags
    are lowercase.
    */

  
  /*
   *     <li>ABNF definition: <a href="http://www.ietf.org/rfc/rfc4234.txt">RFC 4234</a></li>

 Language-Tag  = langtag
               / privateuse             ; private use tag
               / grandfathered          ; grandfathered registrations

 langtag       = (language
                  ["-" script]
                  ["-" region]
                  *("-" variant)
                  *("-" extension)
                  ["-" privateuse])

 language      = (2*3ALPHA [ extlang ]) ; shortest ISO 639 code
               / 4ALPHA                 ; reserved for future use
               / 5*8ALPHA               ; registered language subtag

 extlang       = *3("-" 3ALPHA)         ; reserved for future use

 script        = 4ALPHA                 ; ISO 15924 code

 region        = 2ALPHA                 ; ISO 3166 code
               / 3DIGIT                 ; UN M.49 code

 variant       = 5*8alphanum            ; registered variants
               / (DIGIT 3alphanum)

 extension     = singleton 1*("-" (2*8alphanum))

 singleton     = %x41-57 / %x59-5A / %x61-77 / %x79-7A / DIGIT
               ; "a"-"w" / "y"-"z" / "A"-"W" / "Y"-"Z" / "0"-"9"
               ; Single letters: x/X is reserved for private use

 privateuse    = ("x"/"X") 1*("-" (1*8alphanum))

 grandfathered = 1*3ALPHA 1*2("-" (2*8alphanum))
                 ; grandfathered registration
                 ; Note: i is the only singleton
                 ; that starts a grandfathered tag

 alphanum      = (ALPHA / DIGIT)       ; letters and numbers
                  

   */

    private static final String languageRE_1    = "(?:[a-zA-Z]{2,3}(?:-[a-zA-Z]{3}){0,3})" ; //including extlang
    private static final String languageRE_2    = "[a-zA-Z]{4}" ;
    private static final String languageRE_3    = "[a-zA-Z]{5,8}" ;
    private static final String language        = "(?:"+languageRE_1+"|"+languageRE_2+"|"+languageRE_3+")" ;

    private static final String script          = "[a-zA-Z]{4}" ;
    private static final String region          = "[a-zA-Z]{2}|[0-9]{3}" ;
    private static final String variant         = "[a-zA-Z0-9]{5,8}" ;
    private static final String extension1      = "(?:[a-zA-Z0-9]-[a-zA-Z0-9]{2,8})" ;
    private static final String extension       = extension1+"(?:-"+extension1+")*" ;
    
//    private static final String singleton = null ;
//    private static final String privateuse = null ;
//    private static final String grandfathered = null ;

    private static final String langtag = String.format("^(%s)(?:-(%s))?(?:-(%s))?(?:-(%s))?(?:-(%s))?$"
                                                        ,language
                                                        ,script
                                                        ,region
                                                        ,variant
                                                        ,extension
                                                        ) ;
    
    // Private use forms "x-"
    private static final String privateuseRE    = "^[xX](-[a-zA-Z0-9]{1,8})*$" ; 
    // In general, this can look like a langtag but there are no registered forms that do so.
    // This is for the "i-" forms only.
    private static final String grandfatheredRE = "i(?:-[a-zA-Z0-9]{2,8}){1,2}" ;  
    
    private static Pattern pattern              = Pattern.compile(langtag) ;
    private static Pattern patternPrivateuse    = Pattern.compile(privateuseRE) ;
    private static Pattern patternGrandfathered = Pattern.compile(grandfatheredRE) ; 
    
    /** Parse a langtag string and return it's parts in canonical case.
     *  See constants for the array contents.  Parts not present cause a null
     *  in the return array. 
     *  @return Langtag parts, or null if the input string does not poarse as a lang tag.  
     */
    public static String[] parse(String languageTag)
    {
        String[] parts = new String[partsLength] ;
        Matcher m = pattern.matcher(languageTag) ;
        if ( ! m.find() )
        {
            m = patternPrivateuse.matcher(languageTag) ;
            if ( m.find() )
            {
                // Place in the "extension" part
                parts[idxExtension] = m.group(0) ;
                return parts ;
            }
                
            m = patternGrandfathered.matcher(languageTag) ;
            
            if ( m.find() )
            {
                // Place in the "extension" part
                parts[idxExtension] = m.group(0) ;
                return parts ;
            }
            
            // Give up.
            return null ;
        }
            
        int gc = m.groupCount() ;
        for ( int i = 0 ; i < gc ; i++ )
            parts[i] = m.group(i+1) ;
        
        parts[idxLanguage]  = lowercase(parts[idxLanguage]) ;
        parts[idxScript]    = strcase(parts[idxScript]) ;
        parts[idxRegion]    = strcase(parts[idxRegion]) ;
        parts[idxVariant]   = strcase(parts[idxVariant]) ;
        //parts[idxExtension] = strcase(parts[idxExtension]) ;  // Leave extensions alone.
        return parts ;
    }

    /** Canonicalize with the rules of RFC 4646 */
    public static String canonical(String str)
    {
        if ( str == null )
            return null ;
        String[] parts = parse(str) ;
        String x = canonical(parts) ;
        if ( x == null )
            return str ;
        return x ;
    }
    
    /** Canonicalize with the rules of RFC 4646
    "In this format, all non-initial two-letter subtags are uppercase, all
    non-initial four-letter subtags are titlecase, and all other subtags
    are lowercase."
    In addition, leave extensions unchanged.
     */
    public static String canonical(String[] parts)
    {
        if ( parts == null )
            return null ;
        
        if ( parts[0] == null )
        {
            // Grandfathered
            return parts[idxExtension] ;
        }

        StringBuilder sb = new StringBuilder() ;
        sb.append(parts[0]) ;
        for ( int i = 1 ; i < parts.length ; i++ )
        {
            if ( parts[i] != null )
            {
                sb.append("-") ;
                sb.append(parts[i]) ;
            }
        }
        return sb.toString(); 
    }
    
    private static String strcase(String string)
    {
        if ( string == null ) return null ;
        if ( string.length() == 2 ) return  uppercase(string) ;
        if ( string.length() == 4 ) return  titlecase(string) ;
        return lowercase(string) ;
    }

    private static String lowercase(String string)
    {
        if ( string == null ) return null ;
        return string.toLowerCase() ;
    }

    private static String uppercase(String string)
    {
        if ( string == null ) return null ;
        return string.toUpperCase() ;
    }

    private static String titlecase(String string)
    {
        if ( string == null ) return null ;
        char ch1 = string.charAt(0) ;
        ch1 = Character.toUpperCase(ch1) ;
        string = string.substring(1).toLowerCase() ;
        return ch1+string ;
    }

    // ----------
    
    public static void main(String ... args) //throws IOException
    {
        // Test data.
        String[] tags = {
            "en", "en-uk", "es-419", "zh-Hant", 
            "sr-Latn-CS" , "sl-nedis", "sl-IT-nedis" , "sl-Latn-IT-nedis",
            "de-CH-x-Phonebk",
            "zh-cn-a-myExt-x-private",
            "x-foo",
            "x-kx-kx-kx",
            "i-whatever",
            "12345"} ;
        
        if ( args.length == 0 )
            args = tags ;
        
        for ( String str : args )
        {
            String[] parts = LangTag.parse(str) ;
            System.out.print("\""+str+"\"") ;
            boolean first =true ;

            if ( parts == null )
            {
                System.out.print("  ==>  Illegal") ;
            }
            else
            {
                String canonical = canonical(parts) ;
                System.out.print("  ==>  \""+canonical+"\"") ;

                System.out.print(" (") ;
                for ( String s : parts )
                {
                    if ( ! first )
                        System.out.print(", ") ;
                    first = false ;
                    if ( s == null )
                        System.out.print("null") ;
                    else
                        System.out.print("\""+s+"\"") ;
                }
                System.out.print(")") ;
            }
            System.out.println() ;
        }
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