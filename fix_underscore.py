import os

def process_file(file_path):
    with open(file_path, "r") as f:
        content = f.read()

    if "catch (_: kotlinx.coroutines.CancellationException)" in content:
        # replace it with catch (e: kotlinx.coroutines.CancellationException) { throw e }
        new_content = content.replace("catch (_: kotlinx.coroutines.CancellationException) {", "catch (ignoredCancellationException: kotlinx.coroutines.CancellationException) {")
        new_content = new_content.replace("throw _", "throw ignoredCancellationException")
        with open(file_path, "w") as f:
            f.write(new_content)
        print(f"Fixed underscore in {file_path}")

for root, dirs, files in os.walk("app/src/main/java"):
    for file in files:
        if file.endswith(".kt"):
            process_file(os.path.join(root, file))
