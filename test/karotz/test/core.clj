(ns karotz.test.core
  (:use [karotz.core])
  (:use [clojure.test])
  (:use [clojure.contrib.mock]))


(defn should-report-failure []
  (with-redefs [login-url (constantly "LOGIN")
                karotz-request (constantly "")]
     sign-in {}))
