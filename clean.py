with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if line.startswith('    private fun startPolling()'):
        skip = True
        new_lines.append(line)
        new_lines.append('        pollJob?.cancel()\n')
        new_lines.append('        pollJob = viewModelScope.launch {\n')
        new_lines.append('            while (isActive) {\n')
        new_lines.append('                delay(5000)\n')
        new_lines.append('                _task.value?._id?.let { id ->\n')
        new_lines.append('                    loadTask(id)\n')
        new_lines.append('                }\n')
        new_lines.append('            }\n')
        new_lines.append('        }\n')
        new_lines.append('    }\n')
        new_lines.append('\n')
        continue
    
    if skip:
        if line.startswith('fun requestToClaim(') or line.startswith('    fun requestToClaim('):
            skip = False
            new_lines.append(line)
        continue
        
    if not skip:
        new_lines.append(line)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.writelines(new_lines)
