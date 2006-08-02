#!/bin/env ruby

require 'format'

columns=nil
data=[]

#ARGF.each do |line|
#  line.chomp!
#  v = line.split("\t")
#  if !columns
#    columns = v
#  else
#    data << v 
#  end
#end

# Ruby has CSV 
require 'csv.rb'
CSV::Reader.parse(ARGF) do |row|
  if !columns
    columns = row
  else
    data << row
  end
end

Fmt::table(columns, data)
