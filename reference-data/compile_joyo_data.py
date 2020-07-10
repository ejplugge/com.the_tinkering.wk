#!/usr/bin/python

import sys
import os
import json
import BeautifulSoup

respath = "../app/src/main/res/raw"
joyo_input_file = os.path.join("download", "joyo-grades.html")
joyo_output_file = os.path.join(respath, "wikipedia_joyo_grades.json")

def soup_text(o):
  if o is None: return None
  if isinstance(o, BeautifulSoup.Tag):
    return u''.join(map(soup_text, o.contents))
  if isinstance(o, BeautifulSoup.ProcessingInstruction): return u''
  if isinstance(o, BeautifulSoup.Comment): return u''
  if isinstance(o, BeautifulSoup.Declaration): return u''
  return o.string

soup = BeautifulSoup.BeautifulSoup(open(joyo_input_file, "rb").read())
tables = soup('table', 'sortable')
assert len(tables) == 1
table = tables[0]

data = {}
for row in table('tr')[1:]:
  row = row.findAll('td')
  if not row: continue
  assert len(row) >= 6
  id = int(soup_text(row[0]).strip())
  new_kanji = soup_text(row[1]).strip()
  old_kanji = soup_text(row[2]).strip()
  grade = soup_text(row[5]).strip()

  if '&#160;' in new_kanji:
    p = new_kanji.find('&#160;')
    new_kanji = new_kanji[:p]

  if '&#160;' in old_kanji:
    p = old_kanji.find('&#160;')
    old_kanji = old_kanji[:p]

  if '&#x' in new_kanji:
    p1 = new_kanji.find('&#x')
    p2 = new_kanji.find(';', p1)
    assert p1 >= 0
    assert p2 >= 0
    code = int(new_kanji[p1+3:p2], 16)
    new_kanji = new_kanji[:p1] + unichr(code) + new_kanji[p2+1:]

  if '&#x' in old_kanji:
    p1 = old_kanji.find('&#x')
    p2 = old_kanji.find(';', p1)
    assert p1 >= 0
    assert p2 >= 0
    code = int(old_kanji[p1+3:p2], 16)
    old_kanji = old_kanji[:p1] + unichr(code) + old_kanji[p2+1:]

  if '&#' in new_kanji:
    p1 = new_kanji.find('&#')
    p2 = new_kanji.find(';', p1)
    assert p1 >= 0
    assert p2 >= 0
    code = int(new_kanji[p1+2:p2], 10)
    new_kanji = new_kanji[:p1] + unichr(code) + new_kanji[p2+1:]

  if '&#' in old_kanji:
    p1 = old_kanji.find('&#')
    p2 = old_kanji.find(';', p1)
    assert p1 >= 0
    assert p2 >= 0
    code = int(old_kanji[p1+2:p2], 10)
    old_kanji = old_kanji[:p1] + unichr(code) + old_kanji[p2+1:]

  assert '&' not in new_kanji, `new_kanji`
  assert '&' not in old_kanji, `old_kanji`

  assert grade in ('1', '2', '3', '4', '5', '6', 'S'), `grade`
  grade = 7 if grade == 'S' else int(grade)

  if new_kanji:
    data[new_kanji] = grade
  if id == 1814:
    for c in old_kanji:
      if c and c not in data:
        data[c] = grade
  else:
    if old_kanji and old_kanji not in data:
      data[old_kanji] = grade

fo = open(joyo_output_file, "wb")
json.dump(data, fo, indent=2, sort_keys=True)
fo.close()
