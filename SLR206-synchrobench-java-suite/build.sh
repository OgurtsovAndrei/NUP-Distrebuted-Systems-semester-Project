echo "Build..."

javac -d ./java/bin \
    ./java/src/contention/abstractions/*.java \
    ./java/src/my/list/*.java

echo "Built Done!"
