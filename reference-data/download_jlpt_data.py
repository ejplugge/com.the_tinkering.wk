#!/usr/bin/python

import sys
import os
import urllib2

if not os.path.exists("download"):
  os.mkdir("download")

n5_vocab_url = "http://www.tanos.co.uk/jlpt/jlpt5/vocab/"
n5_kanji_url = "http://www.tanos.co.uk/jlpt/jlpt5/kanji/"
n4_vocab_url = "http://www.tanos.co.uk/jlpt/jlpt4/vocab/"
n4_kanji_url = "http://www.tanos.co.uk/jlpt/jlpt4/kanji/"
n3_vocab_url = "http://www.tanos.co.uk/jlpt/jlpt3/vocab/"
n3_kanji_url = "http://www.tanos.co.uk/jlpt/jlpt3/kanji/"
n2_vocab_url = "http://www.tanos.co.uk/jlpt/jlpt2/vocab/"
n2_kanji_url = "http://www.tanos.co.uk/jlpt/jlpt2/kanji/"
n1_vocab_url = "http://www.tanos.co.uk/jlpt/jlpt1/vocab/"
n1_kanji_url = "http://www.tanos.co.uk/jlpt/jlpt1/kanji/"

for level in (1, 2, 3, 4, 5):
  url = "http://www.tanos.co.uk/jlpt/jlpt%d/kanji/" % level
  print "Fetching:", url
  conn = urllib2.urlopen(url)
  data = conn.read()

  fo = open(os.path.join("download", "jlpt_n%d_kanji.html" % level), "wb")
  fo.write(data)
  fo.close()

for level in (1, 2, 3, 4, 5):
  url = "http://www.tanos.co.uk/jlpt/jlpt%d/vocab/" % level
  print "Fetching:", url
  conn = urllib2.urlopen(url)
  data = conn.read()

  fo = open(os.path.join("download", "jlpt_n%d_vocab.html" % level), "wb")
  fo.write(data)
  fo.close()

