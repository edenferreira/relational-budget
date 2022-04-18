(ns edenferreira.clojure.set.extensions
  (:refer-clojure :exclude [extend])
  (:require [clojure.set :as set]))

(defn extend [rel & {:as m}]
  (with-meta
    (set
     (map (fn [r]
            (reduce-kv
             (fn [m k f]
               (assoc m k (f r)))
             r
             m))
          rel))
    (meta rel)))

(defn project-away
  [xrel ks]
  (with-meta (set (map #(apply dissoc % ks) xrel)) (meta xrel)))

(defn summarize [rels projection & {:as m}]
  (with-meta
    (set
     (map (fn [[projected rels]]
            (-> m
                (update-vals (fn [f]
                               (f rels)))
                (->> (merge projected))))
          (set/index rels projection)))
    (meta rels)))

(comment
  )
;; Out of the Tarpit examples

#_(SalesInfo =
             project (extend (RawSales,
                              (areaCode = areaCodeForAddress (address)),
                              (saleSpeed = datesToSpeedBand (dateRegistered,
                                                             decisionDate)),
                              (priceBand = priceBandForPrice (offerPrice))),
                       address agent areaCode saleSpeed priceBand)

             (set/project
              (set/extend2 raw-sales
                           :area-code (comp area-code-for-address :address)
                           :sales-speed (fn [{:keys [data-registered decision-date]}]
                                          (datesToSpeedBad data-registered decision-date))
                           :price-band (comp price-band-for-price :offer-price))
              [:address :agent :area-code :sales-speed :price-band])

             CurrentOffer =
             summarize (Offer,
                        project (Offer, address bidderName bidderAddress),
                        quota (offerDate,1))

             (set/summarize offer
                            [:address :bidder-name :bidder-address]
                            :offer-date (max-attr :offer-date))

             CommissionDue =
             project (summarize (SalesCommissions,
                                 project (SalesCommissions, agent),
                                 totalCommission = sum (commission)),
                                agent totalCommission)

             (set/project
              (set/summarize sales-commision
                             [:agent]
                             :total-commission (sum :commission))
              [:agent :total-commision])

             count (restrict (summarize (Offer,
                                         project (Offer, address bidderName
                                                         bidderAddress),
                                         numberOfOffers = count ())
                                        | numberOfOffers > 10)) == 0
             (count (set/select (comp #(< 10 %) :number-of-offers)
                                (set/summarize offer
                                               [:address :bidder-name :bidder-address]
                                               :number-of-offers count))))
