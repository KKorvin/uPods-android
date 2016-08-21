# uPods - Online Radio & Podcasts

uPods is Android app, with material design, which allows you to listen more than 30k online radio stations. 
uPods also has basic podcasts supports and allows search and add new podcasts directly from iTunes database. 
For managing your profile uPods provides one click registration with FB, Twitter or VK and cloud sync across all your devices.

For now uPods is available only on Android, but I am also working on Google Chrome extension which will be available
in near time. I am also thinking about extending to other platforms.

uPods also has a backend part created in REST API way. It uses uwsgi + nginx for webserver, python and Flask for dealing 
with code, old good MySQL as database and redis for caching.

Link to Google Play: https://play.google.com/store/apps/details?id=com.chickenkiller.upods2

Key Features
-----------------------------------------------
- Free and open sourced.
- 30k radio stations
- Handy made tops (English, Russian)
- Direct access to to iTunes podcasts.
- VLC based audio and video player.
- Material design.
- Select stream quality.
- Cloud based sync for your subscriptions and favorite lists.

Using app code for learning Android development
-----------------------------------------------

I used some modules and code pieces from old first version of uPods, 
thats why in some places (specially xml layouts and resources) code has not the best design, 
but you still can use code for learning Android.

In app you can find examples of next things:

 - Implementation of social networks login (Twitter, VK, Facebook)
 - Threading with AsyncTask, using callbacks for UI updating from background threads
 - Working with JSON & XML (SAX parser) formats
 - Services and Intent Services
 - Notifications
 - sqlite
 - Lots of work with fragments
 - Custom Listview & Recyclerview adapters 
 - SlidingDrawer
 - Animations
 - okhttp3, glide, vlc-android-sdk and other popular open source libraries
 
Building  project
-----------------------------------------------
To build the project you will need Config.java file which is not inside repositary. This file contains social networks and analytics api keys, you can create this file with empty strings to compile the project or use the real api keys from your accounts to make all the functionality work. You will see which variables are missing after importing the project to your IDE.

Contribution
-----------------------------------------------

Contribution is always welcome, if you will need some help with backend API, just ping me to alon.milo@gmail.com
