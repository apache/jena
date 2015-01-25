#!/usr/bin/ruby
## Licensed to the Apache Software Foundation (ASF) under one
## or more contributor license agreements.  See the NOTICE file
## distributed with this work for additional information
## regarding copyright ownership.  The ASF licenses this file
## to you under the Apache License, Version 2.0 (the
## "License"); you may not use this file except in compliance
## with the License.  You may obtain a copy of the License at
## 
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License

pattern=''
count=0
output=false
file=nil
files=[]

ARGF.each_with_index do |line, idx|
    if ! file.nil?
        file.puts line
        if line.start_with?('}')
            count = count+1
            file.close
            file=nil
        end
        next
    end
    line = line.strip
    
    next if line == ''
    next if line.start_with?("#")
    if line =~ /^pattern=(.*)/
        pattern=$1
        count = 0
        next
    end
    if line.start_with?("{")
        fn = sprintf("%s%02d.rq", pattern, count)
        files << fn
        file = File.open(fn, "w+")
        file.puts "PREFIX : <http://example/>"
        file.puts ""
        file.puts "SELECT *"
        file.puts line
    end
end

## puts "{"
## sep=" "
## files.each do | fn |
##     print "    ",sep
##     print ' "',fn,'"'
##     print "\n"
##     sep=","
## end
## puts "}"

files.each do | fn |
    tn = fn.sub(/\.rq$/,'')
    tn = tn.gsub("-","_")
    printf "    @Test public void %-20s { test(\"%s\") ; }",tn+"()",fn
    print "\n"
end
