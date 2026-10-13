#!/usr/bin/env python3
"""Catch a seemingly valid APK that is missing the executables invoked by v4.0."""
import sys
from zipfile import ZipFile

with ZipFile(sys.argv[1]) as apk:
    assert apk.testzip() is None, 'Corrupt APK'
    assert 'classes.dex' in apk.namelist(), 'Missing app bytecode'
    for name in ('libdfroot.so', 'libbootstrap.so'):
        path = 'lib/arm64-v8a/' + name
        data = apk.read(path)
        assert data[:5] == b'\x7fELF\x02', f'{path}: not a 64-bit ELF'
        assert int.from_bytes(data[18:20], 'little') == 183, f'{path}: not AArch64'
        print(f'OK: {path} ({len(data)} bytes)')
    print('OK: APK CRC and required native executables')
# libksud.so intentionally belongs to the selected KernelSU manager, not DFRoot.
