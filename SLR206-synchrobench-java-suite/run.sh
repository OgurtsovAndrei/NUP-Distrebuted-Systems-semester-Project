classes=(
   "linkedlists.lockbased.CoarseGrainedListBasedSet"
   "linkedlists.lockbased.LockCouplingListIntSet"
#    "linkedlists.lockbased.FineGrainedListBasedSet"
 #   "skiplists.lockfree.NonBlockingFriendlySkipListMap"
    "my.list.MyLinkedList"
    "my.list.MyListWithMutex"
    "my.list.MyListWithHandOverHandLocking"
    "my.list.MyListWithHandOverHandSpinLocking"
    "my.list.MyOptiListWithHandOverHandSpinLocking"
)

for class in "${classes[@]}"; do
  echo $class
    java -cp ./java/bin \
        contention.benchmark.Test \
        -b "$class" \
        -W 5 \
        -d 5000 \
        -t 20 \
        -i 1024 \
        -r 2048
done
