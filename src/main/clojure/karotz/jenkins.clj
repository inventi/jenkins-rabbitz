(ns karotz.jenkins  
  (:import jenkins.model.Jenkins
           hudson.model.Result))

(def jenkins (Jenkins/getInstance))

(defn workspace-path [{build :build}]
  (.. build getWorkspace toURI))

(defn file-url [file {build :build}]
  (str (.getRootUrl jenkins) (.. build getProject getUrl) file))

(defn build-name [{build :build}]
  (.. build getProject getName))

(defn user-list [user-list]
  (let [users (set user-list)]
    (if (< (count users) 2) 
      (first users)
      (str (apply str (interpose ", " (butlast users))) " and " (last users)))))

(defn commiters-list [{build :build}]
  (user-list (map #(.getId (.getAuthor %)) (.getChangeSet build))))

(defn failed? [{build :build}]
  (= (.getResult build) Result/FAILURE))

(defn succeed? [{build :build}]
  (= (.getResult build) Result/SUCCESS))

(defn recovered? [build]
  (let [prev-build (.getPreviousBuild (build :build))]
    (if (nil? prev-build)
      false
      (and (succeed? build) (failed? {:build prev-build})))))


(defn token-ids [jenkins-descriptor]
  (let [token-ids (.getTokenIds jenkins-descriptor)
        count (count (.getInstallations jenkins-descriptor))]
    (if (empty? token-ids)
      (repeat count "")
      token-ids)))
  

(defn build-data [jenkins-build jenkins-descriptor]
  (let [karotz-id (.getInstallations jenkins-descriptor)
        token-ids (token-ids jenkins-descriptor)]
  (hash-map :api-key (.getApiKey jenkins-descriptor) 
            :secret (.getSecretKey jenkins-descriptor)            
            :interactive-ids (reverse (zipmap karotz-id token-ids)) 
            :build jenkins-build)))