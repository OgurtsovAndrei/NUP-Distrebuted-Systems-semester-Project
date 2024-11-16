classes=(
    "linkedlists.lockbased.CoarseGrainedListBasedSet"
    "linkedlists.lockbased.FineGrainedListBasedSet"
    "skiplists.lockfree.NonBlockingFriendlySkipListMap"
    "my.list.MyLinkedList"
    "my.list.MyListWithMutex"
)

for class in "${classes[@]}"; do
  echo $class
    java -cp ./java/bin \
        contention.benchmark.Test \
        -b "$class" \
        -W 5 \
        -d 3000 \
        -t 20 \
        -i 1024 \
        -r 2048
done
