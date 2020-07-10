#!/usr/bin/python

import sys
import os
import json

respath = "../app/src/main/res/raw"
freq_input_file = "kanjidic-freq.txt"
freq_output_file = os.path.join(respath, "kanjidic_freq.json")

data = {}
for line in open(freq_input_file, "rb"):
  line = [s for s in line.decode('utf-8').strip().split() if s]
  if not line: continue
  assert len(line) == 2
  characters = line[0]
  freq = int(line[1])
  data[characters] = freq

fo = open(freq_output_file, "wb")
json.dump(data, fo, indent=2, sort_keys=True)
fo.close()
