#!/vendor/bin/sh
# Flush logcat and dmesg to /cache continuously with unbuffered writes and sync

# Start logcat outputting to stdout redirected to /cache (triggers fflush per line in logcat)
/system/bin/logcat -b all -v threadtime > /cache/boot_logcat.txt &
LOGCAT_PID=$!

# Also capture dmesg
/system/bin/dmesg -w > /cache/boot_dmesg.txt &
DMESG_PID=$!

while kill -0 $LOGCAT_PID 2>/dev/null || kill -0 $DMESG_PID 2>/dev/null; do
    sleep 0.5
    sync
done
