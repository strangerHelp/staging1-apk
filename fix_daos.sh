for f in app/src/main/java/com/strangerhelp/app/data/local/dao/*.kt; do
  sed -i 's/suspend fun insert\(.*\)(.*)/& : List<Long>/g' "$f"
  sed -i 's/suspend fun clear\(.*\)(.*)/& : Int/g' "$f"
done
