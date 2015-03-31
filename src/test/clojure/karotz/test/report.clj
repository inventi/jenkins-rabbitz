(ns karotz.test.report
  (:use [karotz.report])
  (:use [clojure.test])
  (:require [clojure.xml :as xml]
            [karotz.jenkins :as jenkins])
  (:import (lt.inventi.karotz Mock$Build Mock$Descriptor Mock$Jenkins)))


(deftest test-report-failure
  (with-redefs [report-build-state (fn [build message] message)]
  (testing "Reports failure with users"
             (is (= "failed. Last change was made by test"
                  (with-redefs [jenkins/commiters-list (constantly "test")]
                    (report-failure nil)))))

  (testing "Reports failure without users"
             (is (= "failed."
                  (with-redefs [jenkins/commiters-list (constantly nil)]
                    (report-failure nil)))))))


(deftest test-report-recovery
  (with-redefs [report-build-state (fn [build message] message)]
  (testing "Reports recovery with users"
             (is (= "is back to normal. Thanks to test"
                  (with-redefs [jenkins/commiters-list (constantly "test")]
                    (report-recovery nil)))))

  (testing "Reports recovery without users"
             (is (= "is back to normal."
                  (with-redefs [jenkins/commiters-list (constantly nil)]
                    (report-recovery nil)))))))

