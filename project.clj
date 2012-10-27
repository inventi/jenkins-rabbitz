(defproject lt.inventi/karotz "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :source-paths ["src/main/clojure"]
  :dependencies [[org.clojure/clojure "1.5.0-alpha5"]
                 [commons-codec/commons-codec "1.5"]
                 [org.jenkins-ci.main/jenkins-core "1.424"]]
  :dev-dependencies [[vimclojure/server "2.3.1"]
                     [org.clojure/data.xml "0.0.6"]])
