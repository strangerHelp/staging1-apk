import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    content = f.read()

# Reset first
content = re.sub(r'@PrimaryKey @SerializedName\(.*?\) @SerializedName\(.*?\) val _id', r'@PrimaryKey val _id', content)
content = re.sub(r'@SerializedName\(.*?\) val _id', r'val _id', content)

# Now carefully apply
content = content.replace(
    'val _id: String = ""',
    '@SerializedName(value = "_id", alternate = ["id"]) val _id: String = ""'
)

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(content)
