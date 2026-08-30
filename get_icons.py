import urllib.request
import re

url_location = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/location_on/default/48px.svg"
url_handshake = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/handshake/default/48px.svg"

try:
    with urllib.request.urlopen(url_location) as response:
        html = response.read().decode()
        print("Location: ", html)
except Exception as e:
    print(e)
    
try:
    with urllib.request.urlopen(url_handshake) as response:
        html = response.read().decode()
        print("Handshake: ", html)
except Exception as e:
    print(e)
