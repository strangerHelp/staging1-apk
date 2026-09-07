import os
import glob

def process_file(file_path):
    with open(file_path, "r") as f:
        content = f.read()

    target1 = """} catch (e: Exception) {"""
    target2 = """} catch(e: Exception) {"""
    
    # We will do a generic regex replace, but it's safer to just replace catch(e: Exception) with catch(e: kotlinx.coroutines.CancellationException) { throw e } catch(e: Exception)
    import re
    # Match "} catch (e: Exception) {" or similar
    pattern = r"\}\s*catch\s*\(\s*[a-zA-Z0-9_]+\s*:\s*Exception\s*\)\s*\{"
    
    # We don't want to replace if there's already CancellationException right before it.
    
    if re.search(pattern, content):
        # Read lines
        lines = content.split('\n')
        new_lines = []
        for line in lines:
            if re.search(pattern, line) and "CancellationException" not in "\n".join(new_lines[-2:]):
                var_name_match = re.search(r"catch\s*\(\s*([a-zA-Z0-9_]+)\s*:\s*Exception\s*\)", line)
                var_name = var_name_match.group(1) if var_name_match else "e"
                
                # Check indentation
                indent = line[:len(line) - len(line.lstrip())]
                
                new_lines.append(indent + "} catch (" + var_name + ": kotlinx.coroutines.CancellationException) {")
                new_lines.append(indent + "    throw " + var_name)
                new_lines.append(line)
            else:
                new_lines.append(line)
                
        new_content = '\n'.join(new_lines)
        if new_content != content:
            with open(file_path, "w") as f:
                f.write(new_content)
            print(f"Patched {file_path}")

for root, dirs, files in os.walk("app/src/main/java"):
    for file in files:
        if file.endswith(".kt"):
            process_file(os.path.join(root, file))
