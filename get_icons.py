import urllib.request
import re

urls = {
    "handshake_filled": "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/web/handshake/materialsymbolsrounded/handshake_48px_fill.svg",
    "location_on": "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/web/location_on/materialsymbolsrounded/location_on_48px_fill.svg"
}

for name, url in urls.items():
    req = urllib.request.Request(url)
    try:
        with urllib.request.urlopen(req) as response:
            svg = response.read().decode('utf-8')
            print(f"--- {name} ---")
            print(svg)
    except Exception as e:
        print(f"Failed to fetch {name}: {e}")
