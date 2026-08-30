import urllib.request
url = "https://raw.githubusercontent.com/google/material-design-icons/master/src/social/handshake/materialicons/24px.svg"
try:
    response = urllib.request.urlopen(url)
    data = response.read().decode('utf-8')
    print(data)
except Exception as e:
    print(e)
