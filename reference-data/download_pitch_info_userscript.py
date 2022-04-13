#!/usr/bin/python

import sys
import os
import urllib2

if not os.path.exists("download"):
  os.mkdir("download")

url = "https://raw.githubusercontent.com/Invertex/WaniKani-Pitch-Info/master/WaniKani%20Pitch%20Info.js"

print "Fetching:", url
conn = urllib2.urlopen(url)
data = conn.read()

fo = open(os.path.join("download", "pitch_info_userscript.js"), "wb")
fo.write(data)
fo.close()

