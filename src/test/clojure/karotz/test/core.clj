(ns karotz.test.core
  (:use [karotz.core])
  (:use [clojure.test]))

(deftest test-error 
         (testing "Should test error"
                  (is (error? "ERROR"))
                  (is (error? "NOT_CONNECTED"))))

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
                  (is (= "12345"
                         (with-redefs [valid-id? (constantly true)]
                                      (sign-in {:interactive-id "12345"}))))
                  (is (= "new-id"
                         (with-redefs [valid-id? (constantly false)
                                       karotz-request (constantly "new-id")]
                                      (sign-in {:interactive-id "old-id" :secret "123"}))))))



