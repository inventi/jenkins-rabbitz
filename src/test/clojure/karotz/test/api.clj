(ns karotz.test.api
  (:use [karotz.api])
  (:use [clojure.test])
  (:require [clojure.xml :as xml]))

(deftest test-error 
         (testing "Should test error"
                  (is (error? "ERROR some other text"))
                  (is (error? "NOT_CONNECTED some other text"))
                  (is (error? "NOT_CONNECTED"))
                  (is (error? "ERROR"))
                  (is (not (error? nil)))))

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
  
  (testing "Returns new interactive id"
           (is (= "INTERACTIVE-ID-WITH-PARAM"   
	         (karotz-request "INTERACTIVE-ID" "src/test/resources/ok-response.xml"))))))

(deftest test-sign-in                           
   (testing "Signin should return valid id"
            (is (= "789"
                   (with-redefs [valid-id? (constantly false)
                                 karotz-request (constantly "789")
                                 login-url (constantly "")]
                     (sign-in ["install1" "123"] {}))))))