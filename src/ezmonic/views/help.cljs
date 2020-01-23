(ns ezmonic.views.help
  (:require [ezmonic.db :as db]
            [ezmonic.style :as style]
            [ezmonic.views.shared
             :refer [div center-quote]
             :as shared]
            ["react-native"
             :refer [View
                     Text
                     TouchableHighlight]
             :as rn]
            ["react-navigation" :as react-navigation]
            ["react-navigation-stack" :as react-navigation-stack]
            [re-frame.core :as rf]
            [reagent.core :as rg])
  (:require-macros [ezmonic.util :refer [defnav]]))

(def consonants
  ["S, Z, soft C"
   "T, D"
   "N"
   "M"
   "R"
   "L"
   "J, SH, soft G"
   "K, NG"
   "F, V"
   "B, P"])

(def numbers (vec (map str (range 10))))
(def descriptions
  ["Ace or Zoo"
   "Tea or Doe"
   "Nay or Knee"
   "Mow or Aim"
   "Row or Ear"
   "Lay or Eel"
   "Joy, Edge, or Ash"
   "Key, Quay or Hang"
   "Foe or Halve"
   "Bay or Ape"])

(defn row
  [number consonant description bg-color]
  [:> View {:style {:display "flex"
                    :flexDirection :row
                    :justify-content "space-between"
                    :backgroundColor bg-color}}
   [:> Text {:style {:flex-grow 1
                     :flex-basis 0}} number]
   [:> Text {:style {:flex-grow 1
                     :flex-basis 0}} consonant]
   [:> Text {:style {:flex-grow 1
                     :flex-basis 0}} description]])

(defnav help
  []
  [shared/safe-scroll-wrapper
   [:> View
    [:> Text {:style style/heading} "Quickref"]
    [:> Text "The following table is a quick overview for how to convert consonant sounds to numbers."]
    [:> View
     (for [[number consonant description bg-color]
           (map vector numbers consonants descriptions (cycle ["#D3D3D3" "white"]))]
       ^{:key number} [row number consonant description bg-color])]
    [:> Text {:style style/heading} "How to use this app"]
    [div "Humans are really bad at remembering numbers."]
    [div "But we are really good at stories."]
    [div "We can trick our brains into remembering big numbers by breaking down the numbers into words and then using those words to create a story."]
    [div "For example: how easily can you remember the number Pi to 11 digits?"]
    [center-quote "3.1415926535"]
    [div "Now close your eyes and try to repeat that."]
    [div "Not too easy, right?"]
    [div "What about this. How easily can you remember the following story?"]
    [center-quote "A METEOR landed on and killed my favorite TULIP, then an ANGEL sent me an EMAIL saying the tulip was in heaven."]
    [div "METEOR, TULIP, ANGEL, EMAIL"]
    [div "You can probably close your eyes and repeat that back after reading it just a single time."]
    [div "Well, as you probably guessed, you just remembered Pi to 11 digits. All you need to do is use the table above to convert the key words back into numbers."]
    [:> Text {:style style/heading} "Step by step"]
    [div "Each consonant sound can be translated to a number using the above table."]
    [div "Note! We strictly care about consonant sounds (and only some of them)."]
    [div "Vowels sounds are ignored, as well as some other sounds like H and W."]
    [div "And we only care about the sound! So a word like RACE with a soft C translates differently than CAKE with a hard C."]
    [div "Here's how you would translate that METEOR story back into Pi."]
    [div "The M in METEOR translates to 3. Just look at the table."]
    [div "The T translates to 1, the R translates to 4."]
    [div "The T, L, and P in TULIP translate to 1, 5, and 9."]
    [div "Even though the NG is listed as a translation to 7 in the table, remember that we care about SOUNDS, and the sound of NG in ANGEL is an N sound followed by a soft G sound. Therefore, ANGEL translates to 2, 6, 5"]
    [div "And we're back to an easy one with EMAIL. It's simply 3, 5."]
    [div "All together now:"]
    [center-quote "METEOR (314) TULIP (159)"]
    [center-quote "ANGEL (265) EMAIL (35)"]
    [:> Text {:style style/heading} "Next steps"]
    [div "At first, you'll probably find yourself often referring to the table. But you'll have the sound/number translations down in no time and soon you'll be making mnemonics without even taking out your phone and opening the app!"]]]
  

  (fn [{:keys [navigation]} props]
    (clj->js
     {:title "help"
      :headerStyle style/header})))

(def help-stack
  (let [stack (. react-navigation-stack createStackNavigator
                 #js {:help-home help})]
    (doto stack
      (goog.object/set "navigationOptions" #js {:tabBarLabel "Help"}))))
