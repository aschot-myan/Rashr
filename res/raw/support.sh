echo ----Dirs ls----
echo ========/========
ls -la / >> $1
echo ========/dev========
ls -la /dev/
echo ========/dev/block========
ls -la /dev/block/
echo ========/dev/mtd========
ls -la /dev/mtd/
echo ========/dev/block/platform========
ls -la /dev/block/platform/
echo ========/dev/block/plarform/*========
ls -la /dev/block/platform/*/
echo ========/dev/block/plarform/*/*========
ls -la /dev/block/platform/*/*/
echo ========/proc========
ls -la /proc/
echo ========/proc/emmc========
cat /proc/emmc