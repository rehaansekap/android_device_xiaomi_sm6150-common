#!/vendor/bin/sh
#
# Copyright (C) 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
# Author: rehanseekap
#

zram_mb=$(getprop persist.sys.zram_size)

# If property is not set, keep system default from fstab.zram (55%)
if [ -z "$zram_mb" ]; then
    exit 0
fi

# If 0, disable zram swap completely
if [ "$zram_mb" -eq 0 ]; then
    swapoff /dev/block/zram0 2>/dev/null
    echo 1 > /sys/block/zram0/reset 2>/dev/null
    exit 0
fi

# Custom size in MB -> convert to bytes
req_bytes=$((zram_mb * 1024 * 1024))
cur_bytes=$(cat /sys/block/zram0/disksize 2>/dev/null)

if [ "$cur_bytes" = "$req_bytes" ]; then
    exit 0
fi

swapoff /dev/block/zram0 2>/dev/null
echo 1 > /sys/block/zram0/reset 2>/dev/null
echo zstd > /sys/block/zram0/comp_algorithm 2>/dev/null
echo 1 > /sys/block/zram0/use_dedup 2>/dev/null
echo $req_bytes > /sys/block/zram0/disksize 2>/dev/null
mkswap /dev/block/zram0 2>/dev/null
swapon /dev/block/zram0 2>/dev/null
