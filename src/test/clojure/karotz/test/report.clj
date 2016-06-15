(ns karotz.test.report
  (:use [karotz.report])
  (:use [clojure.test])
  (:require [clojure.xml :as xml]
            [karotz.jenkins :as jenkins]
            [karotz.tts :as tts])
  (:import (lt.inventi.karotz Mock$Build Mock$Descriptor Mock$Jenkins)))

(deftest test-report-failure
  (with-redefs [report-build-state (fn [_ _ msg] msg)]
    (testing "Reports failure with users"
      (is (= "failed. Last change was made by test"
             (report-failure {} {:commiters "test"}))))

    (testing "Reports failure without users"
      (is (= "failed."
             (report-failure {} {:commiters nil}))))))

(deftest test-report-recovery
  (with-redefs [report-build-state  (fn  [_ _ msg] msg)]
    (testing "Reports recovery with users"
      (is (= "is back to normal. Thanks to test"
             (report-recovery {} {:commiters "test"}))))

    (testing "Reports recovery without users"
      (is (= "is back to normal."
             (report-recovery {} {:commiters nil}))))))

