(ns karotz.report
  (:gen-class
  :name "lt.inventi.karotz.KarotzClojureReporter"
  :implements [lt.inventi.karotz.KarotzReporter])
  (:require [clojure.java.io :as io]
            [karotz.jenkins :as jenkins]
            [karotz.tts :as tts]
            [karotz.api :as api])
  (:import (java.util.logging Logger Level)))

(def log (Logger/getLogger "lt.inventi.karotz.report"))

(defn request-for-each [karotz f]
        (for [karot (:karotz karotz)]
          (let [result (f karot)]
            (.log log Level/INFO (str "sending request: " result " -> " karot))
            result)))

(defn report-build-state [karotz build message]
  (let [text (str (:build-name build) " " message)
        tts-url (jenkins/file->url (tts/tts->file! text karotz) build)]
    (request-for-each karotz #(api/say-out-loud % tts-url))))

(defn prepare-message [message commiters-message commiter-names]
  (str message (if (empty? commiter-names) ""
                 (str " " commiters-message " " commiter-names))))

(defn report-failure [karotz build]
  (report-build-state karotz build
                      (prepare-message "failed." "Last change was made by"
                                       (:commiters build))))

(defn report-recovery [karotz build]
  (report-build-state karotz build
                      (prepare-message "is back to normal." "Thanks to"
                                       (:commiters build))))

(defn -prebuild [this jenkins-build jenkins-descriptor]
  (dorun
    (let [karotz (jenkins/karotz jenkins-descriptor)]
      (request-for-each karotz #(api/move-ears %)))))

(defn -perform [this jenkins-build jenkins-descriptor]
  (dorun
    (let [build (jenkins/build-info jenkins-build)
          karotz (jenkins/karotz jenkins-descriptor)]
    (if (:failed? build)
        (report-failure karotz build)
      (if (:recovered? build)
          (report-recovery karotz build)
        (:karotz karotz))))))
