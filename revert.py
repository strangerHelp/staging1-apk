import os

def process_file(file_path):
    with open(file_path, "r") as f:
        lines = f.readlines()
        
    new_lines = []
    i = 0
    changed = False
    while i < len(lines):
        line = lines[i]
        if "catch (" in line and ": kotlinx.coroutines.CancellationException) {" in line:
            # check if next line is throw
            if i + 1 < len(lines) and "throw " in lines[i+1]:
                # skip these two lines
                i += 2
                changed = True
                continue
        new_lines.append(line)
        i += 1
        
    if changed:
        with open(file_path, "w") as f:
            f.writelines(new_lines)
        print(f"Reverted {file_path}")

for root, dirs, files in os.walk("app/src/main/java"):
    for file in files:
        if file.endswith(".kt"):
            process_file(os.path.join(root, file))
