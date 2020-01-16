(ns ezmonic.views.shared
  (:require [ezmonic.style :as style]
            [ezmonic.db :as db]
            [reagent.core :as rg]
            [re-frame.core :as rf]
            [clojure.string :as string]
            ["react-native"
             :refer [View
                     Text
                     TouchableHighlight]
             :as rn]))

(def PickerItem (.. rn -Picker -Item))

(defn safe-scroll-wrapper
  [& children]
  [:> rn/SafeAreaView
   (into
    [:> rn/ScrollView {:style {:margin 10}}]
    children)])

(safe-scroll-wrapper [:foo] [:bar])


(defn foo [& args]
  (reduce + args))

(defn bar
  [& args]
  (into [] args))


(defn edit-bar [text navigation nav-path]
  [:> View style/edit-bar
   [:> Text text]
   [:> TouchableHighlight
    {:on-press (fn []
                 (. navigation navigate "edit"))}
    [:> Text "Edit"]]])


(defn picker
  [picker-idx subel-cursor]
  (let [val (rg/cursor subel-cursor [::db/chosen-word])]
    (fn [picker-idx mnemonic-subelement]
      [:> rn/View 
       [:> rn/Text {:style {:margin-left "auto"
                            :margin-right "auto"}}
        (::db/number mnemonic-subelement)]]
      (into
       [:> rn/Picker {:style {:width 170}
                      :item-style {:font-size 10}
                      :selectedValue @val
                      :onValueChange (fn [v]
                                       (reset! val v)
                                       (rg/flush))
                      :enabled true}]
       (map-indexed
        (fn [idx word] 
          ^{:key idx} [:> PickerItem {:label word
                                      :value word}])
        (::db/word-choices @subel-cursor))))))

(defn native-pickers
  "Display pickers full of mnemonics for a given `number`.

  Uses native picker, which looks fine in Android, but for this
  particular app is not the right fit."
  [elements]
  (into
   [:> rn/View {:style {:flex-direction "row"
                        :flex-wrap "wrap"
                        :justify-content "space-between"}}]
   (map-indexed
    (fn [idx element]
      ^{:key idx}
      [:> rn/View
       [:> rn/Text {:style {:margin-left "auto"
                            :margin-right "auto"}}
        (::db/number element)]
       [picker idx (rg/cursor elements [idx])]])
    @elements)))

(defn text-input
  [{:keys [initial-val on-submit]}]
  (let [val (atom initial-val)]
    (fn [initial-val]
      [:> rn/View
       [:> rn/TextInput
        {:style style/text-input
         :value @val
         :text-align-vertical "top"
         :multiline true
         :number-of-lines 5
         :placeholder "E.g. 3.14159265 -> A METEOR (3.14) landed on and killed my favorite TULIP (159). It's now an ANGEL (265) in the sky."
         :on-change-text (fn [text]
                           (reset! val text)
                           (rg/flush))
         :on-submit-editing #(on-submit @val)}]])))


(defn save-button [{:keys [on-save] :or {on-save identity}}]
  [:> rn/Button
   {:title "Save"
    :style {:flex 1}
    :on-press on-save}])

(defn delete-button [{:keys [on-delete] :or {on-save identity}}]
  [:> rn/Button
   {:title "Delete"
    :style {:flex 1}
    :color "red"
    :on-press on-delete}])

(defn clear-button [{:keys [on-clear] :or {on-clear identity}}]
  [:> rn/Button
   {:title "Delete"
    :style {:flex 1}
    :color "red"
    :on-press on-clear}])

(defn mnemonic-form
  [mnemonic {:keys [on-save on-delete on-reset]}]
  (let [mnemonic-edition (rg/atom mnemonic)
        name (rg/cursor mnemonic-edition [::db/name])
        story (rg/cursor mnemonic-edition [::db/story])]
    (fn [mnemonic {:keys [on-save on-delete]}]
      [:> rn/View
       [:> rn/View
        [:> rn/Text "Number: " (::db/number @mnemonic-edition)]]
       [:> rn/View
        [:> rn/Text "Words: "]
        [native-pickers (rg/cursor mnemonic-edition [::db/elements])]]
       [:> rn/View
        [:> rn/Text "Name"]
        [:> rn/TextInput
         {:value @name
          :style style/text-input
          :placeholder "E.g. Jenny's number"
          :on-change-text (fn [text]
                            (reset! name text)
                            (rg/flush))}]]
       [:> rn/View
        [:> Text
         "Write a sentence or story that uses those words."
         " Save it for later reference."]
        [:> rn/TextInput
         {:value @story
          :style style/text-input
          :text-align-vertical "top"
          :multiline true
          :number-of-lines 5
          :placeholder "E.g. 3.14159265 -> A METEOR (3.14) landed on and killed my favorite TULIP (159). It's now an ANGEL (265) in the sky."
          :on-change-text (fn [text]
                            (reset! story text)
                            (rg/flush))}]]
       [:> rn/View
        {:style {:display "flex"
                 :padding-top 2
                 :flex-direction "row"
                 :justify-content "space-between"}}
        [:> rn/Button
         {:title (if on-delete "Delete" "Reset")
          :color "red"
          :on-press (fn []
                      (if on-delete
                        (on-delete mnemonic)
                        (on-reset mnemonic)))}]
        [:> rn/Button
         {:title "Save"
          :disabled (or
                     (empty? (string/trim @name))
                     (= mnemonic @mnemonic-edition))
          :on-press (fn []
                      (if on-save
                        (on-save @mnemonic-edition))
                      (rf/dispatch [:save-mnemonic @mnemonic-edition]))}]]])))
