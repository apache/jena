/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.conneg;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.List ;

import org.openjena.atlas.logging.Log ;

public class AcceptList
{
    private List<MediaRange> ranges ;
    private List<MediaRange> sortedRanges = null ;
    
    /** 
     * Create an empty list of accept items from the give strings.
     * @param acceptStrings
     */
    
    private AcceptList()
    { ranges = new ArrayList<MediaRange>() ; }

    /**
     * Create a list of accept items from the give strings.
     * @param mediaRanges
     */
    
    public AcceptList(List<MediaRange> mediaRanges)
    { ranges = new ArrayList<MediaRange>(mediaRanges) ; }
    
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
    
    public AcceptList(MediaType...acceptItems)
    { 
        ranges = new ArrayList<MediaRange>() ;
        for ( MediaType mtype : acceptItems )
            ranges.add(new MediaRange(mtype)) ;
    }        

    /**
     * Create a list of accept items from strings.
     * @param acceptStrings
     */
    
    public AcceptList(String... acceptStrings)
    {
        ranges = new ArrayList<MediaRange>() ;
        for ( int i = 0 ; i < acceptStrings.length ; i++ )
            ranges.add(new MediaRange(acceptStrings[i])) ;
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
            ranges = new ArrayList<MediaRange>() ;
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
    
    /** Find and return a match for a MediaRange */
    public MediaRange match(MediaRange aItem)
    {
        ensureSorted() ;
        for ( MediaRange acceptItem : sortedRanges )
        {
            if ( acceptItem.accepts(aItem) )
            {
                // Return the more grounded term
                // E.g. i = text/plain ; aItem = text/*
                
                if ( aItem.moreGroundedThan(acceptItem) )
                    acceptItem = new MediaRange(aItem) ;
                return acceptItem ;
            }
        }
        return null ;
    }
 
    private void ensureSorted()
    {
        // Need to record the position as well to 
        if ( sortedRanges == null )
        {
            sortedRanges = new ArrayList<MediaRange>(ranges) ; 
            Collections.sort(sortedRanges, comparator) ;
        }
    }
    /** Find the best thing in offer list with the proposal 
     *  "best" means highest q value, with left most being better for same q.
     * 
     * @param proposalList Client list of possibilities
     * @param offerList    Server list of possibilities
     * @return MediaType
     */
    
    static public MediaType match(AcceptList proposalList, AcceptList offerList)
    {
        MediaRange choice = null ;
        
        for ( MediaRange offer : offerList.entries() )
        {
            MediaRange m = proposalList.match(offer) ;
            if ( m != null )
            {
                if ( choice != null && choice.q >= m.q )
                    continue ; 
                choice = m ;
            }
        }
        if ( choice == null )
            return null ;
        return new MediaType(choice);
    }
    
    public MediaType first()
    {
        ensureSorted() ;
        if ( sortedRanges.isEmpty() )
            return null ;
        return sortedRanges.get(0) ; 
    }
    
    private static List<MediaRange> stringToAcceptList(String s)
    {
        List<MediaRange> ranges = new ArrayList<MediaRange>() ;
        if ( s == null )
            return ranges ;

        String[] x = s.split(",") ;
        for ( int i = 0 ; i < x.length ; i++ )
        {
            if ( x[i].equals(""))
                continue ;
            MediaRange mType = new MediaRange(x[i]) ;
            ranges.add(mType) ;
        }
        return ranges ;
    }
    
    private static class MediaRangeCompare implements Comparator<MediaRange>
    {
        public int compare(MediaRange mType1, MediaRange mType2)
        {
            int r = Double.compare(mType1.q, mType2.q) ;
            
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