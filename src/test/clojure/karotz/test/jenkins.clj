(ns karotz.test.jenkins
  (:require [karotz.jenkins :as jenkins])
  (:use [clojure.test])
  (:import
    (lt.inventi.karotz Mock$Build Mock$Descriptor Mock$Jenkins Mock$EmptyDescriptor)))

(deftest test-user-list
         (testing "Forms names list message"
                  (is (= "testas1, testas2, testas3 and testas"
                         (#'jenkins/user-list ["testas" "testas1" "testas2" "testas3"]))))

         (testing "Nil for empty list"
                  (is (nil? (#'jenkins/user-list []))))

         (testing "Deduplicates names"
                  (is (= "test" (#'jenkins/user-list `("test" "test" "test"))))))

(def build (jenkins/build-info (Mock$Build.)))

(deftest test-status
    (testing "failed build"
             (is (:failed? build)))
    (testing "not succeed build"
             (is (not (:succeed? build))))
    (testing "recovered"
             (is (not (:recovered? build)))))

(deftest test-geters
  (testing "returns build name"
           (is (= (.. (Mock$Build.) getName)
                  (:build-name build))))
  (testing "returns commitesr"
           (is (= (.. (Mock$Build.) getId)
                  (:commiters build)))))
