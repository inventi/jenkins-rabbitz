(ns karotz.test.core
  (:use [karotz.core])
  (:use [clojure.test])
  (:require [clojure.xml :as xml])
  (:import (lt.inventi.karotz Mock$Build Mock$Descriptor Mock$Jenkins)))

(deftest test-error 
         (testing "Should test error"
                  (is (error? "ERROR some other text"))
                  (is (error? "NOT_CONNECTED some other text"))
                  (is (error? "NOT_CONNECTED"))
                  (is (error? "ERROR"))
                  (is (not (error? nil)))))

(deftest test-user-list 
         (testing "Forms names list message"
                  (is (= "testas, testas1, testas2 and testas3" 
                         (user-list ["testas" "testas1" "testas2" "testas3"]))))
         
         (testing "Nil for empty list"
                  (is (nil? (user-list []))))
         
         (testing "Deduplicates names"
                  (is (= "test" (user-list `("test" "test" "test"))))))

(deftest test-report-failure
  (with-redefs [report-build-state (fn [data build message] message)] 
  (testing "Reports failure with users"
             (is (= "failed. Last change was made by test"
                  (with-redefs [commiters-list (constantly "test")]
                    (report-failure nil nil)))))
  
  (testing "Reports failure without users"
             (is (= "failed."
                  (with-redefs [commiters-list (constantly nil)]
                    (report-failure nil nil)))))))


(deftest test-report-recovery
  (with-redefs [report-build-state (fn [data build message] message)] 
  (testing "Reports recovery with users"
             (is (= "is back to normal. Thanks to test"
                  (with-redefs [commiters-list (constantly "test")]
                    (report-recovery nil nil)))))
  
  (testing "Reports recovery without users"
             (is (= "is back to normal."
                  (with-redefs [commiters-list (constantly nil)]
                    (report-recovery nil nil)))))))
                                
(deftest test-sign-in                           
   (testing "Signin should return valid id"
            (is (= {"install1" "123", "install2" "789"}
                   (with-redefs [valid-id? (fn [id] (= id {"install1" "123"}))
                                 karotz-request (constantly "789")
                                 login-url (constantly "")]
                     (sign-in {:interactive-ids {"install1" "123", "install2" "456"}}))))))


(deftest test-karotz-request
(with-redefs [karotz-api ""]
  (testing "Parses correct response"
	  (is (= "INTERACTIVE-ID" 
	         (karotz-request "src/test/resources/ok-response.xml"))))
  
  (testing "Parses erroneus response"
	  (is (= "NOT_CONNECTED" 
	         (karotz-request "src/test/resources/error-response.xml"))))
  
  (testing "Returns io exception if it occurs"
	  (is (= "ERROR java.io.IOException: problem"
           (with-redefs [xml/parse (fn [a] (throw (java.io.IOException. "problem")))]
             (karotz-request "no-such-file")))))

  (testing "Merges correclty ids"
      (is (= {"install1" "ERROR", "install2" "455"}
           (with-redefs [xml/parse (fn [url] (if (= url "&interactiveid=456") "455" "ERROR"))
                         tag-content (fn [tag content] (identity content))]
             (karotz-request {"install1" "123" "install2" "456"} "")))))
  
  (testing "Appends interactive id"
           (is (= {"installation1" "INTERACTIVE-ID-WITH-PARAM", "installation2" "INTERACTIVE-ID-WITH-PARAM"}   
	         (karotz-request {"installation1" "INTERACTIVE-ID", "installation2" "INTERACTIVE-ID"} "src/test/resources/ok-response.xml"))))))

(deftest acceptance-test
  (with-redefs [xml/parse (fn [url] (if (< -1 (.indexOf url "INTERACTIVE-ID1")) "INTERACTIVE-ID1" "INTERACTIVE-ID2"))
                tag-content (fn [tag content] (identity content))
                jenkins (Mock$Jenkins.)
                tts-media (constantly "MEDIA-URL")
                karotz-api ""]
    (testing "Perform should work"
             (is (= {"INSTALLATION1" "INTERACTIVE-ID1", "INSTALLATION2" "INTERACTIVE-ID2"}
                    (-perform nil (Mock$Build.) (Mock$Descriptor.)))))
    (testing "Prebuild should work"
             (is (= {"INSTALLATION1" "INTERACTIVE-ID1", "INSTALLATION2" "INTERACTIVE-ID2"}
                    (-prebuild nil (Mock$Build.) (Mock$Descriptor.)))))))
