#require 'java'
#include_class 'java.sql.DriverManager' 

# Ruby has a CSV 
#require 'csv'

def calc_widths(columns, data)
  x = []
  columns.each { |c| x << c.length }
  data.each do |row|
    row.each_index { |i|  x[i] = row[i].length if row[i].length > x[i] }
  end
  return x
end

def print_row(items, widths, sep, left, mid, right)
  print left
  items.each_index do |i|
    print mid if i != 0
    print sep
    printf("%-*s",widths[i],items[i])
    print sep
  end
  print right
  print "\n" 
end

columns=nil
data=[]

ARGF.each do |line|
  line.chomp!
  v = line.split("\t")
  if !columns
    columns = v
  else
    data << v 
  end
end

widths = calc_widths(columns, data)
# Make lines like column names
lines = []
columns.each_index { |i| lines<<"-"*(widths[i]) ; }
lines2 = []
columns.each_index { |i| lines2<<"="*(widths[i]) ; }


print_row(lines,   widths, "-", "+", "-", "+")
print_row(columns, widths, " ", "|", "|", "|")
print_row(lines2,  widths, "=", "|", "|", "|")
data.each { |row| print_row(row, widths, " ", "|", "|", "|") }
print_row(lines, widths, "-", "+", "-", "+")

