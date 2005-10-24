package com.hp.hpl.jena.mem.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.mem.MatchOrBind;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.util.iterator.*;

public class TestTripleBunch extends GraphTestBase
    {
    protected static final Triple tripleSPO = triple( "s P o" );
    protected static final Triple tripleXQY = triple( "x Q y" );

    public class HashedTripleBunch extends TripleBunch
        {
        protected Triple [] contents = new Triple[3];
        protected int size = 0;
        
        protected final Triple REMOVED = triple( "-s- --P-- -o-" );
        
        public boolean contains( Triple t )
            { return findSlot( t ) < 0; }
            
        protected int findSlot( Triple t )
            {
            int index = t.hashCode() % contents.length;
            while (true)
                {
                if (t.equals( contents[index] )) return ~index;
                if (contents[index] == null) return index;
                index += 1;
                }
            }         
        
        protected int findSlotBySameValueAs( Triple t )
            {
            int index = t.hashCode() % contents.length;
            while (true)
                {
                if (contents[index] == null) return index;
                if (t.matches( contents[index] )) return ~index;
                index += 1;
                }
            }


        public boolean containsBySameValueAs( Triple t )
            {
            return findSlotBySameValueAs( t ) < 0;
            }

        public int size()
            {
            return size;
            }

        public void add( Triple t )
            {
            assertFalse( "precondition", contains( t ) );
            int where = findSlot( t );
            assertFalse( where < 0 );
            contents[where] = t;
            size += 1;
            }

        public void remove( Triple t )
            {
            assertTrue( "precondition", contains( t ) );
            int where = findSlot( t );
            assertTrue( where < 0 );
            contents[~where] = REMOVED;
            size -= 1;
            }

        public ExtendedIterator iterator()
            {
            return new NiceIterator()
                {
                int index = 0;
                public boolean hasNext()
                    {
                    while (index < contents.length && (contents[index] == null || contents[index] == REMOVED))
                        index += 1;
                    return index < contents.length;
                    }
                
                public Object next()
                    {
                    assertTrue( hasNext() );
                    Object answer = contents[index];
                    index += 1;
                    return answer;
                    }
                
                public void remove()
                    {
                    
                    }
                };
            }

        public void app( Domain d, StageElement next, MatchOrBind s )
            {
            }
        }
    
    public TestTripleBunch(String name)
        { super( name ); }

    private TripleBunch getBunch()
        {
        return new HashedTripleBunch();
        }
    
    public void testEmptyBunch()
        {
        TripleBunch b = getBunch();
        assertEquals( 0, b.size() );
        assertFalse( b.contains( tripleSPO ) );
        assertFalse( b.contains( tripleXQY ) );
        assertFalse( b.iterator().hasNext() );
        }

    public void testAddElement()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        assertEquals( 1, b.size() );
        assertTrue( b.contains( tripleSPO ) );
        assertEquals( listOf( tripleSPO ), iteratorToList( b.iterator() ) );
        }

    public void testAddElements()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        b.add( tripleXQY );
        assertEquals( 2, b.size() );
        assertTrue( b.contains( tripleSPO ) );
        assertTrue( b.contains( tripleXQY ) );
        assertEquals( setOf( tripleSPO, tripleXQY ), iteratorToSet( b.iterator() ) );
        }
    
    public void testRemoveOnlyElement()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        b.remove( tripleSPO );
        assertEquals( 0, b.size() );
        assertFalse( b.contains( tripleSPO ) );
        assertFalse( b.iterator().hasNext() );
        }
    
    public void testRemoveFirstOfTwo()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        b.add( tripleXQY );
        b.remove( tripleSPO );
        assertEquals( 1, b.size() );
        assertFalse( b.contains( tripleSPO ) );
        assertTrue( b.contains( tripleXQY ) );
        assertEquals( listOf( tripleXQY ), iteratorToList( b.iterator() ) );
        }

    protected List listOf( Triple x )
        {
        List result = new ArrayList();
        result.add( x );
        return result;
        }
    
    protected Set setOf( Triple x, Triple y )
        {
        Set result = setOf( x );
        result.add( y );
        return result;
        }
    
    protected Set setOf( Triple x )
        {
        Set result = new HashSet();
        result.add( x );
        return result;
        }
    

    }
