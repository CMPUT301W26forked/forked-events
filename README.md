# How to use API
1. Go to Google Cloud -> create API for Maps SDK for Android | Places API (New)

2. Go to your local.properties -> Add two lines: 
```
PLACES_API_KEY=YOUR_APIKEY
MAPS_API_KEY=YOUR_APIKEY
```
3. Go to AndroidManifest.xml -> Uncomment:
```
<meta-data
android:name="com.google.android.geo.API_KEY"
android:value="${MAPS_API_KEY}" />
```
---
# For detail: 

https://developers.google.com/maps/documentation/android-sdk
https://developers.google.com/maps/documentation/places/web-service/place-autocomplete
[https://developers.google.com/maps/get-started?](https://developers.google.com/maps/documentation/android-sdk?)
