(ns karotz.tts
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as st])
  (:import
    (javax.sound.sampled AudioSystem AudioFormat AudioFormat$Encoding AudioFileFormat$Type AudioInputStream)
    (java.io SequenceInputStream ByteArrayOutputStream)
    (com.amazonaws.auth BasicAWSCredentials)
    (com.ivona.services.tts IvonaSpeechCloudClient)
    (com.ivona.services.tts.model CreateSpeechRequest Input Voice)))

(defn- ivona-tts [text credentials]
  (let [ivona (IvonaSpeechCloudClient. (BasicAWSCredentials. (:access-key credentials) (:secret-key credentials)))]
    (.setEndpoint ivona "https://tts.eu-west-1.ivonacloud.com")
    (.getCreateSpeechUrl ivona
                         (doto
                           (CreateSpeechRequest.)
                           (.setInput (doto (Input.) (.setData text)))
                           (.setVoice (doto (Voice.) (.setName "Salli")))))))

(defn- tts->url [text credentials]
  (ivona-tts text credentials))

(defn- tts->stream [text credentials]
  (let [con (.. (tts->url text credentials) openConnection)]
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

(defn tts->file! [text credentials]
  (let [ttsfile (tmp-file "soundz")
        tmpfile (tmp-file "temp")]
    (with-open
      ;karotz cuts about 100ms from begining and 1s from end of media sound.
      ;Thus we have to add extra pause.
      [silence (as-audio-in (io/resource "silence.wav"))
       tts-mp3 (as-audio-in (tts->stream text credentials))]
      (do
        (write-wav (decode-mp3 tts-mp3) tmpfile)
        (with-open [tts-wav (as-audio-in tmpfile)
                    joined (join-streams tts-wav silence)]
          (write-wav joined ttsfile))))
    (.delete tmpfile)
    ttsfile))
