(ns sexpbot.core
  (:use [sexpbot.plugins.utils]
	[sexpbot.respond]
	[clojure.contrib.str-utils :only [re-split]])
  (:import (org.jibble.pircbot PircBot)))

(def botconfig 
     (ref {:prepend \$
	   :server "irc.freenode.net"
	   :channel "#acidrayne"}))

(defn wall-hack-method [class-name name- params obj & args]
  (-> class-name (.getDeclaredMethod (name name-) (into-array Class params))
    (doto (.setAccessible true))
    (.invoke obj (into-array Object args))))

(defn split-args [s] (let [[command & args] (re-split #" " s)]
		       {:command command
			:args args}))

(defn make-bot [] 
  (let [bot (proxy [PircBot] []
	      (onMessage 
	       [chan send login host mess]
	       (if (= (first mess) (@botconfig :prepend))
		 (respond (split-args (apply str (rest mess))) this send chan login host))))]
    (wall-hack-method PircBot :setName [String] bot "sexpbot")
    (doto bot
      (.setVerbose true)
      (.connect "irc.freenode.net")
      (.joinChannel "#()"))
    (dosync (alter botconfig merge{:bot bot}))))

(make-bot)