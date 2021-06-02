(ns game.core
)
;;start data { {:name :color} ...}
;;sample data
(def players #{ {:name "Maurice", :color :blue} 
               {:name "jeannette", :color :green} 
})


;;Game State targetted: {
;;turn int current turn
;;turnseq (:color ...) all colors once
;;players {:color { :name  :cell :lastroll  :wait } ...}
;;cells  { cellKey  FncForThisCell ...} map cell with fnc that will be triggered when entering this cell
;;}
(declare InitPlayers)
(declare InitTurnSeq)
(declare InitCells)
(defn InitGameState [players]
    {
     :turn 0
     :players (InitPlayers players)
     :turnseq (InitTurnSeq players)
     :cells (InitCells)
   }
)

(defn InitPlayers [players] 
(into {} (map #(into {} { (:color %) { 
                 :name (:name %)
                 :cell 0
                 :lastRoll [0 0]
                 :wait 0 }}) ;-1 means waiting for another players
      players))  
)

(declare GetColors)
(defn InitTurnSeq [players] ( GetColors players ))

(declare Nothing)
(declare FirstDoubleMove)
(declare DoubleMove)
(declare SkipTurn)
(declare Jail)
(declare Goto)

(defn InitCells [] 
{
 0  Nothing ; used for every other cells
 9  FirstDoubleMove
 18 DoubleMove
 19 (partial SkipTurn 2)
 27 DoubleMove
 31 Jail
 36 DoubleMove
 42 (partial Goto 30)
 45 DoubleMove
 52 Jail
 54 DoubleMove
 58 (partial Goto 0)
 ;;63 Win
}
)

;;not pure function
(defn RollDice [] (inc (rand-int 6)))
;;not pure function
(defn Roll [] [(RollDice) (RollDice)])


;;utility functions

(defn GetColors [players] (map :color players))

(defn NbPlayer [gs] (count (:turnseq gs)))


(defn GetActiveColor [gs] (nth (:turnseq gs) (mod (:turn gs) (NbPlayer gs) ) ))

(defn GetActivePlayer [gs] ((GetActiveColor gs) (:players gs)))

(defn GetActivePlayerCell [gs] ((GetActivePlayer gs) :cell))

(defn GetActivePlayerRoll [gs] ((GetActivePlayer gs) :lastRoll))

(defn GetActivePlayerWait [gs] ((GetActivePlayer gs) :wait))

(defn GetPreviousColor [gs] (nth (:turnseq gs) (mod (- (:turn gs) 1) (NbPlayer gs) ) ))

(defn GetPreviousPlayer [gs] ((GetPreviousColor gs) (:players gs)))

(defn GetPreviousPlayerRoll [gs] ((GetPreviousPlayer gs) :lastRoll))

(defn GetPreviousPlayerCell [gs] ((GetPreviousPlayer gs) :cell))

(defn GetCellFn [gs cell] ( (gs :cells) cell ))

(defn GetActivePlayerCellFn [gs] 
  ( let [f (GetCellFn gs (GetActivePlayerCell gs))] 
    (if (nil? f) (GetCellFn gs 0) f)
  )
)

(defn TriggerActivePlayerCell [gs] ((GetActivePlayerCellFn gs) gs))

;;cells specials

(defn Nothing [gs] gs)

(defn FirstDoubleMove [gs]
  (if (< (gs :turn) (NbPlayer gs))
    (let [color (GetActiveColor gs) roll (GetActivePlayerRoll gs)]
        (case roll 
          [6 3] (update-in gs [:players color]  assoc :cell 26 )
          [3 6] (update-in gs [:players color]  assoc :cell 26 )
          [4 5] (update-in gs [:players color] assoc  :cell 53 )
          [5 4] (update-in gs [:players color] assoc  :cell 53 )
          gs
        )
    )
    (DoubleMove gs)
  )
)

(defn DoubleMove [gs]
  (let [color (GetActiveColor gs) dices (reduce + (GetActivePlayerRoll gs))]
    (-> gs
      (update-in  [:players color :cell ] + dices )
      (TriggerActivePlayerCell)
    )
  )
)

(defn Goto [n gs]
  (let [color (GetActiveColor gs) ]
      (update-in gs [:players color] assoc :cell n )
  )
)

(defn SkipTurn [n gs]  
  (let [color (GetActiveColor gs) ]
    (if (< n 0)
      (update-in gs [:players color] assoc :wait n )
      (update-in gs [:players color :wait] + n )
    )
  )
)

(defn Jail [gs] (SkipTurn -1 gs))


(defn FindPlayerColorOnCell [gs cell] 
  (first (first (filter #(= ((get % 1) :cell) cell) (gs :players))))
)


(defn ComputeNextGameState [gs rolls] 
  (let [color (GetActiveColor gs) 
       dices (reduce + rolls)
       wait (GetActivePlayerWait gs)
       srccell (get-in gs [:players color :cell]) 
       tgtcell (+ srccell dices) 
       tgtcell (if (> tgtcell 63) (- 63 (- tgtcell 63 )) tgtcell)
       tgtcolor (FindPlayerColorOnCell gs tgtcell)
       winner (FindPlayerColorOnCell gs 63)  ]
       (if (nil? winner)
          (cond-> gs 
            ;;swap existing player on target cell if exist
            (and ( = wait 0)  (some? tgtcolor) ) ( ->
                    (update-in [:players tgtcolor ] assoc :cell srccell )
                    (update-in [:players tgtcolor ] assoc :wait 0 )
            )
            ( = wait 0) (->
                  (update-in [:players color] assoc :cell tgtcell  )
                  (update-in [:players color] assoc :lastRoll rolls )
                  (TriggerActivePlayerCell )
            )
            (> wait 0) (
              update-in [:players color :wait ] dec
            )
            true (update :turn inc)
          )
          gs ;;do nothing if we have a winner
       )
  ) 
)


