echo ----Product INFOS---- >> $1
getprop ro.product.name >> $1
getprop ro.product.model >> $1
getprop ro.product.device >> $1
getprop ro.product.board >> $1
echo ----Dirs ls---- >> $1
echo ========/======== >> $1
ls -la / >> $1
echo ========/dev======== >> $1
ls -la /dev/ >> $1
echo ========/dev/block======== >> $1
ls -la /dev/block/ >> $1
echo ========/dev/mtd======== >> $1
ls -la /dev/mtd/ >> $1
echo ========/dev/block/platform======== >> $1
ls -la /dev/block/platform/ >> $1
echo ========/dev/block/plarform/*======== >> $1
ls -la /dev/block/platform/*/ >> $1
echo ========/dev/block/plarform/*/*======== >> $1
ls -la /dev/block/platform/*/*/ >> $1
echo ========/proc======== >> $1
ls -la /proc/ >> $1
echo ========/proc/emmc======== >> $1
cat /proc/emmc >> $1