(ns karotz.api
  (:require [clojure.xml :as xml])
  (:import java.io.IOException
           java.net.URLEncoder
           javax.crypto.Mac
           javax.crypto.spec.SecretKeySpec
           org.apache.commons.codec.binary.Base64))

(def karotz-api "http://api.karotz.com/api/karotz/")

(defn tag-content [tag content]
  (first
    (for [x (xml-seq content)
          :when (= tag (:tag x))]
      (first (:content x)))))

(def fail-codes #{"ERROR", "NOT_CONNECTED"})

(defn error? [code]
  (if (nil? code)
    false
    (reduce #(or %1 %2) (map #(< -1 (.indexOf code %)) fail-codes))))


(defn karotz-request
  ([token url]
    (karotz-request (str url "&interactiveid=" token)))

  ([url]
   (let [content (xml/parse (str karotz-api url))
         code (tag-content :code content)]
     (if (error? code)
       code
       (tag-content :interactiveId content)))))


(defn sign-query [query secret]
  (String. (Base64/encodeBase64
             (let [ mac (Mac/getInstance "HmacSHA1")]
               (do
                 (.init mac (SecretKeySpec. (.getBytes secret) "HmacSHA1"))
                 (.doFinal mac (.getBytes query))))) "ASCII"))

(defn login-url [api-key secret installid]
    (let [query (str "apikey=" api-key
                     "&installid=" installid
                     "&once=" (str (.nextInt (java.util.Random.) 99999999))
                     "&timestamp=" (long (/ (System/currentTimeMillis) 1000)))]
      (str "start?" query "&signature=" (URLEncoder/encode (sign-query query secret) "utf8"))))

(defn valid-id? [token]
  (try
    (let [response (karotz-request token "ears?left=0&right=0&relative=true")]
      (not (error? response)))
    (catch IOException e false)))

(defn sign-in
  ([[karotz-id token] credentials]
    (if (valid-id? token)
        token
        (let [{api-key :api-key secret :secret} credentials]
          (karotz-request (login-url api-key secret karotz-id))))))

(defn move-ears
  ([token]
   (move-ears token 20 -30))

  ([token left right]
   (let [request-url (str "ears?left=" left "&right=" right "&relative=true")]
     (do
       (println request-url)
       (karotz-request token request-url)))))

(defn say-out-loud [token media-url]
  (let [request-url (str "multimedia?action=play&url="
                       (URLEncoder/encode media-url))]
    (karotz-request token request-url)))