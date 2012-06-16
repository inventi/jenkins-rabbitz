(ns karotz.core
  (:gen-class :methods [#^{:static true} [doKarotz [String String String] String]])
  (:require [clojure.xml :as xml]))

(def karotz-api "http://api.karotz.com/api/karotz/")
(declare sign-in sign-out move-ears login-url sign-query tag-content karotz-request fetch-id) 

(defn -doKarotz [api-key install-id secret]
  (sign-out (move-ears (sign-in (hash-map :api-key api-key :install-id install-id :secret secret)))))

(defn sign-in 
  "sign-ins to karotz with provided data. Data should be provided as map.
  {:api-key <api-key> :install-id <install-id> :secret <secret>}"
  [data] 
  (karotz-request (login-url data)))

(defn karotz-request
  ([interactive-id url] (karotz-request (str url "&interactiveid=" interactive-id)))
  ([url] 
   (let [content (xml/parse url) 
         interactive-id (fetch-id content)]
     (if (empty? interactive-id)
       content
       interactive-id))))


(defn move-ears [interactive-id]
  (karotz-request interactive-id
                  (str karotz-api "ears?left=20&right=-30&relative=true")))

(defn sign-out 
  ([interactive-id]
   (karotz-request interactive-id
                   (str karotz-api "interactivemode?action=stop"))))

(defn fetch-id [content]
  (tag-content :interactiveId content))

(defn tag-content [tag content]
  (first 
    (for [x (xml-seq content) 
        :when (= tag (:tag x))]
    (first (:content x)))))

(defn login-url [data]
    (let [query (str "apikey=" (data :api-key) 
                     "&installid=" (data :install-id) 
                     "&once=" (str (.nextInt (java.util.Random.) 99999999)) 
                     "&timestamp=" (long (/ (System/currentTimeMillis) 1000)))]
      (str karotz-api "start?" query "&signature=" (java.net.URLEncoder/encode (sign-query query (data :secret)) "utf8"))))

(defn sign-query [query secret]
  (String. (org.apache.commons.codec.binary.Base64/encodeBase64 
             (let [ mac (javax.crypto.Mac/getInstance "HmacSHA1")]
               (do 
                 (.init mac (javax.crypto.spec.SecretKeySpec. (.getBytes secret) "HmacSHA1"))
                 (.doFinal mac (.getBytes query))))) "ASCII"))
