import xml.etree.ElementTree as ET

tree = ET.parse('app/src/main/AndroidManifest.xml')
root = tree.getroot()
application = root.find('application')

# check if activity exists
exists = False
for activity in application.findall('activity'):
    if activity.get('{http://schemas.android.com/apk/res/android}name') == '.ui.screens.auth.OAuthWebViewActivity':
        exists = True

if not exists:
    new_activity = ET.Element('activity')
    new_activity.set('{http://schemas.android.com/apk/res/android}name', '.ui.screens.auth.OAuthWebViewActivity')
    new_activity.set('{http://schemas.android.com/apk/res/android}exported', 'false')
    new_activity.set('{http://schemas.android.com/apk/res/android}theme', '@style/Theme.StrangerHelp')
    application.append(new_activity)
    tree.write('app/src/main/AndroidManifest.xml')
    print("Manifest updated.")
else:
    print("Manifest already contains the activity.")
