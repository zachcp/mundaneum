(ns mundaneum.write-data
  "port of https://github.com/Wikidata/Wikidata-Toolkit-Examples/blob/master/src/examples/EditOnlineDataExample.java
  to get enough of a hang of it that I can use it to update small bits of real data"
  (:require [clj-http.client :as client])
  (:import [org.wikidata.wdtk.datamodel.helpers Datamodel
                                                ItemDocumentBuilder
                                                StatementBuilder])
  (:import [org.wikidata.wdtk.wikibaseapi ApiConnection
                                          WikibaseDataEditor]))

;
; Note: this is awesome: https://tools.wmflabs.org/wikidata-todo/quick_statements.php
; wikibase-edit is good too https://github.com/maxlath/wikibase-edit
;  - can we get a similar, small API but use with mundaneum?
; http://learningwikibase.com/data-import/ also good
;
; Try to distill down a simple edit statement using the following example:
;   - https://github.com/Wikidata/Wikidata-Toolkit-Examples/blob/master/src/examples/EditOnlineDataExample.java
;
; Note: Q4115189 is sandbox entity
;
; (Datamodel/makeWikidataItemIdValue "Q4115189")
; (Datamodel/makeWikidataPropertyIdValue "P31")
; (Datamodel/makeStatement)

; Use tests to see how everything fits together
;  https://github.com/Wikidata/Wikidata-Toolkit/blob/eb50180ec61db68f18c463c6b564e60fec681370/wdtk-datamodel/src/test/java/org/wikidata/wdtk/datamodel/implementation/StatementImplTest.java
;  https://github.com/Wikidata/Wikidata-Toolkit/blob/eb50180ec61db68f18c463c6b564e60fec681370/wdtk-wikibaseapi/src/test/java/org/wikidata/wdtk/wikibaseapi/WikibaseDataEditorTest.java

; http://baskauf.blogspot.com/2019/06/putting-data-into-wikidata-using.html

(defn make-statement [id prop value]
  (let [id        (Datamodel/makeWikidataItemIdValue id)
        property  (Datamodel/makeWikidataPropertyIdValue prop)
        value     (Datamodel/makeStringValue value)
        statement (StatementBuilder/forSubjectAndProperty id property)]
    (print id)
    (-> statement (.withValue value) (.build))))

(defn make-item-document [id label language  statements]
  (let [id        (Datamodel/makeWikidataItemIdValue id)
        item-doc  (ItemDocumentBuilder/forItemId id)]
   (.withLabel item-doc label language)
   (doseq [statement statements]
     (.withStatement item-doc statement))
   (.build item-doc)))

(def iri "http://www.test.wikidata.org/entity/")
(def conn (ApiConnection/getTestWikidataApiConnection))

;// Optional login -- required for operations on real wikis:
;// connection.login("my username", "my password");

(def statement1 (make-statement "Q4115189" "P166" "Q136696"))
(def item-doc (make-item-document "Q4115189" "WikiDataTest" "en" [statement1]))
item-doc

(def wdbe (WikibaseDataEditor. conn iri))


(.createItemDocument wdbe item-doc "Wikidata Toolkit example test item creation")


;ItemDocument itemDocument = ItemDocumentBuilder.forItemId(noid)
;.withLabel("Wikidata Toolkit test", "en")
;.withStatement(statement1).withStatement(statement2)
;.withStatement(statement3).build();


; "P166" :award-recieved
; "Q136696" Lasker-DeBakey Clinical Medical Research Award
;
(.getValue statement1)
(.getSubject statement1)
(.getClaim statement1)
(.getMainSnak statement1)
(.getStatementId statement1)
(.getA statement1)



;(client/get "http://google.com")
;client/get "http://www.wikidata.org/entity/Q1985727"


  ;(:import [org.wikidata.wdtk.datamodel.helpers Datamodel
  ;                                              ItemDocumentBuilder
  ;                                              ReferenceBuilder
  ;                                              StatementBuilder])
  ;(:import [org.wikidata.wdtk.datamodel.interfaces DatatypeIdValue
  ;                                                 EntityDocument
  ;                                                 ItemDocument
  ;                                                 ItemIdValue
  ;                                                 PropertyDocument
  ;                                                 PropertyIdValue
  ;                                                 Reference
  ;                                                 Statement
  ;                                                 StatementDocument
  ;                                                 StatementGroup EntityIdValue])
  ;(:import [org.wikidata.wdtk.util WebResourceFetcherImpl])
  ;(:import [org.wikidata.wdtk.wikibaseapi ApiConnection
  ;                                        LoginFailedException
  ;                                        WikibaseDataEditor
  ;                                        WikibaseDataEditor
  ;                                        WikibaseDataFetcher])
  ;(:import [org.wikidata.wdtk.wikibaseapi.apierrors EditConflictErrorException
  ;                                                  MediaWikiApiErrorException
  ;                                                  NoSuchEntityErrorException]))


