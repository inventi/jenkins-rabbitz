(ns karotz.core
(:gen-class 
  :name "lt.inventi.karotz.KarotzClojureReporter"
  :implements [lt.inventi.karotz.KarotzReporter])
  (:require [clojure.xml :as xml])
  (:require [clojure.java.io :as io])
  (:use [clojure.contrib.logging :only (info)])
  (:import (javax.sound.sampled AudioSystem AudioFormat AudioFormat$Encoding AudioFileFormat$Type AudioInputStream))
  (:import (java.io SequenceInputStream ByteArrayOutputStream)))

(defn tag-content [tag content]
  (first 
    (for [x (xml-seq content) 
          :when (= tag (:tag x))]
      (first (:content x)))))

(def fail-codes #{"ERROR", "NOT_CONNECTED"})

(defn error? [code]
  (contains? fail-codes code))

(def karotz-api "http://api.karotz.com/api/karotz/")

(defn karotz-request
  ([interactive-id url] (karotz-request (str url "&interactiveid=" interactive-id)))
  ([url] 
   (try (let [content (xml/parse (str karotz-api url))  
         code (tag-content :code content)]
     (if (error? code)
       code
       (tag-content :interactiveId content)))
          (catch java.io.IOException e "ERROR"))))

(defn tts-media-url [text]
  (java.net.URI. "http" "translate.google.lt" "/translate_tts" (str "tl=en&q=" text) nil))


(defn tts-stream [tts-text] 
  (let [con (.. (tts-media-url tts-text) toURL openConnection)]
   (do (.. con (setRequestProperty "User-Agent" "Mozilla/5.0 ( compatible ) "))
     (.. con getInputStream))))


(defn audio-input-stream 
  ([stream] 
    (AudioSystem/getAudioInputStream stream))
  ([stream format]
    (AudioSystem/getAudioInputStream format stream)))


(defn decode-mp3 [stream]
  (let [format (.getFormat stream)
        encoding AudioFormat$Encoding/PCM_SIGNED
        sample-rate (.getSampleRate format)
        bitrate 16
        channels (.getChannels format)
        frame-size (* 2 channels)
        frame-rate sample-rate
        big-endian false]
    (audio-input-stream stream
      (AudioFormat.
        encoding sample-rate bitrate channels frame-size frame-rate big-endian))))


(defn write-wav [audio-stream out-file]
    (AudioSystem/write audio-stream AudioFileFormat$Type/WAVE (io/file out-file)))
          
(defn join-stream [stream1 stream2]
  (AudioInputStream. 
    (SequenceInputStream. stream1 stream2)
    (.getFormat stream1)
    (+ (.getFrameLength stream1) (.getFrameLength stream2))))


(defn tts-to-file [tts-text location]
;karotz cuts about 100ms from begining and 1s from end of media sound.
;Thus we have to add extra pause.  
(with-open 
  [silence (audio-input-stream (io/resource "silence.wav"))
   tts-mp3 (audio-input-stream (tts-stream tts-text))]
  (do
	  (write-wav (decode-mp3 tts-mp3) (io/file location "tts.wav"))
	  (with-open 
	    [tts-wav (audio-input-stream (io/file location "tts.wav"))
	     joined (join-stream tts-wav silence)]
	    (write-wav joined (str location "/build-tts.wav"))))))


(defn tts-media [text build]
  (do 
    (tts-to-file text (io/file (.. build getWorkspace toURI)))
      (str 
	       (.. (jenkins.model.Jenkins/getInstance) getRootUrl)
	       (.. build getProject getUrl) 
	       "ws/build-tts.wav")))


(defn say-out-loud [text interactive-id build]
  (let [media-url (tts-media text build)]
    (karotz-request interactive-id (str "multimedia?action=play&url=" (java.net.URLEncoder/encode media-url)))))

(defn move-ears [interactive-id]
  (karotz-request interactive-id "ears?left=20&right=-30&relative=true"))

(defn sign-out 
  ([interactive-id]
   (karotz-request interactive-id "interactivemode?action=stop")))

(defn sign-query [query secret]
  (String. (org.apache.commons.codec.binary.Base64/encodeBase64 
             (let [ mac (javax.crypto.Mac/getInstance "HmacSHA1")]
               (do 
                 (.init mac (javax.crypto.spec.SecretKeySpec. (.getBytes secret) "HmacSHA1"))
                 (.doFinal mac (.getBytes query))))) "ASCII"))

(defn login-url [data]
    (let [query (str "apikey=" (data :api-key) 
                     "&installid=" (data :install-id) 
                     "&once=" (str (.nextInt (java.util.Random.) 99999999)) 
                     "&timestamp=" (long (/ (System/currentTimeMillis) 1000)))]
      (str "start?" query "&signature=" (java.net.URLEncoder/encode (sign-query query (data :secret)) "utf8"))))

(defn valid-id? [interactive-id]
  (if (boolean interactive-id)
    (let [response (karotz-request interactive-id "ears?left=10&right=-10&relative=true")]
      (not (error? response)))))

(defn sign-in 
  "sign-ins to karotz with provided data. Data should be provided as map.
  {:api-key <api-key> :install-id <install-id> :secret <secret> :interactive-id <last known interactive id>}"
  ([data]
   (if (valid-id? (:interactive-id data))
     (:interactive-id data)
     (karotz-request (login-url data)))))


(defn user-list [[user & others :as users]]
  (if (empty? others) user
    (str (apply str (interpose ", " (butlast users))) " and " (last users))))

(defn commiters-list [build]
  (user-list (map #(.getId (.getAuthor %)) (.getChangeSet build))))

(defn report-build-state [build-data build message]
    (say-out-loud (str (:name build-data) " " message) (sign-in build-data) build))

(defn report-failure [build-data build]
  (report-build-state build-data 
                      build
                      (str "failed. Last change was made by " (commiters-list build))))

(defn report-recovery [build-data build]
  (report-build-state build-data 
                      (str "is back to normal thanks to " (commiters-list build))))

(defn map-build-data [build descriptor]
  (hash-map :api-key (.getApiKey descriptor) 
            :install-id (.getInstallationId descriptor) 
            :secret (.getSecretKey descriptor)
            :interactive-id (.getInteractiveId descriptor)
            :name (.getName (.getProject build))))

(import hudson.model.Result)
(defn failed? [build]
  (= (.getResult build) Result/FAILURE))

(defn succeed? [build]
  (= (.getResult build) Result/SUCCESS))

(defn recovered? [this-build]
  (let [prev-build (.getPreviousBuild this-build)]
    (if (nil? prev-build)
      false
      (and (succeed? this-build) (failed? prev-build)))))

(defn -prebuild [this build descriptor]
  (let [build-data (map-build-data build descriptor)
        build-name (:name build-data)]
  (do 
    (info (str "reporting build start " build-name)) 
    (move-ears (sign-in build-data)))))

(defn -perform [this build descriptor]
  (let [build-data (map-build-data build descriptor)
        build-name (:name build-data)]
    (if (failed? build)
      (do 
        (info (str "reporting build failure " build-name)) 
        (report-failure build-data build))
      (if (recovered? build)
        (do 
          (info (str "reporting build recovery " build-name))
          (report-recovery build-data build))
        (:interactive-id build-data)))))

