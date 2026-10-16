#!/usr/bin/env python3
"""Confirm ARMv7 ELF32 and that no ARM64 native libraries are packaged."""
import sys
from zipfile import ZipFile
with ZipFile(sys.argv[1]) as apk:
    assert apk.testzip() is None, "Corrupt APK"
    names = apk.namelist()
    assert "classes.dex" in names
    native = [n for n in names if n.startswith("lib/") and n.endswith(".so")]
    assert native == ["lib/armeabi-v7a/libdfroot_archprobe.so"], native
    elf = apk.read(native[0])
    assert elf[:4] == b"\x7fELF", "ELF magic missing"
    assert elf[4] == 1, "Expected ELFCLASS32"
    assert int.from_bytes(elf[18:20], "little") == 40, "Expected EM_ARM"
    print("PASS: ARMv7 ELF32 library only, no ARM64 native binaries")
