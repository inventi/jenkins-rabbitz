

(defproject jenkins-rabbitz "1.0.0-SNAPSHOT"
  :description "Jenkins karotz plugin"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.jenkins-ci.main/jenkins-core "1.522"]
                 [javax.servlet/servlet-api "2.4"]
                 [com.googlecode.soundlibs/jlayer "1.0.1-1"]
                 [com.googlecode.soundlibs/tritonus-share "0.3.7-1"]
                 [com.googlecode.soundlibs/mp3spi "1.9.5-1"]]

  :source-paths ["src" "src/main/clojure"]
  :test-paths ["test" "src/test/clojure"]
  :java-source-paths ["src/main/java" "src/test/java"]

  :jvm-opts ["-Dfile.encoding=UTF-8" "-Dconsole.encoding=utf-8"]

  :repositories [["jenkins" "http://repo.jenkins-ci.org/releases"]])