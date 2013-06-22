(ns karotz.test.jenkins
  (:use [karotz.jenkins])
  (:use [clojure.test])
  (:import
    (lt.inventi.karotz Mock$Build Mock$Descriptor Mock$Jenkins Mock$EmptyDescriptor)))

(deftest test-user-list 
         (testing "Forms names list message"
                  (is (= "testas, testas1, testas2 and testas3" 
                         (user-list ["testas" "testas1" "testas2" "testas3"]))))
         
         (testing "Nil for empty list"
                  (is (nil? (user-list []))))
         
         (testing "Deduplicates names"
                  (is (= "test" (user-list `("test" "test" "test"))))))

(deftest data-map
  (testing "correctly maps data"
           (let [build (Mock$Build.)
                 descriptor (Mock$Descriptor.)]
           (is (= {:api-key "API-KEY"
                   :secret "SECRET-KEY"
                   :interactive-ids [["INSTALLATION1" "INTERACTIVE-ID1"]
                                     ["INSTALLATION2" "INTERACTIVE-ID2"]]
                   :build build}
                  (build-data build descriptor)))))
  (testing "correctly maps empty descriptor"
           (let [build (Mock$Build.)
                 descriptor (Mock$EmptyDescriptor.)]
           (is (= {:api-key "API-KEY"
                   :secret "SECRET-KEY"
                   :interactive-ids [["INSTALLATION1" ""]
                                     ["INSTALLATION2" ""]]
                   :build build}
                  (build-data build descriptor))))))


(def build (build-data (Mock$Build.) (Mock$Descriptor.)))
  
(deftest test-status
    (testing "failed build"
             (is (failed? build)))
    (testing "not succeed build"
             (is (not (succeed? build))))
    (testing "recovered"
             (is (not (recovered? build)))))

(deftest test-geters
  (testing "returns workspace"
           (is (= (.. (Mock$Build.) toURI)
                  (workspace-path build))))
  (testing "returns build name"
           (is (= (.. (Mock$Build.) getName)
                  (build-name build))))
  (testing "returns commitesr"
           (is (= (.. (Mock$Build.) getId)
                  (commiters-list build))))
  (testing "file url"
           (with-redefs [jenkins (Mock$Jenkins.)]
             (is (= "http://project/test.txt"
                  (file-url "/test.txt" build))))))