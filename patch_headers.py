import os
import re

def replace_in_file(filepath):
    if not os.path.exists(filepath):
        print(f"File {filepath} not found")
        return
        
    with open(filepath, "r") as f:
        content = f.read()
        
    # We want to replace the whole block of Icon + annotated string with StrangerHelpHeader()
    # In LandingScreen, it's inside a Row
    
    # Let's do a regex to find the Row containing the logo and the annotated string
    # Actually, simpler to just replace the whole text builder and the logo.
    
    # In LandingScreen:
    pattern_landing_top = r'com\.strangerhelp\.app\.ui\.components\.StrangerHelpLogo\(size\s*=\s*32\.dp\)\s*Spacer\(modifier\s*=\s*Modifier\.width\(4\.dp\)\)\s*androidx\.compose\.ui\.text\.buildAnnotatedString\s*\{\s*withStyle\(androidx\.compose\.ui\.text\.SpanStyle\(color\s*=\s*DarkNavy,\s*fontWeight\s*=\s*FontWeight\.Bold,\s*fontSize\s*=\s*20\.sp\)\)\s*\{\s*append\("stranger"\)\s*\}\s*withStyle\(androidx\.compose\.ui\.text\.SpanStyle\(color\s*=\s*OrangePrimary,\s*fontWeight\s*=\s*FontWeight\.Bold,\s*fontSize\s*=\s*20\.sp\)\)\s*\{\s*append\("help"\)\s*\}\s*\}\.let\s*\{\s*text\s*->\s*Text\(text\s*=\s*text\)\s*\}'
    
    if re.search(pattern_landing_top, content):
        content = re.sub(pattern_landing_top, 'com.strangerhelp.app.ui.components.StrangerHelpHeader(logoSize = 32.dp, textSize = 20.sp)', content)
        print("Replaced top header in LandingScreen")

    # In LandingScreen bottom (the one with the logo sized 24dp)
    pattern_landing_bottom = r'com\.strangerhelp\.app\.ui\.components\.StrangerHelpLogo\(size\s*=\s*24\.dp\)\s*Spacer\(modifier\s*=\s*Modifier\.width\(4\.dp\)\)\s*androidx\.compose\.ui\.text\.buildAnnotatedString\s*\{\s*withStyle\(androidx\.compose\.ui\.text\.SpanStyle\(color\s*=\s*DarkNavy,\s*fontWeight\s*=\s*FontWeight\.Bold,\s*fontSize\s*=\s*16\.sp\)\)\s*\{\s*append\("stranger"\)\s*\}\s*withStyle\(androidx\.compose\.ui\.text\.SpanStyle\(color\s*=\s*OrangePrimary,\s*fontWeight\s*=\s*FontWeight\.Bold,\s*fontSize\s*=\s*16\.sp\)\)\s*\{\s*append\("help"\)\s*\}\s*\}\.let\s*\{\s*text\s*->\s*Text\(text\s*=\s*text\)\s*\}'
    
    if re.search(pattern_landing_bottom, content):
        content = re.sub(pattern_landing_bottom, 'com.strangerhelp.app.ui.components.StrangerHelpHeader(logoSize = 24.dp, textSize = 16.sp)', content)
        print("Replaced bottom header in LandingScreen")

    # In LoginScreen, it uses size 96.dp for logo and 32.sp for text
    pattern_login = r'com\.strangerhelp\.app\.ui\.components\.StrangerHelpLogo\(size\s*=\s*96\.dp\)\s*Spacer\(Modifier\.height\(8\.dp\)\)\s*androidx\.compose\.ui\.text\.buildAnnotatedString\s*\{\s*withStyle\(androidx\.compose\.ui\.text\.SpanStyle\(color\s*=\s*MaterialTheme\.colorScheme\.primary,\s*fontWeight\s*=\s*FontWeight\.Bold,\s*fontSize\s*=\s*32\.sp\)\)\s*\{\s*append\("stranger"\)\s*\}\s*withStyle\(androidx\.compose\.ui\.text\.SpanStyle\(color\s*=\s*MaterialTheme\.colorScheme\.secondary,\s*fontWeight\s*=\s*FontWeight\.Bold,\s*fontSize\s*=\s*32\.sp\)\)\s*\{\s*append\("help"\)\s*\}\s*\}\.let\s*\{\s*text\s*->\s*Text\(text\s*=\s*text\)\s*\}'

    # Wait, LoginScreen logo is stacked vertically, not horizontally! 
    # StrangerHelpHeader is horizontal (Row).
    # We should create a StrangerHelpVerticalHeader or just leave the text in LoginScreen but update the text to be same as what user wanted if needed. User just said "Header logo". Login is likely fine, but let's just make sure it's correct.
    
    with open(filepath, "w") as f:
        f.write(content)

replace_in_file("app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt")
