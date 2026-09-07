import os

foreground = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Perfectly centered map pin, scaled to exactly fit the 66dp Android safe zone -->
    <group
        android:translateX="42"
        android:translateY="42"
        android:pivotX="12"
        android:pivotY="12"
        android:scaleX="3.3"
        android:scaleY="3.3">
        
        <!-- Dark Navy Map Pin -->
        <path
            android:fillColor="#101828"
            android:pathData="M12,2 C8.13,2 5,5.13 5,9 c0,5.25 7,13 7,13 s7,-7.75 7,-13 c0,-3.87 -3.13,-7 -7,-7 z" />

        <!-- Handshake inside the bulb of the pin, explicitly scaled and positioned -->
        <group
            android:translateX="0"
            android:translateY="-2.5"
            android:pivotX="12"
            android:pivotY="12"
            android:scaleX="0.65"
            android:scaleY="0.65">
            
            <path
                android:fillColor="#FFFFFF"
                android:strokeColor="#F5A623"
                android:strokeWidth="1.2"
                android:strokeLineJoin="round"
                android:pathData="M16.48,10.41c-0.39,0.39-1.04,0.39-1.43,0l-4.47-4.46l-7.05,7.04l-0.66-0.63c-1.17-1.17-1.17-3.07,0-4.24l4.24-4.24 c1.17-1.17,3.07-1.17,4.24,0L16.48,9C16.87,9.39,16.87,10.02,16.48,10.41z M17.18,8.29c0.78,0.78,0.78,2.05,0,2.83 c-1.27,1.27-2.61,0.22-2.83,0l-3.76-3.76l-5.57,5.57c-0.39,0.39-0.39,1.02,0,1.41c0.39,0.39,1.02,0.39,1.42,0l4.62-4.62l0.71,0.71 l-4.62,4.62c-0.39,0.39-0.39,1.02,0,1.41c0.39,0.39,1.02,0.39,1.42,0l4.62-4.62l0.71,0.71l-4.62,4.62c-0.39,0.39-0.39,1.02,0,1.41 c0.39,0.39,1.02,0.39,1.41,0l4.62-4.62l0.71,0.71l-4.62,4.62c-0.39,0.39-0.39,1.02,0,1.41c0.39,0.39,1.02,0.39,1.41,0l8.32-8.34 c1.17-1.17,1.17-3.07,0-4.24l-4.24-4.24c-1.15-1.15-3.01-1.17-4.18-0.06L17.18,8.29z" />
        </group>
    </group>
</vector>
"""

brand = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#101828"
        android:pathData="M12,2 C8.13,2 5,5.13 5,9 c0,5.25 7,13 7,13 s7,-7.75 7,-13 c0,-3.87 -3.13,-7 -7,-7 z" />
    <group
        android:translateX="0"
        android:translateY="-2.5"
        android:pivotX="12"
        android:pivotY="12"
        android:scaleX="0.65"
        android:scaleY="0.65">
        <path
            android:fillColor="#FFFFFF"
            android:strokeColor="#F5A623"
            android:strokeWidth="1.2"
            android:strokeLineJoin="round"
            android:pathData="M16.48,10.41c-0.39,0.39-1.04,0.39-1.43,0l-4.47-4.46l-7.05,7.04l-0.66-0.63c-1.17-1.17-1.17-3.07,0-4.24l4.24-4.24 c1.17-1.17,3.07-1.17,4.24,0L16.48,9C16.87,9.39,16.87,10.02,16.48,10.41z M17.18,8.29c0.78,0.78,0.78,2.05,0,2.83 c-1.27,1.27-2.61,0.22-2.83,0l-3.76-3.76l-5.57,5.57c-0.39,0.39-0.39,1.02,0,1.41c0.39,0.39,1.02,0.39,1.42,0l4.62-4.62l0.71,0.71 l-4.62,4.62c-0.39,0.39-0.39,1.02,0,1.41c0.39,0.39,1.02,0.39,1.42,0l4.62-4.62l0.71,0.71l-4.62,4.62c-0.39,0.39-0.39,1.02,0,1.41 c0.39,0.39,1.02,0.39,1.41,0l4.62-4.62l0.71,0.71l-4.62,4.62c-0.39,0.39-0.39,1.02,0,1.41c0.39,0.39,1.02,0.39,1.41,0l8.32-8.34 c1.17-1.17,1.17-3.07,0-4.24l-4.24-4.24c-1.15-1.15-3.01-1.17-4.18-0.06L17.18,8.29z" />
    </group>
</vector>
"""

with open("app/src/main/res/drawable/ic_launcher_foreground.xml", "w") as f:
    f.write(foreground)

with open("app/src/main/res/drawable/ic_logo_brand.xml", "w") as f:
    f.write(brand)

print("Icons synchronized and updated.")
