#!/usr/bin/python

import sys
import os
import urllib2

url = "https://en.wikipedia.org/wiki/List_of_j%C5%8Dy%C5%8D_kanji"
joyo_output_file = os.path.join("download", "joyo-grades.html")

if not os.path.exists("download"):
  os.mkdir("download")

print "Fetching:", url
conn = urllib2.urlopen(url)
data = conn.read()

fo = open(joyo_output_file, "wb")
fo.write(data)
fo.close()

