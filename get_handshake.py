import urllib.request
import re

url = "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/web/handshake/materialsymbolsoutlined/handshake_48px.svg"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    svg = response.read().decode('utf-8')
    print(svg)
