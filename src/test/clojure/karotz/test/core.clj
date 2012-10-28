(ns karotz.test.core
  (:use [karotz.core])
  (:use [clojure.test]))

(testing "Should test error"
         (is (error? "ERROR"))
         (is (error? "NOT_CONNECTED")))

(testing "Signin should return valid id"
         (is (= "12345"
                (with-redefs [valid-id? (constantly true)]
                             (sign-in {:interactive-id "12345"}))))
         (is (= "new-id"
                (with-redefs [valid-id? (constantly false)
                              karotz-request (constantly "new-id")]
                             (sign-in {:interactive-id "old-id" :secret "123"})))))



