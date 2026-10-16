#!/bin/sh
# Read-only kernel compatibility report for Android TV / RTD6748 / 5.4.284.
# Run on the ADB host, not inside an "adb shell".
# Does not run su, insmod, exploit code, or write to the television.
set -eu

if ! command -v adb >/dev/null 2>&1; then
    echo "ERROR: install Android Platform Tools (adb) on the host." >&2
    exit 1
fi

if [ "$#" -gt 1 ]; then
    echo "Usage: sh scripts/tv_rtd6748_probe.sh [adb-device-serial]" >&2
    exit 2
fi

adb_cmd() {
    if [ "$#" -ge 1 ] && [ "${DEVICE_SERIAL:-}" != "" ]; then
        adb -s "$DEVICE_SERIAL" "$@"
    else
        adb "$@"
    fi
}

DEVICE_SERIAL="${1:-}"
echo "===== DFRoot Modern: TV kernel compatibility (READ ONLY) ====="
echo "ADB state:"
adb_cmd get-state
echo

echo "===== Device properties ====="
for name in \
    ro.product.manufacturer \
    ro.product.model \
    ro.build.version.release \
    ro.build.version.sdk \
    ro.board.platform \
    ro.hardware \
    ro.product.cpu.abilist \
    ro.product.cpu.abilist32 \
    ro.product.cpu.abilist64 \
    ro.zygote \
    ro.boot.verifiedbootstate \
    ro.boot.vbmeta.device_state \
    ro.boot.flash.locked \
    ro.build.version.security_patch; do
    value="$(adb_cmd shell getprop "$name" | tr -d '\r')"
    printf '%s = %s\n' "$name" "$value"
done

echo
echo "===== Kernel ====="
adb_cmd shell uname -a
adb_cmd shell cat /proc/version
echo
echo "===== CPU detail (non-identifying) ====="
adb_cmd shell 'grep -Ei "^(processor|model name|Hardware|Features|CPU architecture)" /proc/cpuinfo | head -n 24' || true

echo
echo "===== Live kernel configuration (when accessible) ====="
adb_cmd shell '
    if [ -r /proc/config.gz ]; then
        if command -v zcat >/dev/null 2>&1; then
            zcat /proc/config.gz 2>/dev/null |
                grep -E "^(CONFIG_(ARM64|ARM|ARCH_RTK6748|MODULES|MODVERSIONS|MODULE_UNLOAD|IKCONFIG|IKCONFIG_PROC|IKHEADERS|KALLSYMS|KALLSYMS_ALL|LOCALVERSION|PREEMPT|SECURITY_SELINUX|INET_ESP|XFRM_USER)=|# CONFIG_(ARM64|MODULES|MODVERSIONS|IKCONFIG) is not set)" ||
                echo "Kernel config readable but requested flags absent"
        else
            echo "zcat not available inside this TV"
        fi
    else
        echo "/proc/config.gz unavailable or permission denied"
    fi
' || true

echo
echo "===== Existing modules, kernel headers and symbols (metadata only) ====="
adb_cmd shell 'ls -ld /proc/config.gz /sys/kernel/kheaders.tar.xz /proc/kallsyms /proc/modules /vendor/lib/modules /vendor/lib64/modules /lib/modules 2>/dev/null || true'
adb_cmd shell 'head -n 8 /proc/modules 2>/dev/null || true'
echo
echo "===== End. Do not install unverified kernel modules ====="
