import urllib.request

url = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/location_on/default/24px.xml"
try:
    with urllib.request.urlopen(url) as response:
        print(response.read().decode())
except Exception as e:
    print(e)
