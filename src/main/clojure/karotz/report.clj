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
        (for [karot karotz]
          (let [result (f karot)]
            (.log log Level/INFO (str "sending request: " result " -> " karot))
            result)))

(defn report-build-state [build message]
  (let [text (str (jenkins/build-name build) " " message)
        tts-url (jenkins/file->url (tts/tts->file! text) build)
        karotz (:karotz build)]
    (request-for-each karotz
                      #(api/say-out-loud % tts-url))))

(defn prepare-message [message commiters-message commiter-names]
  (str message (if (empty? commiter-names) ""
                 (str " " commiters-message " " commiter-names))))

(defn report-failure [build]
  (report-build-state build
                      (prepare-message "failed." "Last change was made by"
                                       (jenkins/commiters-list build))))

(defn report-recovery [build]
  (report-build-state build
                      (prepare-message "is back to normal." "Thanks to"
                                       (jenkins/commiters-list build))))

(defn -prebuild [this jenkins-build jenkins-descriptor]
  (dorun
    (let [build (jenkins/as-build-data jenkins-build jenkins-descriptor)
          karotz (:karotz build)]
      (request-for-each karotz
                        #(api/move-ears %)))))

(defn -perform [this jenkins-build jenkins-descriptor]
  (dorun
    (let [build (jenkins/as-build-data jenkins-build jenkins-descriptor)]
    (if (jenkins/failed? build)
        (report-failure build)
      (if (jenkins/recovered? build)
          (report-recovery build)
        (:karotz build))))))
