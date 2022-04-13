#!/usr/bin/python

import sys
import os
import json

respath = "../app/src/main/res/raw"
userscript_input_file = os.path.join("download", "pitch_info_userscript.js")
weblio_input_file = "weblio_pitch_info.json"
pitch_info_output_file = os.path.join(respath, "pitch_info.json")

table = open(userscript_input_file, "rb").read()
p1 = table.find("var vocabTable = {")
assert p1 > 0
p2 = table.find("}", p1)
assert p2 > p1
table = table[p1+18:p2].decode('utf-8')

userscript_data = {}
offset = 0
while offset < len(table):
  p1 = table.find(":", offset)
  if p1 < 0: break
  characters = table[offset:p1].strip().replace('"', '').replace("'", "")
  assert table[p1+1] == "["
  p2 = table.find("]", p1)
  assert p2 > p1
  assert table[p2+1] == ","
  numbers = map(int, table[p1+2:p2].split(","))
  offset = p2 + 2

  l = []
  for n in numbers:
    if n >= 0:
      l.append((None, None, n))
  userscript_data[characters] = l

weblio_data = json.load(open(weblio_input_file, "rb"))

data = {}
for characters, l in weblio_data.items():
  data[characters] = l + userscript_data.get(characters, [])

fo = open(pitch_info_output_file, "wb")
json.dump(data, fo, indent=2, sort_keys=True)
fo.close()
