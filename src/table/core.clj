(ns table.core
  (:use [clojure.string :only [join replace-first]] ))

(def ^:dynamic *style* :plain)
(def styles
  {
   :plain {:top ["+-" "-+-" "-+"], :middle ["+-" "-+-" "-+"] :bottom ["+-" "-+-" "-+"]
           :dash "-" :header-walls ["| " " | " " |"] :body-walls ["| " " | " " |"] }
   :org {:top ["|-" "-+-" "-|"], :middle ["|-" "-+-" "-|"] :bottom ["|-" "-+-" "-|"]
           :dash "-" :header-walls ["| " " | " " |"] :body-walls ["| " " | " " |"] }
   :unicode {:top ["┌─" "─┬─" "─┐"] :middle ["├─" "─┼─" "─┤"] :bottom ["└─" "─┴─" "─┘"]
         :dash "─" :header-walls ["│ " " │ " " │"] :body-walls ["│ " " ╎ " " │"]}
   })

(defn style-for [k] (k (styles *style*)))

(defn render-rows [table]
  (let [
    fields (if (map? (first table)) (distinct (vec (flatten (map keys table)))) (first table))
    headers (map #(if (keyword? %) (name %) (str %)) fields)
    rows (if (map? (first table))
           (map #(map (fn [k] (get % k)) fields) table) (rest table))
    rows (map (fn [row] (map #(if (nil? %) "" (str %)) row)) rows)
    rows (map vec rows)
    widths (map-indexed
             (fn [idx header]
               (apply max (count header) (map #(count (str (nth % idx))) rows)))
             headers)
    fmt-row (fn [row]
              (map-indexed
                (fn [idx val] (format (str "%-" (nth widths idx) "s") val))
                row))
    wrap-row (fn [row beg mid end] (str beg (join mid row) end))
    headers (fmt-row headers)
    border-for (fn [section]
                 (apply wrap-row
                   (map #(apply str (repeat (.length (str %)) (style-for :dash))) headers)
                   (style-for section)))
    header (apply wrap-row headers (style-for :header-walls))
    body (map #(apply wrap-row (fmt-row %) (style-for :body-walls)) rows) ]

    (concat [(border-for :top) header (border-for :middle)]
            body [( border-for :bottom)])))

(defn table-str [ args & {:keys [style] :as options :or {style :plain}}]
  (binding [*style* style] (apply str (join "\n" (render-rows args)))))

(defn table [& args]
  (println (apply table-str args)))
