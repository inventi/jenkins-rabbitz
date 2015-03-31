(ns karotz.tts
  (:require [clojure.java.io :as io])
  (:import (javax.sound.sampled AudioSystem AudioFormat AudioFormat$Encoding AudioFileFormat$Type AudioInputStream))
  (:import (java.io SequenceInputStream ByteArrayOutputStream)))

(defn- tts->url [text]
  (java.net.URI. "http" "translate.google.lt" "/translate_tts" (str "tl=en&q=" text) nil))

(defn- tts->stream [text]
  (let [con (.. (tts->url text) toURL openConnection)]
   (do
     (.. con (setRequestProperty "User-Agent" "Mozilla/5.0 ( compatible ) "))
     (.. con getInputStream))))

(defn- as-audio-in
  ([stream]
    (AudioSystem/getAudioInputStream stream))
  ([stream format]
    (AudioSystem/getAudioInputStream format stream)))

(defn- decode-mp3 [stream]
  (let [format (.getFormat stream)
        encoding AudioFormat$Encoding/PCM_SIGNED
        sample-rate (.getSampleRate format)
        bitrate 16
        channels (.getChannels format)
        frame-size (* 2 channels)
        frame-rate sample-rate
        big-endian false]
    (as-audio-in stream
      (AudioFormat.
        encoding sample-rate bitrate channels frame-size frame-rate big-endian))))

(defn- write-wav [audio-stream out-file]
    (AudioSystem/write audio-stream AudioFileFormat$Type/WAVE out-file))

(defn- join-streams [stream1 stream2]
  (AudioInputStream.
    (SequenceInputStream. stream1 stream2)
    (.getFormat stream1)
    (+ (.getFrameLength stream1) (.getFrameLength stream2))))

(defn- tmp-file [s]
  (java.io.File/createTempFile "karotz" s))

(defn tts->file! [text]
  (let [ttsfile (tmp-file "soundz")
        tmpfile (tmp-file "temp")]
    (with-open
      ;karotz cuts about 100ms from begining and 1s from end of media sound.
      ;Thus we have to add extra pause.
      [silence (as-audio-in (io/resource "silence.wav"))
       tts-mp3 (as-audio-in (tts->stream text))]
      (do
        (write-wav (decode-mp3 tts-mp3) tmpfile)
        (with-open [tts-wav (as-audio-in tmpfile)
                    joined (join-streams tts-wav silence)]
          (write-wav joined ttsfile))))
    (.delete tmpfile)
    ttsfile))
