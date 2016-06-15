(ns karotz.jenkins
  (:import jenkins.model.Jenkins
           hudson.model.Result
           hudson.FilePath))

(def jenkins (Jenkins/getInstance))

(defrecord Karotz [karotz access-key secret-key])

(defrecord BuildInfo [build-name failed? succeed? recovered? commiters project-url workspace])

(defn file->url [file build]
  (let [target (FilePath. (:workspace build) "karotz-sayz")]
    (with-open [in (clojure.java.io/input-stream file)]
      (.copyFrom target in))
    (str (.getRootUrl jenkins) (:project-url build) "ws/" (.getName target))))

(defn- build-name [build]
  (.. build getProject getName))

(defn- user-list [user-list]
  (let [users (set user-list)]
    (if (< (count users) 2)
      (first users)
      (str (apply str (interpose ", " (butlast users))) " and " (last users)))))

(defn- commiters-list [build]
  (user-list (map #(.getId (.getAuthor %)) (.getChangeSet build))))

(defn- failed? [build]
  (= (.getResult build) Result/FAILURE))

(defn- succeed? [build]
  (= (.getResult build) Result/SUCCESS))

(defn- recovered? [build]
  (let [prev-build (.getPreviousBuild build)]
    (if (nil? prev-build)
      false
      (and (succeed? build) (failed? prev-build)))))

(defn build-info [build]
  (->BuildInfo
    (build-name build)
    (failed? build)
    (succeed? build)
    (recovered? build)
    (commiters-list build)
    (.. build getProject getUrl)
    (.. build getWorkspace)))

(defn karotz [descriptor]
  (->Karotz (.getInstallations descriptor)
            (.accessKey descriptor)
            (.secretKey descriptor)))

