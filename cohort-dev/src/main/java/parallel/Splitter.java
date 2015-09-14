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
 
package parallel;

import java.util.ArrayList ;
import java.util.List ;
import java.util.function.Consumer ;

class Splitter<X> {
    
    private Consumer<List<X>> processor;
    private List<X> batch = null ;
    private int N; 

    public Splitter(Consumer<List<X>> processor, int N) {
        this.processor = processor ;
        this.N = N ;
    }
    
    public void start() {}
    
    public void item(X item) {
        if ( batch == null )
            batch = new ArrayList<>() ;
        batch.add(item) ;
        if ( batch.size() == N ) {
            process() ;
        }
    }
    
    private void process() {
        processor.accept(batch);
        batch.clear() ;
    }

    public void finish() {
        if ( batch != null )
            process() ;
    }
}
