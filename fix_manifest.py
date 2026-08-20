import xml.etree.ElementTree as ET

path = 'app/src/main/AndroidManifest.xml'
with open(path, 'r') as f:
    content = f.read()

# I will just use simple string replacement to remove the exact duplicates since it's easier and avoids XML namespace issues
lines = content.split('\n')
new_lines = []
seen_permissions = set()
seen_services = set()

for line in lines:
    if '<uses-permission' in line:
        perm = line.strip()
        if perm in seen_permissions:
            continue
        seen_permissions.add(perm)
    elif '<service' in line and 'TrackingService' in line:
        if 'TrackingService' in seen_services:
            # skip until </service>
            pass
        seen_services.add('TrackingService')
    
    # manual cleanup is safer via sed or python script that rewrites exactly. Let's just rewrite the whole file cleanly.
