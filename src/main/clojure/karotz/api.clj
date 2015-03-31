(ns karotz.api
  (:require [clojure.xml :as xml]
            [clojure.string :as st])
  (:import java.io.IOException
           java.net.URLEncoder
           javax.crypto.Mac
           javax.crypto.spec.SecretKeySpec
           org.apache.commons.codec.binary.Base64
           (java.util.logging Logger Level)))

(def log (Logger/getLogger "lt.inventi.karotz.api"))

(defn karotz-api [host]
  (str "http://" host "/cgi-bin"))

(defn karotz-request
  ([karot url]
   (let [url  (str (karotz-api karot) "/" url)]
     (future
       (try
         (slurp url)
         (catch Exception e
           (.log log Level/SEVERE "failed to send request to karotz" e))))
     url)))

(defn move-ears
  ([karot]
   (move-ears karot 5 3))

  ([karot left right]
   (let [request-url (str "ears?left=" left "&right=" right)]
       (karotz-request karot request-url))))

(defn say-out-loud [karot media-url]
  (let [escaped-url (st/replace (str "sound?url=" media-url) #" " "%20")]
    (karotz-request karot escaped-url)))
