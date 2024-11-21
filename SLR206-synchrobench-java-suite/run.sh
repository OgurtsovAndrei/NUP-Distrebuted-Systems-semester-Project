#!/bin/bash

# Get the current timestamp for unique directory naming
timestamp=$(date +"%Y%m%d_%H%M%S")

# Define the base directory for results
base_results_dir="/tmp/results_$timestamp"
json_file="$base_results_dir/data.json"

# Create the directory
mkdir -p "$base_results_dir"

# Define static parameters
warmup_time=0
duration=2000

# Define variable parameters
thread_counts=(20)
update_ratios=(0 10 100)
list_sizes=(100 1000 10000)  # 100, 1k, 10k

# Define the classes
classes=(
    "linkedlists.lockbased.LockCouplingListIntSet"
    "linkedlists.lockbased.FineGrainedListBasedSet"
    "my.list.MyListWithMutex"
    "my.list.MyListWithHandOverHandLocking"
    "my.list.MyListWithHandOverHandSpinLocking"
    "my.list.MyListWithHandOverHandMyOptimizedVolatileSpinLocking"
)

# Initialize the JSON array
echo "[" > "$json_file"

# Iterate over each combination of parameters
for class in "${classes[@]}"; do
  for threads in "${thread_counts[@]}"; do
    for update_ratio in "${update_ratios[@]}"; do
      for initial_size in "${list_sizes[@]}"; do
        range=$((initial_size * 2))

        echo "Running benchmark for class: $class with threads=$threads, update_ratio=$update_ratio, initial_size=$initial_size"

        # Create a directory for the current combination
        combo_dir="$base_results_dir/$(echo "$class" | tr '.' '_')/threads_${threads}_update_${update_ratio}_size_${initial_size}"
        mkdir -p "$combo_dir"

        # Define the result file path
        result_file="$combo_dir/result.txt"

        # Run the Java command and save results to a file
        java -cp ./java/bin \
            contention.benchmark.Test \
            -b "$class" \
            -W "$warmup_time" \
            -d "$duration" \
            -t "$threads" \
            -i "$initial_size" \
            -r "$range" \
            -u "$update_ratio" \
            > "$result_file"

        # Write parameters and result reference to the JSON file
        echo "  {" >> "$json_file"
        echo "    \"class\": \"$class\"," >> "$json_file"
        echo "    \"warmup_time\": $warmup_time," >> "$json_file"
        echo "    \"duration\": $duration," >> "$json_file"
        echo "    \"threads\": $threads," >> "$json_file"
        echo "    \"initial_size\": $initial_size," >> "$json_file"
        echo "    \"range\": $range," >> "$json_file"
        echo "    \"update_ratio\": $update_ratio," >> "$json_file"
        echo "    \"result_file\": \"$result_file\"" >> "$json_file"
        echo "  }," >> "$json_file"
      done
    done
  done
done

# Remove the trailing comma and close the JSON array
sed -i '$ s/,$//' "$json_file"
echo "]" >> "$json_file"

echo "Results saved to $base_results_dir"
