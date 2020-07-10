#!/usr/bin/python

import sys
import os
import json
import BeautifulSoup

respath = "../app/src/main/res/raw"
kanji_jlpt_output_file = os.path.join(respath, "kanji_jlpt_levels.json")
vocab_jlpt_output_file = os.path.join(respath, "vocab_jlpt_levels.json")

def soup_text(o):
  if o is None: return None
  if isinstance(o, BeautifulSoup.Tag):
    return u''.join(map(soup_text, o.contents))
  if isinstance(o, BeautifulSoup.ProcessingInstruction): return u''
  if isinstance(o, BeautifulSoup.Comment): return u''
  if isinstance(o, BeautifulSoup.Declaration): return u''
  return o.string

data = {}

for level in (1,2,3,4,5):
  soup = BeautifulSoup.BeautifulSoup(open(os.path.join("download", "jlpt_n%d_kanji.html" % level), "rb").read())
  table = soup('th', text='Onyomi')[0].parent.parent.parent
  for row in table('tr')[1:]:
    row = row.findAll('td')
    if not row: continue
    assert len(row) >= 1
    characters = soup_text(row[0]).strip()
    if characters:
      data[characters] = level

fo = open(kanji_jlpt_output_file, "wb")
json.dump(data, fo, indent=2, sort_keys=True)
fo.close()

data = {}

for level in (1,2,3,4,5):
  soup = BeautifulSoup.BeautifulSoup(open(os.path.join("download", "jlpt_n%d_vocab.html" % level), "rb").read())
  table = soup('th', text='Hiragana')[0].parent.parent.parent
  for row in table('tr')[1:]:
    row = row.findAll('td')
    if not row: continue
    assert len(row) >= 1
    characters = soup_text(row[0]).strip()
    if characters:
      data[characters] = level

fo = open(vocab_jlpt_output_file, "wb")
json.dump(data, fo, indent=2, sort_keys=True)
fo.close()
