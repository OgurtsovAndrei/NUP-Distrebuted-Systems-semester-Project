import os
import re
import json
import sys
from collections import defaultdict
import plotly.graph_objects as go

def extract_throughput(result_file):
    """Extract throughput from the benchmark result file."""
    try:
        with open(result_file, 'r') as file:
            log_data = file.read()

        # Regex pattern for Throughput with exact match
        throughput_pattern = r"Throughput \(ops/s\):\s+([\d.]+)"
        match = re.search(throughput_pattern, log_data)

        if match:
            return float(match.group(1))
        else:
            return None
    except FileNotFoundError:
        print(f"Error: Result file {result_file} not found.")
        return None

def extract_data_from_json(json_file):
    """Extract data from a single JSON file and associated result files."""
    try:
        with open(json_file, 'r') as file:
            data = json.load(file)

        results = []
        for entry in data:
            class_name = entry.get("class", "Unknown")
            result_file = entry.get("result_file", "N/A")
            params = {
                "warmup_time": entry.get("warmup_time", "N/A"),
                "duration": entry.get("duration", "N/A"),
                "threads": entry.get("threads", "N/A"),
                "initial_size": entry.get("initial_size", "N/A"),
                "range": entry.get("range", "N/A"),
                "update_ratio": entry.get("update_ratio", "N/A")
            }

            throughput = extract_throughput(result_file)
            results.append((class_name, params, throughput))

        return results
    except FileNotFoundError:
        print(f"Error: JSON file {json_file} not found.")
        return []
    except json.JSONDecodeError:
        print(f"Error: Failed to decode JSON file {json_file}.")
        return []

def find_results_directories(base_directory):
    """Find all directories matching the pattern 'results_<timestamp>'."""
    matching_dirs = []
    pattern = re.compile(r"results_\d{8}_\d{6}")
    for entry in os.listdir(base_directory):
        entry_path = os.path.join(base_directory, entry)
        if os.path.isdir(entry_path) and pattern.match(entry):
            matching_dirs.append(entry_path)
    return matching_dirs

def extract_all_results(base_directory):
    """Find all JSON files in matching directories and extract their results."""
    all_results = []
    result_dirs = find_results_directories(base_directory)
    for result_dir in result_dirs:
        json_file = os.path.join(result_dir, "data.json")
        if os.path.exists(json_file):
            print(f"Processing {json_file}...")
            results = extract_data_from_json(json_file)
            all_results.extend(results)
    return all_results

def group_results_by_params(results):
    """Group throughput values by class and parameters."""
    grouped = defaultdict(list)
    for class_name, params, throughput in results:
        if throughput is not None:
            key = (class_name, params["threads"], params["initial_size"], params["update_ratio"])
            grouped[key].append(throughput)
    return grouped

def plot_graphs(grouped_results):
    """Plot graphs with logarithmic axes."""
    # Threads graph for list_sizes=1000, update_ratios=10 and 100
    list_size = 1000
    update_ratios = [10, 100]
    for ratio in update_ratios:
        threads = sorted({key[1] for key in grouped_results if key[2] == list_size and key[3] == ratio})
        data = []
        for class_name in set(key[0] for key in grouped_results):
            throughputs = [
                sum(grouped_results[(class_name, thread, list_size, ratio)]) / len(grouped_results[(class_name, thread, list_size, ratio)])
                for thread in threads if (class_name, thread, list_size, ratio) in grouped_results
            ]
            data.append(go.Scatter(x=threads, y=throughputs, mode='lines+markers', name=class_name))
        layout = go.Layout(
            title=f"Throughput vs Threads (List Size=1000, Update Ratio={ratio})",
            xaxis=dict(title="Threads", type="log"),
            yaxis=dict(title="Throughput (ops/s)", type="log"),
        )
        fig = go.Figure(data=data, layout=layout)
        fig.show()

    # Update ratios for threads=4 and size=1000
    size = 1000
    update_ratios = sorted({key[3] for key in grouped_results if key[1] == 4 and key[2] == size})
    data = []
    for class_name in set(key[0] for key in grouped_results):
        throughputs = [
            sum(grouped_results[(class_name, 4, size, ratio)]) / len(grouped_results[(class_name, 4, size, ratio)])
            for ratio in update_ratios if (class_name, 4, size, ratio) in grouped_results
        ]
        data.append(go.Scatter(x=update_ratios, y=throughputs, mode='lines+markers', name=class_name))
    layout = go.Layout(
        title=f"Throughput vs Update Ratios (Threads=4, List Size={size})",
        xaxis=dict(title="Update Ratios (%)", type="log"),
        yaxis=dict(title="Throughput (ops/s)", type="log"),
    )
    fig = go.Figure(data=data, layout=layout)
    fig.show()

    # List sizes with threads=4, update_ratios=10 and 100
    update_ratios = [10, 100]
    for ratio in update_ratios:
        sizes = sorted({key[2] for key in grouped_results if key[1] == 4 and key[3] == ratio})
        data = []
        for class_name in set(key[0] for key in grouped_results):
            throughputs = [
                sum(grouped_results[(class_name, 4, size, ratio)]) / len(grouped_results[(class_name, 4, size, ratio)])
                for size in sizes if (class_name, 4, size, ratio) in grouped_results
            ]
            data.append(go.Scatter(x=sizes, y=throughputs, mode='lines+markers', name=class_name))
        layout = go.Layout(
            title=f"Throughput vs List Sizes (Threads=4, Update Ratio={ratio})",
            xaxis=dict(title="List Sizes", type="log"),
            yaxis=dict(title="Throughput (ops/s)", type="log"),
        )
        fig = go.Figure(data=data, layout=layout)
        fig.show()

def main():
    # Check if CLI argument is provided
    if len(sys.argv) > 1:
        base_directory = sys.argv[1]
    else:
        # Default to '/tmp'
        base_directory = '/tmp'

    # Extract all results
    all_results = extract_all_results(base_directory)

    # Group results by class and parameters
    grouped_results = group_results_by_params(all_results)

    # Print results and plot graphs
    if grouped_results:
        print("Extracted Results:")
        print(f"{'Class Name':<60} {'Threads':<8} {'List Size':<10} {'Update Ratio':<15} {'Throughputs':<40}")
        print("-" * 130)
        for (class_name, threads, initial_size, update_ratio), throughputs in grouped_results.items():
            throughputs_str = ", ".join(f"{t:.6f}" for t in throughputs)
            print(f"{class_name:<60} {threads:<8} {initial_size:<10} {update_ratio:<15} {throughputs_str:<40}")
        plot_graphs(grouped_results)
    else:
        print("No results found in the provided data.")

if __name__ == "__main__":
    main()
