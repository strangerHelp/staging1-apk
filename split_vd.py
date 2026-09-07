import urllib.request

url = "https://raw.githubusercontent.com/google/material-design-icons/master/src/maps/location_on/materialicons/24px.svg"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    svg = response.read().decode('utf-8')
    print(svg)
