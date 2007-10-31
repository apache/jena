/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.queryfinder;



public class FindQueries
{
    static boolean printBrokenQueries = false ;
    static boolean printDuplicateQueries = false ;
    static boolean printCounts = true ;
    
    public static void main(String[] argv) 
    {
//        if ( false )
//        {
//        // Test two files on disk
//            Query q1 = QueryFactory.read("tmp\\query1.rq") ;
//            System.out.println(q1) ;
//            Query q2 = QueryFactory.read("tmp\\query2.rq") ;
//            System.out.println(q2) ;
//        
////        Query q1 = QueryFactory.read("../ARQ/testing/ARQ/Syntax/Syntax-SPARQL/syntax-lists-04.rq") ;
////        Query q2 = QueryFactory.read("../../W3C/DataAccess/tests/data/SyntaxFull/syntax-lists-04.rq") ;
//            System.out.println(q1.hashCode()) ;
//            System.out.println(q2.hashCode()) ;
//            System.out.println(q1.equals(q2)) ;
//            System.exit(0) ;
//        }
//        
//        String s = "/home/afs/Projects/ARQ/testing/ARQ/" ;
//        Map<Query, File> queriesARQ   = findQueries(s) ;
//        findQueries(queriesARQ, "/home/afs/Projects/ARQ/testing/DAWG/") ;
//        
//        s = "/home/afs/W3C/DataAccess/tests" ;
//        Map<Query, File> queriesDAWG  = findQueries(s) ;
//        queriesDAWG  = findQueries(s) ;
//        
////        for ( Query q : queriesARQ.keySet() )
////        {
////            System.out.println(queriesARQ.get(q)) ;
////        }
////        System.out.println() ;
////        System.out.println() ;
//        
//        int missing = 0 ;
//        int same = 0 ;
//        for ( Query q : queriesDAWG.keySet() )
//        {
//            if ( ! queriesARQ.containsKey(q) )
//            {
//                File f = queriesDAWG.get(q) ;
//                String x = findShortName(queriesARQ, f.getName()) ;
//                
//                System.out.println(f) ;
//                if ( x == null )
//                    System.out.println("*** No match") ;
//                else
//                {
//                    System.out.println("??? "+x) ;
//                    Query query2 = QueryFactory.read(x) ;
//                    System.out.println(q) ;
//                    System.out.println("--------") ;
//                    System.out.println(query2) ; 
//                    
//                }
//                missing++ ;
//            }
//            else
//            {
//                //System.out.println("Same: "+queriesDAWG.get(q)) ;
//                same++ ;
//            }
//        }
//        System.out.printf("Same:    %d\n", same) ;
//        System.out.printf("Missing: %d\n", missing) ;
//    }
//    
//
//    static String findShortName(Map<Query, File> queries, String shortName)
//    {
//        
//        for ( Entry <Query, File> e : queries.entrySet() )
//        {
//            if ( e.getValue().getName().equals(shortName) )
//                return e.getValue().getPath() ;
//        }
//        return null ;
//    }
//    
//    
//    static int countAll = 0;
//    static int countValid = 0;
//
//    static Map<Query, File> findQueries(String s)
//    {
//        final Map<Query, File> queries = new HashMap<Query, File>(500) ;
//        findQueries(queries, s) ;
//        return queries ;
//    }
//    
//    static void findQueries(final Map<Query, File> queries, String s)
//    {        countAll = 0;
//        countValid = 0;
//        Action a = new Action()
//        {
//            public void action(File f)
//            {
//                try
//                {
//                    countAll++ ;
//                    String fn = f.toURI().toString() ;
//                    Query q = QueryFactory.read(fn) ;
//                    if ( printDuplicateQueries && queries.containsKey(q) )
//                        System.out.printf("Duplicate: %s/%s\n",
//                                          queries.get(q).getName(),
//                                          f.getName()) ;
//                    queries.put(q, f) ;
//                    countValid++ ;
//                } 
////                catch (MalformedURLException ex)
////                {
////                    ex.printStackTrace();
////                }
//                catch (QueryParseException x)
//                {
//                    if (printBrokenQueries) 
//                        System.out.println("Duff query: "+f.getName()) ;
//                }
//            }
//        } ;
//        Finder.find(s, "\\.rq$", a) ;
//        if ( printCounts )
//            System.out.printf("%d/%d [%d]\n", countValid, countAll, queries.size()) ;
    }

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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