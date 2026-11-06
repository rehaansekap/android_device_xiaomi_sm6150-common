#!/vendor/bin/sh
#
# Copyright (C) 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
# Author: rehanseekap
#

zram_mb=$(getprop persist.sys.zram_size)

# If property is empty or not set (-1), keep system default from fstab.zram (55%)
if [ -z "$zram_mb" ] || [ "$zram_mb" = "-1" ]; then
    exit 0
fi

# Locate binaries
SWAPOFF=/vendor/bin/swapoff
[ ! -x "$SWAPOFF" ] && SWAPOFF=/system/bin/swapoff
[ ! -x "$SWAPOFF" ] && SWAPOFF=swapoff

MKSWAP=/vendor/bin/mkswap
[ ! -x "$MKSWAP" ] && MKSWAP=/system/bin/mkswap
[ ! -x "$MKSWAP" ] && MKSWAP=mkswap

SWAPON=/vendor/bin/swapon
[ ! -x "$SWAPON" ] && SWAPON=/system/bin/swapon
[ ! -x "$SWAPON" ] && SWAPON=swapon

# If 0, disable zram swap completely
if [ "$zram_mb" -eq 0 ]; then
    $SWAPOFF /dev/block/zram0 2>/dev/null
    sleep 1
    echo 1 > /sys/block/zram0/reset 2>/dev/null
    exit 0
fi

# Custom size in MB -> convert to bytes
req_bytes=$((zram_mb * 1024 * 1024))
cur_bytes=$(cat /sys/block/zram0/disksize 2>/dev/null)

if [ "$cur_bytes" = "$req_bytes" ]; then
    exit 0
fi

$SWAPOFF /dev/block/zram0 2>/dev/null
sleep 1
echo 1 > /sys/block/zram0/reset 2>/dev/null
echo zstd > /sys/block/zram0/comp_algorithm 2>/dev/null
echo 1 > /sys/block/zram0/use_dedup 2>/dev/null
echo $req_bytes > /sys/block/zram0/disksize 2>/dev/null
$MKSWAP /dev/block/zram0 2>/dev/null
$SWAPON /dev/block/zram0 2>/dev/null
