/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            28-Nov-2003
 * Filename           $RCSfile: DIGConnectionPool.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-01 22:40:07 $
 *               by   $Author: ian_dickinson $
 *
 * @copyright@
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import java.util.*;



/**
 * <p>
 * Maintains a pool of active DIG connections and whether they are allocated or not.
 * Implements Singleton pattern.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGConnectionPool.java,v 1.1 2003-12-01 22:40:07 ian_dickinson Exp $)
 */
public class DIGConnectionPool {
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /** The singleton instance */
    private static DIGConnectionPool s_instance = new DIGConnectionPool();
    
    
    // Instance variables
    //////////////////////////////////

    /** The adapter pool - unallocated adapters */
    private List m_pool = new ArrayList();
    
    /** The allocated adapter list */
    private List m_allocated = new ArrayList();
    
    
    // Constructors
    //////////////////////////////////

    /** Private constructor to enforce Singleton pattern. */
    private DIGConnectionPool() {}
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the singleton instance of the adapter pool.</p>
     */
    public static DIGConnectionPool getInstance() {
        return s_instance;
    }
    
    
    public DIGConnection allocate() {
        DIGConnection dc = m_pool.isEmpty() ? new DIGConnection() : (DIGConnection) m_pool.remove( 0 );
        m_allocated.add( dc );
            
        return dc;
    }
    
    
    public void release( DIGConnection dc ) {
        m_allocated.remove( dc );
        m_pool.add( dc );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
@footer@
*/
