(ns karotz.test.report
  (:use [karotz.report]
        [karotz.api]
        [karotz.jenkins])
  (:use [clojure.test])
  (:require [clojure.xml :as xml])
  (:import (lt.inventi.karotz Mock$Build Mock$Descriptor Mock$Jenkins)))


(deftest test-report-failure
  (with-redefs [report-build-state (fn [build message] message)] 
  (testing "Reports failure with users"
             (is (= "failed. Last change was made by test"
                  (with-redefs [commiters-list (constantly "test")]
                    (report-failure nil)))))
  
  (testing "Reports failure without users"
             (is (= "failed."
                  (with-redefs [commiters-list (constantly nil)]
                    (report-failure nil)))))))


(deftest test-report-recovery
  (with-redefs [report-build-state (fn [build message] message)] 
  (testing "Reports recovery with users"
             (is (= "is back to normal. Thanks to test"
                  (with-redefs [commiters-list (constantly "test")]
                    (report-recovery nil)))))
  
  (testing "Reports recovery without users"
             (is (= "is back to normal."
                  (with-redefs [commiters-list (constantly nil)]
                    (report-recovery nil)))))))
                                

(deftest acceptance-test
  (with-redefs [xml/parse (fn [url] (if (< -1 (.indexOf url "INTERACTIVE-ID1")) "INTERACTIVE-ID1" "INTERACTIVE-ID2"))
                tag-content (fn [tag content] (identity content))
                jenkins (Mock$Jenkins.)
                tts-media (constantly "MEDIA-URL")
                karotz-api ""]
    (let [build (Mock$Build.)
          descriptor (Mock$Descriptor.)]
    (testing "Perform should work"
             (is (= ["INTERACTIVE-ID1" "INTERACTIVE-ID2"]
                    (-perform nil build descriptor))))
    (testing "Prebuild should work"
             (is (= ["INTERACTIVE-ID1" "INTERACTIVE-ID2"]
                    (-prebuild nil build descriptor)))))))
