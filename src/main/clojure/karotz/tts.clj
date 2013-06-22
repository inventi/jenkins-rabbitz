(ns karotz.tts
  (:require [clojure.java.io :as io])
  (:import (javax.sound.sampled AudioSystem AudioFormat AudioFormat$Encoding AudioFileFormat$Type AudioInputStream))
  (:import (java.io SequenceInputStream ByteArrayOutputStream)))

(defn tts-media-url [text]
  (java.net.URI. "http" "translate.google.lt" "/translate_tts" (str "tl=en&q=" text) nil))

(defn tts-stream [tts-text] 
  (let [con (.. (tts-media-url tts-text) toURL openConnection)]
   (do 
     (.. con (setRequestProperty "User-Agent" "Mozilla/5.0 ( compatible ) "))
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


(defn as-file [tts-text location]
;karotz cuts about 100ms from begining and 1s from end of media sound.
;Thus we have to add extra pause.  
(with-open 
  [silence (audio-input-stream (io/resource "silence.wav"))
   tts-mp3 (audio-input-stream (tts-stream tts-text))]
  (let [file "build-tts.wav"]
  (do
	  (write-wav (decode-mp3 tts-mp3) (io/file location "tts.wav"))
	  (with-open 
	    [tts-wav (audio-input-stream (io/file location "tts.wav"))
	     joined (join-stream tts-wav silence)]
	    (write-wav joined (str location "/" file)))
   file))))