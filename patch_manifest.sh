sed -i '/<\/application>/i \
        <provider\n            android:name="androidx.core.content.FileProvider"\n            android:authorities="${applicationId}.provider"\n            android:exported="false"\n            android:grantUriPermissions="true">\n            <meta-data\n                android:name="android.support.FILE_PROVIDER_PATHS"\n                android:resource="@xml/file_paths" \/>\n        <\/provider>\n\n        <service\n            android:name=".service.TrackingService"\n            android:foregroundServiceType="location"\n            android:exported="false" \/>\n' app/src/main/AndroidManifest.xml

sed -i '/<uses-permission android:name="android.permission.INTERNET" \/>/a \
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" \/>\n    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" \/>' app/src/main/AndroidManifest.xml
