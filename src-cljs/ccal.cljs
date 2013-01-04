(ns ccal
  (:use [jayq.core :only [$ on]]
        [cljs.reader :only [read-string]]))

(let [$cal ($ :#cal)
      w (.width $cal) h (.height $cal)]
  (def size {:w w :h h :w2 (/ w 2) :h2 (/ h 2)}))

(def cal-text (atom
               "{:radius 250
 :rotate -60
 :m-ticks {:line {:in 0.95 :out 1.05 :col \"#888\"}
           :label {:trad 1.03 :trot 272 :theta 4 :tcol \"#00A\"}}
 :m-evs [[1 \"Jan\"] [32 \"Feb\"] [60 \"Mar\"]
         [91 \"Apr\"] [121 \"May\"] [152 \"Jun\"]
         [182 \"July\"] [213 \"Aug\"] [244 \"Sept\"]
         [274 \"Oct\"] [305 \"Nov\"] [335 \"Dec\"]]
 :w-ticks {:line {:in 0.99 :out 1.01 :col \"#444\"}}}"))

(def evs-text (atom "{:today {:line {:in 0 :out 0.5 :col \"#FAA\"} :label {:text \"\"}}\n :default {:line {:in 0.8 :out 1 :col \"#080\"}\n           :label {:text \"???\" :tsize 1 :tcol \"#000\" :trad 0.85 :theta 2}}\n 1   [{:label {:text \"NYD10k!\" :tcol \"#800\"}}]\n 13  [{:label {:text \"Rough 'n' Tumble Ten\" :tcol \"#800\" :trad 0.7}}]\n 34  [{:label {:text \"Haglof Open 5\" :trad 0.8}}\n      {:label {:text \"C\" :tcol \"#800\" :trot -90 :trad 1.12 :tsize 1.1 :theta 0}\n       :line {:in 1.05 :out 1.1 :col \"#800\"}}]\n 54  [{:label {:text \"☸ Gospel Pass\"}}\n      {:label {:text \"C\" :tcol \"#800\" :trot -90 :trad 1.12 :tsize 1.1 :theta 0}\n       :line {:in 1.05 :out 1.1 :col \"#800\"}}]\n 69  [{:label {:text \"Grizzly\"}}]\n 76  [{:label {:text \"☸ Lionheart\"}}\n      {:label {:text \"B\" :tcol \"#C00\" :trot -90 :trad 1.13 :tsize 1.5 :theta 0}\n       :line {:in 1.05 :out 1.1 :col \"#800\"}}]\n 96  [{:label {:text \"Bath Beat\" :theta -2}}]\n 97  [{:label {:text \"Exe to Axe\"}}]\n 117 [{:label {:text \"Three Peaks Race\" :trad 0.78}}\n      {:label {:text \"B\" :tcol \"#C00\" :trot -90 :trad 1.13 :tsize 1.5 :theta 0}\n       :line {:in 1.05 :out 1.1 :col \"#800\"}}]\n 132 [{:label {:text \"☸ Fred Whitton Challenge\" :trad 0.7}}]\n 152 [{:label {:text \"Hot Chilli\\nEndurance Weekend\" :theta 3 :trad 0.7}}]\n 187 [{:label {:text \"CELTMAN!\" :tcol \"#A00\" :tsize 1.5 :trad 0.8}}\n      {:label {:text \"A\" :tcol \"#F00\" :trot -90 :trad 1.13 :tsize 2 :theta 0}\n       :line {:in 1.05 :out 1.1 :col \"#800\"}}]\n 214 [{:label {:text \"Grim Reaper\" :tcol \"#A00\" :tsize 1.5 :trad 0.79}}]\n 251 [{:label {:text \"IM WALES\" :tcol \"#A00\" :tsize 1.5 :trad 0.79}}]}\n"))

(comment
  (def evs-text (atom "{:default {:line {:in 0.8 :out 1 :col \"#080\"}
           :label {:text \"???\" :tsize 1 :tcol \"#000\" :trad 0.85 :theta 2}}
 1  [{:label {:text \"NYD10k!\" :tcol \"#800\"}
      :line {:col \"#0AA\"}}]
 13 [{:label {:text \"Rough 'n' Tumble Ten\" :tcol \"#800\" :trad 0.7 :theta 2}}
     {:label {:text \"A\" :tcol \"#F00\" :trot -90 :trad 1.15 :theta 0}
      :line {:in 1 :out 1.1 :col \"#F00\"}}]}")))

(def cal (atom (read-string @cal-text)))
(def evs (atom (read-string @evs-text)))

(def $c-text ($ :#c-text))
(def $e-text ($ :#e-text))
(def $c-wrap ($ :#c-wrap))
(def $e-wrap ($ :#e-wrap))

(def $c-edit (.fromTextArea js/CodeMirror (.get $c-text 0)))
(def $e-edit (.fromTextArea js/CodeMirror (.get $e-text 0)))
(.hide $c-wrap)

(def $raf (js/Raphael "cal" (size :w) (size :h)))

(defn spit-cal []
  (.setValue $c-edit @cal-text))

(defn spit-evs []
  (.setValue $e-edit @evs-text))

(defn line [$r x1 y1 x2 y2]
  (.path $r (clj->js ["M" x1 y1 "L" x2 y2])))

(defn rotate
  ([$e deg]
     (.transform $e (clj->js ["...R" deg])))
  ([$e deg [cx cy]]
     (.transform $e (clj->js ["...R" deg cx cy]))))

(defn translate [$e x y]
  (.transform $e (clj->js ["...T" x y])))

(defn scale [$e s]
  (.transform $e (clj->js ["...S" s])))

(defn draw-ev [{{:keys [in out col]} :line
                {:keys [text trad trot theta tcol tsize]} :label}
               deg]

  (let [{:keys [w2 h2]} size
        r #(- w2 (* % (@cal :radius)))
        s (.set $raf)]

    (.push s
           (-> (line $raf (r in) h2 (r out) h2)
               (.attr "stroke" col)))
    (when text
      (.push s
             (-> (.text $raf (r trad) h2 text)
                 (rotate theta [w2 h2])
                 (rotate trot)
                 (scale tsize)
                 (.attr "fill" tcol))))

    (rotate s deg [w2 h2])))

(defn day2deg [day]
  (+ (@cal :rotate) (/ (* day 360) 365) -1))

(defn draw-cal []

  (let [{:keys [w2 h2]} size
        r (@cal :radius)]

    (-> (.circle $raf w2 h2 r)
        (.attr "stroke" "#EEE")
        (.attr "stroke-width" 2))

    (-> (.text $raf w2 h2 "2013")
        (.attr "fill" "#f00")
        (scale 2))

    (doseq [x (range 12)]
      (draw-ev (assoc-in (@cal :m-ticks) [:label :text] (get-in @cal [:m-evs x 1]))
               (day2deg (get-in @cal [:m-evs x 0]))))

    (doseq [x (range 52)]
      (draw-ev (@cal :w-ticks) (day2deg (* 7 x))))))

(defn draw-evs []
  (let [{:keys [w2 h2]} size
        defs (@evs :default)]

    (draw-ev {:line (merge (defs :line) (get-in @evs [:today :line]))
              :label (merge (defs :label) (get-in @evs [:today :label]))}
             (day2deg (.getDay (js/Date.))))

    (doseq [[k vs] @evs :when (number? k)
            v vs]
      (draw-ev {:line (merge (defs :line) (v :line))
                :label (merge (defs :label) (v :label))}
               (day2deg k)))))

(defn draw-both []
  (.clear $raf)
  (draw-cal)
  (draw-evs))

(defn slurp-cal []
  (try
    (let [u-cal-text (.getValue $c-edit)
          u-cal (read-string u-cal-text)]
      (reset! cal-text u-cal-text)
      (reset! cal u-cal)
      (spit-cal))
    (catch js/Object e
        (js/alert e))))

(defn slurp-evs []
  (try
    (let [u-evs-text (.getValue $e-edit)
          u-evs (read-string u-evs-text)]
      (reset! evs-text u-evs-text)
      (reset! evs u-evs)
      (spit-evs))
   (catch js/Object e
      (js/alert e))))

(defn save-both []
  (.setItem js/localStorage "cal" (.getValue $c-edit))
  (.setItem js/localStorage "evs" (.getValue $e-edit)))

(defn render []
      (slurp-cal)
      (slurp-evs)
      (save-both)
      (draw-both))

(on ($ :#render) :click render)

(on ($ :#toggle) :click
    (fn [e]
      (.toggle $e-wrap)
      (.toggle $c-wrap)))

(if-let [stored-cal (.getItem js/localStorage "cal")]
  (.setValue $c-edit stored-cal)
  (spit-cal))

(if-let [stored-evs (.getItem js/localStorage "evs")]
  (.setValue $e-edit stored-evs)
  (spit-evs))

(render)
