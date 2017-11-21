(ns mundaneum.examples
  (:require [mundaneum.query    :refer [describe entity label property query stringify-query]
             [backtick           :refer [template]]
             [clj-time.format    :as    tf]]))



;;
;; SPARL Queries Translated From
;; https://bitbucket.org/sulab/wikidatasparqlexamples/src
;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; drugs and diseases

(property :canonical-SMILES)

;; predecessors
(query
  '[:select :distinct ?pLabel
    :where [[?p (p :CAS-registry-number) ?cas]]
    :limit 5])
;{:pLabel "actinium"}
;{:pLabel "antimony"}
;{:pLabel "caesium"}
;{:pLabel "dubnium"}
;{:pLabel "europium"}

; Multiple Sclerosis from NCi THesaurus
(query
  '[:select ?disease ?diseaseLabel ?diseaseDescription ?drug ?drugLabel ?drugDescription ?link
    :where [[?disease (wdt :NCI-Thesaurus-ID) "C3243"]]])

; Get drug
(query
  '[:select ?disease ?diseaseLabel ?diseaseDescription ?drug ?drugLabel ?drugDescription ?link
    :where [[?disease      (wdt :NCI-Thesaurus-ID)       "C3243"]
            [?disease      (p :drug-used-for-treatment)  ?disease_drug]
            [?disease_drug (ps :drug-used-for-treatment) ?drug]]])


(query
  ;(stringify-query
  '[:select ?disease ?diseaseLabel ?diseaseDescription ?drug ?drugLabel ?drugDescription ?chemblid ?ndfid
    :where [[?disease      (wdt :NCI-Thesaurus-ID)       "C3243"]
            [?disease      (p :drug-used-for-treatment)  ?disease_drug]
            [?disease_drug (ps :drug-used-for-treatment) ?drug]
            [?disease_drug (prov)                        ?reference]
            :optional [[?reference (pr :ChEMBL-ID) ?chemblid]]
            :optional [[?reference (pr :NDF-RT-ID) ?ndfid]]]])

;; the following required :bind which is not yet implemented.
;;
;(query)
;(stringify-query
;  '[:select ?disease ?diseaseLabel ?diseaseDescription ?drug ?drugLabel ?drugDescription ?chemblid ?ndfid ?genid
;    :where [[?disease      (wdt :NCI-Thesaurus-ID)       "C3243"]
;            [?disease      (p :drug-used-for-treatment)  ?disease_drug]
;            [?disease_drug (ps :drug-used-for-treatment) ?drug]
;            [?disease_drug (prov)                        ?reference]
;            :optional [[?reference (pr :ChEMBL-ID) ?chemblid]
;                       :bind [?chemid :as ?genid]]
;            :optional [[?reference (pr :NDF-RT-ID) ?ndfid]
;                       :bind [?ndfid ?genid]]]])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Drug Repurposing


;; Drug interacts with protein encoded by gene with association to disease. Showing Metformin
(query
;(stringify-query
  '[:select ?gene ?geneLabel ?disease ?diseaseLabel
    :where [[(wd "Q19484") (wdt :physically-interacts-with) ?gene_product]
            [?gene_product  (wdt :encoded-by) ?gene]
            [?gene (wdt :genetic-association) ?disease]]
    :limit 100])


;;
;; Drug
;;
(query
;(stringify-query
  '[:select ?drugLabel ?geneLabel ?biological_processLabel ?diseaseLabel
    :where [[?drug          (wdt :physically-interacts-with) ?gene_product]
            [?gene          (wdt :encodes) ?gene_product]
            [?disease       (wdt :genetic-association) ?gene]
            [?disease       (wdt :subclass-of)* (wd Q12078)]
            [?gene_product  (wdt :biological-process) ?biological_process]
            [?gene          (wdt :genetic-association) ?disease]
            :union [[?biological_process (wdt :subclass-of)* (entity "cell proliferation")]
                    [?biological_process (wdt :part-of)* (entity "cell proliferation")]]]
    :limit 10])


;;
;; Get mapping of Wikipedia to WikiData to Entrez Gene
;;
;; doens't have filter, substring, string
(query
;(stringify-query
  '[:select ?entrez_id ?cid ?article ?label
    :where [[?cid           (wdt :Entrez-Gene-ID) ?entrez_id]
            [?cid           (wdt :found-in-taxon) (entity "Homo sapiens")]
            [?article schema:about ?cid]
            [?article schema:inLanguage "en"]]
    :limit 10])

;SELECT ?entrez_id ?cid ?article ?label WHERE {}
;     ?cid wdt:P351 ?entrez_id .
;     ?cid wdt:P703 wd:Q15978631 .
;     OPTIONAL {
;               ?cid rdfs:label ?label filter (lang(?label) = "en") .}
;
;     ?article schema:about ?cid .
;     ?article schema:inLanguage "en" .
;     FILTER (SUBSTR(str(?article), 1, 25) = "https://en.wikipedia.org/") .
;     FILTER (SUBSTR(str(?article), 1, 38) != "https://en.wikipedia.org/wiki/Template")
;
;limit 10



;Get all the gene ontology evidence codes used in wikidata
(query
  ;(stringify-query
  '[:select distinct ?evidence_code ?evidence_codeLabel
    :where [[?evidence_code (wdt :instance-of) (entity "Gene Ontology Evidence code")]]
    :limit 10])


; Get 10 Gene Ontology subcellular localization information, with evidence codes, and references for Reelin
(query
  ;(stringify-query
  '[:select distinct ?go_bp ?go_bpLabel ?determination ?determinationLabel ?reference_stated_inLabel ?reference_retrieved
    :where [[(entity "Reelin") (wdt :cell-component) ?go_bp]
            [(entity "Reelin") (p :cell-component)   ?go_bp_statement]
            [?go_bp_statement  (pq :determination-method)   ?determination]
            [?go_bp_statement prov:wasDerivedFrom/pr:P248 ?reference_stated_in]
            [?go_bp_statement prov:wasDerivedFrom/pr:P813 ?reference_retrieved]]
    :limit 10])

;SELECT distinct ?go_bp ?go_bpLabel ?determination ?determinationLabel ?reference_stated_inLabel ?reference_retrieved WHERE {}
;       #?protein wdt:P352 "P78509" . # get a protein by uniprot id
;       # note the difference between wdt:P681 and p:681 in the following two statements
;       #wdt gets you to the value of the property (generally what you would expect)
;       #p gets you to the wikidata statement (which is where qualifiers and references live)
;       wd:Q13561329 wdt:P681 ?go_bp . # get a protein record directly and get biological process annotations
;       wd:Q13561329 p:P681 ?go_bp_statement . #get the statements associated with the bp annotations
;       ?go_bp_statement pq:P459 ?determination . # get 'determination method' qualifiers associated with the statements
;       # change to wd:Q23175558 for ISS (Inferred from Sequence or structural Similarity)
;       # or e.g. wd:Q23190881 for IEA (Inferred from Electronic Annotation)
;       #add reference links
;       ?go_bp_statement prov:wasDerivedFrom/pr:P248 ?reference_stated_in . #where stated
;       ?go_bp_statement prov:wasDerivedFrom/pr:P813 ?reference_retrieved . #when retrieved
;       #add labels to everything (and retrieve by appending Label to the item you want in the response)
;       SERVICE wikibase:label {
;                               bd:serviceParam wikibase:language "en" .}
;
;
;limit 10

; Get all monoclonal antibodies which could be used for treatment of melanoma
(query
  ;(stringify-query
  '[:select ?ab ?abLabel ?cas ?gtp ?chembl
    :where [[?ab (wdt :instance-of)    (entity "monoclonal antibody")]
            ;[?ab (wdt :medical-condition-treated) (entity "melanoma")]
            [?ab (wdt :medical-condition-treated) (entity"unstable angina")]
            :optional [[?ab (wdt :instance-of) ?cas]
                       [?ab (wdt :IUPHAR-ID) ?gtp]
                       [?ab (wdt :ChEMBL-ID) ?chembl]]]
    :limit 100])

; Get all the drug-drug interactions for Methadone based on its CHEMBL id CHEMBL651
(query
  ;(stringify-query
  '[:select ?compound ?compoundLabel  ?chembl
    :where [[(entity "methadone") (wdt :significant-drug-interaction) ?compound]
            [?compound (wdt :ChEMBL-ID) ?chembl]]
    :limit 10])

;Get all wikidata statements that cite an academic article as a reference
(query
;(stringify-query
  '[:select ?statement ?PMID ?PMCID
    :where [[?statement (prov) / (pr :stated-in)  ?paper]
            [?paper  (wdt :instance-of) (entity "scientific article")]
            :optional [[?paper (wdt :PubMed-ID) ?PMID]]
            :optional [[?paper (wdt :PMCID) ?PMCID]]]
    :limit 10])

;SELECT ?statement ?PMID ?PMCID WHERE {}
;     ?statement prov:wasDerivedFrom/pr:P248 ?paper .
;     ?paper wdt:P31 wd:Q13442814 .
;     OPTIONAL { ?paper wdt:P698 ?PMID .}
;     OPTIONAL { ?paper wdt:P932 ?PMCID .}
;     FILTER (!BOUND(?PMID))
