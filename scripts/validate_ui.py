#!/usr/bin/env python3
"""Lightweight XML/reference validation before Gradle Android resource processing."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parent.parent
res = root / 'app/src/main/res'
xml_files = list(res.rglob('*.xml'))
try:
    for p in xml_files:
        ET.parse(p)
except ET.ParseError as exc:
    print(f'ERROR: XML inválido: {p}: {exc}', file=sys.stderr)
    sys.exit(1)

all_strings = {e.get('name') for e in ET.parse(res/'values/strings.xml').getroot() if e.get('name')}
all_res_strings = '\n'.join(p.read_text() for p in xml_files)
string_refs = set(re.findall(r'@string/([A-Za-z_0-9]+)', all_res_strings))
main_java = '\n'.join(p.read_text() for p in (root/'app/src/main/java').rglob('*.java'))
string_refs |= set(re.findall(r'R\.string\.([A-Za-z_0-9]+)', main_java))
missing_strings = string_refs - all_strings

id_defs = set(re.findall(r'@\+id/([A-Za-z_0-9]+)', all_res_strings))
id_refs = set(re.findall(r'(?<!android\.)R\.id\.([A-Za-z_0-9]+)', main_java))
missing_ids = id_refs - id_defs

color_names = set()
for p in [res/'values/colors.xml', res/'values-night/colors.xml']:
    color_names.update(e.get('name') for e in ET.parse(p).getroot() if e.tag == 'color')
colors_used = set(re.findall(r'@color/(df_[A-Za-z_0-9]+)', all_res_strings))
missing_colors = colors_used - color_names

if missing_strings or missing_ids or missing_colors:
    print('ERROR: recursos sin definir', { 'strings':sorted(missing_strings),
        'ids':sorted(missing_ids),'colors':sorted(missing_colors) }, file=sys.stderr)
    sys.exit(1)
print(f'OK: {len(xml_files)} XML; {len(string_refs)} strings; {len(id_refs)} IDs; '
      f'{len(colors_used)} colores DFRoot')

# v4.0 hardcodes this package in its unchanged native bootstrap and kernel module.
build = (root / 'app/build.gradle.kts').read_text()
if not re.search(r'applicationId\s*=\s*"df\.root"', build) or re.search(r'^\s*applicationIdSuffix', build, re.M):
    sys.exit('ERROR: el motor v4.0 requiere applicationId df.root sin sufijo')
print('OK: applicationId compatible con las rutas nativas de v4.0')
