/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */


package txnlog;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.sparql.core.QuadAction ;

/** Things to do with transaction logging */
class TxnLog {
    public static final byte ADD = 1 ;
    public static final byte NO_ADD = 2 ;
    public static final byte DEL = 2 ;
    public static final byte NO_DEL = 3 ;
    public static final byte END = 99 ;
    
    public static byte quadActionToByte(QuadAction qaction) {
        switch(qaction) {
            case ADD: return ADD ;
            case DELETE : return DEL ; 
            case NO_ADD : return NO_ADD ;
            case NO_DELETE : return NO_DEL ;
            default:
                throw new InternalErrorException("Bad QuadAction") ;
        }
    }

    public static QuadAction byteToQuadAction(byte b) {
        if ( b == ADD ) return QuadAction.ADD ;
        if ( b == DEL ) return QuadAction.DELETE ;
        if ( b == NO_ADD ) return QuadAction.NO_ADD ;
        if ( b == NO_DEL ) return QuadAction.NO_DELETE ;
        throw new InternalErrorException(String.format("Bad byte value for QuadAction: 0x%02X",Byte.toUnsignedInt(b))) ;
    }
    

}

