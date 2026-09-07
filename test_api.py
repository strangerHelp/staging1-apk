import urllib.request
import urllib.parse
import json
import http.cookiejar

cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
BASE_URL = "https://strangerhelp.com"
# Add user agent
opener.addheaders = [('User-agent', 'Mozilla/5.0'), ('Origin', 'https://strangerhelp.com'), ('Content-Type', 'application/json')]

def make_req(method, path, data=None):
    url = BASE_URL + path
    req_data = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(url, data=req_data, method=method)
    try:
        resp = opener.open(req)
        return resp.getcode(), resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

code, text = make_req("POST", "/api/auth/register", {
    "name": "Test Editor",
    "email": "testeditor12345@strangerhelp.com",
    "password": "password123",
    "role": "poster",
    "phone": "1234567890"
})
print("Register:", code, text)

if code == 400 and "already" in text.lower():
    code, text = make_req("POST", "/api/auth/login", {"email": "testeditor12345@strangerhelp.com", "password": "password123"})
    print("Login:", code, text)

code, text = make_req("POST", "/api/tasks", {
    "title": "Old Title",
    "description": "Old Description",
    "budget": 100,
    "location": {"type": "Point", "coordinates": [77.209, 28.613]},
    "address": "New Delhi"
})
print("Create Task:", code, text)
task_id = json.loads(text).get("_id") or json.loads(text).get("id")

if not task_id:
    print("Failed to get task_id")
    exit(1)

code, text = make_req("PATCH", f"/api/tasks/{task_id}", {"title": "New Title"})
print("PATCH without action:", code, text)

code, text = make_req("PUT", f"/api/tasks/{task_id}", {"title": "New Title PUT"})
print("PUT without action:", code, text)

code, text = make_req("PATCH", f"/api/tasks/{task_id}", {"action": "edit", "title": "New Title With Action"})
print("PATCH with action=edit:", code, text)

