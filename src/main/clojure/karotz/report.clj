(ns karotz.report
  (:gen-class 
  :name "lt.inventi.karotz.KarotzClojureReporter"
  :implements [lt.inventi.karotz.KarotzReporter])  
  (:require [clojure.java.io :as io])
  (:use [karotz.jenkins :as jenkins]
        [karotz.tts :as tts]
        [karotz.api :as api]))

(defn tts-media [text build]
  (let [file-location (io/file (jenkins/workspace-path build))
        tts-file (tts/as-file text file-location)]
    (jenkins/file-url (str "ws/" tts-file) build)))

(defn request-for-each [tokens f]
        (for [[karotz-id :as token] tokens]
          (f token)))

(defn report-build-state [build message]
  (let [message-text (str (jenkins/build-name build) " " message)
        tts-url (tts-media message-text build)
        tokens (build :interactive-ids)]
    (request-for-each tokens 
                      #(api/say-out-loud (api/sign-in % build) tts-url))))

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
  (let [build (jenkins/build-data jenkins-build jenkins-descriptor)
        tokens (build :interactive-ids)]
    (request-for-each tokens
                  #(api/move-ears (api/sign-in % build)))))

(defn -perform [this jenkins-build jenkins-descriptor]
  (let [build (jenkins/build-data jenkins-build jenkins-descriptor)]
    (if (jenkins/failed? build)
        (report-failure build)
      (if (jenkins/recovered? build)
          (report-recovery build)
        (vals (:interactive-ids build))))))