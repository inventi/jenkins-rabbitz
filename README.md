# Jenkins Karotz :rabbit: plugin 

This is Karotz plugin for Jenkins. It just reports build failures to Karotz rabbit.

Noteworthy features:
* Multiple karotz - you can have many karotz. This is usefull for split-up teams.
* Google TTS - karotz has its own TTS, but sometimes it sounds quite weird. So we use Google TTS, which gives better speech quality.
* Tells you who broke the build - karotz will say who were authors of the last change. 
* Thanks for fixing build - karotz will also say who have fixed the build and will thank them.
* Spin ears for every build - karotz will remain silent if there are no problems. It just spin its ears when build is starting.

## Why Clojure?

This plugin is mainly written in Clojure.  There is just small adapter written in Java to connect clojure code to Jenkins as a plugin.
We are in progress to learn Clojure, so we use any opportunity we can to write something useful in clojure.


