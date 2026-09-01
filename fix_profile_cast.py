import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace("ProfileRating(rating = user.rating, totalReviews = user.totalReviews)", "ProfileRating(rating = user?.rating ?: 0.0, totalReviews = user?.totalReviews ?: 0)")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)
