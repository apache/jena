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

package org.apache.jena.atlas.web;

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;

public class AcceptList
{
    private List<MediaRange> ranges ;
    /** 
     * Create an empty list of accept items from the give strings.
     * @param acceptStrings
     */
    
    private AcceptList()
    { ranges = new ArrayList<>() ; }

    /**
     * Create a list of accept items from the give strings.
     * @param mediaRanges
     */
    
    public AcceptList(List<MediaRange> mediaRanges)
    { ranges = new ArrayList<>(mediaRanges) ; }
    
    /**
     * Create a list of accept items from the give MediaTypes.
     * @param acceptItems
     */
    
    public AcceptList(MediaRange...acceptItems)
    { ranges = Arrays.asList(acceptItems) ; }
    
    /**
     * Create a list of accept items from the give MediaTypes.
     * @param acceptItems
     */
    
    public static AcceptList create(MediaType...acceptItems)
    { 
        AcceptList accepList = new AcceptList() ;
        for ( MediaType mtype : acceptItems )
            accepList.ranges.add(new MediaRange(mtype)) ;
        return accepList ;
    }        

    /**
     * Create a list of accept items from strings.
     * @param acceptStrings
     */
    
    public static AcceptList create(String... acceptStrings)
    {
        AcceptList accepList = new AcceptList() ;
        for ( String acceptString : acceptStrings )
        {
            accepList.ranges.add( new MediaRange( acceptString ) );
        }
        return accepList ;
    }
    
    /**
     * Parse an HTTP Accept (or etc) header string. 
     * @param headerString
     */
    
    public AcceptList(String headerString)
    {
        try {
            ranges = stringToAcceptList(headerString) ;
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            Log.warn(this, "Unrecognized accept string (ignored): "+headerString) ;
            ranges = new ArrayList<>() ;
        }
    }
    
    private /*public*/ boolean accepts(MediaRange aItem)
    {
        return match(aItem) != null ;
    }
    
    private List<MediaRange> entries()
    {
        return Collections.unmodifiableList(ranges) ;
    }

    private final static MediaRangeCompare comparator = new MediaRangeCompare() ;
    
    /** Find and return a match for a specific MediaType. 
     * Returns the Accept header entry best matched or null.
     */
    public MediaRange match(MediaType offer) {
        // "this" is the client proposal list (Accept header)
        // aItem is one item of the server offer list - a concrete media type (no "*")

        // Search all, find best by specifivity, "q"(quality), and then first occurring if otherwise equal.
        
        MediaRange choice = null ;
        double weight = -1 ;
        int exact = -1 ;
        
        for ( MediaRange acceptItem : ranges )
        {
            if ( acceptItem.accepts(offer) )
            {
                boolean newChoice = false; 
                if ( choice == null ) 
                    // First possibility.
                    newChoice = true ;
                else if ( weight < acceptItem.get_q() )
                    // New possibility has greater weight
                    newChoice = true ;
                else if ( weight == acceptItem.get_q() && exact < acceptItem.subweight() ) 
                    // New possibility has same weight but better exactness.
                    newChoice = true ;
                
                if ( newChoice )
                {
                    choice = acceptItem ;
                    weight = acceptItem.get_q() ;
                    exact = acceptItem.subweight() ;
                    continue ;
                }
                //if ( weight == acceptItem.get_q() && !exact &&  
            }
        }
        if ( choice == null )
            return null ;
        return choice ;
    }

    /** Find the best thing in offer list 8sever side) matching the proposal (client, Accept header). 
     *  "best" means highest q value, with left most being better for same q.
     * 
     * @param proposalList Client list of possibilities
     * @param offerList    Server list of possibilities
     * @return MediaType
     */
    
    static public MediaType match(AcceptList proposalList, AcceptList offerList)
    {
        MediaRange cause = null ;
        MediaRange choice = null ;  // From the proposalList
        double weight = -1 ;
        int exactness = -1 ;

        for ( MediaRange offer : offerList.entries() )
        {
            MediaRange m = proposalList.match(offer) ;
            if ( m == null )
                continue ;
            boolean newChoice = false ;
            
            if ( choice == null )
                newChoice = true ;
            else if ( weight < m.get_q() )
                newChoice = true ;
            else if ( weight == m.get_q() && ( exactness < m.subweight() ) )
                newChoice = true ;
            
            if ( newChoice ) {
                choice = offer ;
                weight = m.get_q() ;
                exactness = m.subweight() ;
            }
        }
        
        
        if ( choice == null )
            return null ;
        return new MediaType(choice);
    }
    
    public MediaRange first()
    {
        MediaRange choice = null ;
        for ( MediaRange acceptItem : ranges )
        {
            if ( choice != null && choice.get_q() >= acceptItem.get_q() )
                continue ;
            choice = acceptItem ;
        }
        return choice ;
    }
    
    @Override
    public String toString() { return ranges.toString() ; }
    
    private static List<MediaRange> stringToAcceptList(String s)
    {
        List<MediaRange> ranges = new ArrayList<>() ;
        if ( s == null )
            return ranges ;

        String[] x = s.split(",") ;
        for ( String aX : x )
        {
            if ( aX.equals( "" ) )
            {
                continue;
            }
            MediaRange mType = new MediaRange( aX );
            ranges.add( mType );
        }
        return ranges ;
    }
    
    private static class MediaRangeCompare implements Comparator<MediaRange>
    {
        @Override
        public int compare(MediaRange mType1, MediaRange mType2)
        {
            int r = Double.compare(mType1.get_q(), mType2.get_q()) ;
            
            if ( r == 0 )
                r = subCompare(mType1.getType(), mType2.getType()) ;
            
            if ( r == 0 )
                r = subCompare(mType1.getSubType(), mType2.getSubType()) ;
            
//            if ( r == 0 )
//            {
//                // This reverses the input order so that the rightmost elements is the
//                // greatest and hence is the first mentioned in the accept range.
//                
//                if ( mType1.posn < mType2.posn )
//                    r = +1 ;
//                if ( mType1.posn > mType2.posn )
//                    r = -1 ;
//            }
            
            // The most significant sorts to the first in a list.
            r = -r ;
            return r ;
        }
        
        public int subCompare(String a, String b)
        {
            if ( a == null )
                return 1 ;
            if ( b == null )
                return -1 ;
            if ( a.equals("*") && b.equals("*") )
                return 0 ;
            if ( a.equals("*") )
                return -1 ;
            if ( b.equals("*") )
                return 1 ;
            return 0 ;
        }
    }
}
