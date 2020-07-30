#!/usr/bin/python

import sys
import os
import json
import xml.etree.ElementTree as ET
import re

def codepoint2unicode(codepoint):
  if codepoint < 0xD800 or 0xE000 <= codepoint < 0x10000:
    return unichr(codepoint)
  else:
    cp = codepoint - 0x10000
    return unichr(cp >> 10 | 0xD800) + unichr((cp & 0x3FF) | 0xDC00)

re_text_transform = re.compile(r'^matrix\(([-0-9\. ]+)\)$')

respath = "../app/src/main/res/raw"
stroke_input_dir = "download/kanji"
stroke_output_file = os.path.join(respath, "stroke_data.json")

data = {}
for fn in os.listdir(stroke_input_dir):
  if not fn.endswith('.svg') or len(fn) != 9:
    continue
  fullfn = os.path.join(stroke_input_dir, fn)
  basename = fn[:5]
  codepoint = int(basename, 16)

  tree = ET.parse(fullfn)
  root = tree.getroot()

  print basename
  strokes = []

  i = 1
  for path in root.iter('{http://www.w3.org/2000/svg}path'):
    pathData = path.get('d')
    pathId = path.get('id')
    assert pathId == 'kvg:%s-s%d' % (basename, i), (pathId, basename, i)
    strokes.append(pathData)
    i += 1

  j = 1
  for text in root.iter('{http://www.w3.org/2000/svg}text'):
    transform = text.get('transform')
    m = re_text_transform.match(transform)
    assert m
    label_x, label_y = m.group(1).split()[4:]
    label = text.text
    assert label == str(j)
    strokes[j-1] += 'T%d,%s,%s' % (j, label_x, label_y)
    j += 1

  assert i == j

  data[codepoint2unicode(codepoint)] = strokes

fo = open(stroke_output_file, "wb")
json.dump(data, fo, indent=2, sort_keys=True)
fo.close()
