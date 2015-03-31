(ns karotz.test.jenkins
  (:require [karotz.jenkins :as jenkins])
  (:use [clojure.test])
  (:import
    (lt.inventi.karotz Mock$Build Mock$Descriptor Mock$Jenkins Mock$EmptyDescriptor)))

(deftest test-user-list
         (testing "Forms names list message"
                  (is (= "testas, testas1, testas2 and testas3"
                         (jenkins/user-list ["testas" "testas1" "testas2" "testas3"]))))

         (testing "Nil for empty list"
                  (is (nil? (jenkins/user-list []))))

         (testing "Deduplicates names"
                  (is (= "test" (jenkins/user-list `("test" "test" "test"))))))

(def build (jenkins/as-build-data (Mock$Build.) (Mock$Descriptor.)))

(deftest test-status
    (testing "failed build"
             (is (jenkins/failed? build)))
    (testing "not succeed build"
             (is (not (jenkins/succeed? build))))
    (testing "recovered"
             (is (not (jenkins/recovered? build)))))

(deftest test-geters
  (testing "returns workspace"
           (is (= (.. (Mock$Build.) toURI)
                  (jenkins/workspace-path build))))
  (testing "returns build name"
           (is (= (.. (Mock$Build.) getName)
                  (jenkins/build-name build))))
  (testing "returns commitesr"
           (is (= (.. (Mock$Build.) getId)
                  (jenkins/commiters-list build)))))
