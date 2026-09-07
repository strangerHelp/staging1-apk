import urllib.request
import urllib.parse
import json

opener = urllib.request.build_opener()
opener.addheaders = [('User-agent', 'Mozilla/5.0'), ('Origin', 'https://strangerhelp.com')]
BASE_URL = "https://strangerhelp.com"

def get_code(method, path):
    req = urllib.request.Request(BASE_URL + path, method=method)
    try:
        resp = opener.open(req)
        return resp.getcode(), resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

print("PUT:", get_code("PUT", "/api/tasks/fake_id"))
print("PATCH:", get_code("PATCH", "/api/tasks/fake_id"))
