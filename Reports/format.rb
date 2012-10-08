# Formatting utilities
# Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

module Fmt
  def Fmt.table(headings, body)
    widths = calc_widths(headings, body)
    widths = calc_widths(headings, body)
    # Make lines like column names
    lines = []
    headings.each_index { |i| lines<<"-"*(widths[i]) ; }
    lines2 = []
    headings.each_index { |i| lines2<<"="*(widths[i]) ; }
    print_row(lines,   widths, "-", "+", "-", "+")
    print_row(headings, widths, " ", "|", "|", "|")
    print_row(lines2,  widths, "=", "|", "|", "|")
    body.each { |row| print_row(row, widths, " ", "|", "|", "|") }
    print_row(lines, widths, "-", "+", "-", "+")
  end

  private
  def Fmt.calc_widths(columns, data)
    x = []
    columns.each { |c| x << c.length }
    data.each do |row|
      row.each_index { |i|  x[i] = row[i].length if row[i].length > x[i] }
    end
    return x
  end

  def Fmt.print_row(items, widths, sep, left, mid, right)
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
end
