(ns karotz.jenkins
  (:import jenkins.model.Jenkins
           hudson.model.Result
           hudson.FilePath))

(def jenkins (Jenkins/getInstance))

(defrecord build-data [karotz build])

(defn workspace-path [{build :build}]
  (.. build getWorkspace toURI))

(defn file->url [file {build :build}]
  (let [target (FilePath. (.. build getWorkspace) "karotz-sayz")]
    (with-open [in (clojure.java.io/input-stream file)]
      (.copyFrom target in))
    (str (.getRootUrl jenkins) (.. build getProject getUrl) "ws/" (.getName target))))

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

(defn as-build-data [jenkins-build jenkins-descriptor]
  (->build-data (.getInstallations jenkins-descriptor) jenkins-build))

