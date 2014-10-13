(ns karotz.jenkins  
  (:import jenkins.model.Jenkins
           hudson.model.Result))

(def jenkins (Jenkins/getInstance))

(defrecord build-data [api-key secret interactive-ids build])

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
  (let [prev-build (.getPreviousBuild (:build build ))]
    (if (nil? prev-build)
      false
      (and (succeed? build) (failed? {:build prev-build})))))

(defn token-ids [jenkins-descriptor]
  (let [token-ids (.getTokenIds jenkins-descriptor)
        installations (.getInstallations jenkins-descriptor)]
    (take (count installations) (concat token-ids (repeat "")))))

(defn as-build-data [jenkins-build jenkins-descriptor]
  (let [karotz-id (.getInstallations jenkins-descriptor)
        token-ids (token-ids jenkins-descriptor)]
  (->build-data (.getApiKey jenkins-descriptor)
           (.getSecretKey jenkins-descriptor)
           (reverse (zipmap karotz-id token-ids))
           jenkins-build)))

