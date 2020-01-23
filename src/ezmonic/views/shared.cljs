(ns ezmonic.views.shared
  (:require [ezmonic.style :as style]
            [ezmonic.db :as db]
            [reagent.core :as rg]
            [re-frame.core :as rf]
            [clojure.string :as string]
            ["react-native-modal-selector"
             :as rn-modal-selector
             :default ModalSelector]
            ["react-native"
             :refer [View
                     Text
                     TouchableHighlight]
             :as rn]))

(def PickerItem (.. rn -Picker -Item))

(defn div
  [text]
  [:> View
   {:style {:margin-bottom 5}}
   [:> Text text]])

(defn center-quote
  [text]
  [:> View
   {:style {:display "flex"
            :align-items "center"}}
   [:> Text {:style {:margin-right 40
                     :margin-left 40}} text]])

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

(defn modal-selector
  [picker-idx subel-cursor]
  (fn [picker-idx subel-cursor]
    (let [data (map (fn [word] {:key word :label word})
                    (::db/word-choices @subel-cursor))
          init-value (::db/chosen-word @subel-cursor)
          val (rg/cursor subel-cursor [::db/chosen-word])]
      [:> ModalSelector
       {:selected-key init-value
        :data data
        :on-change (fn [v]
                     (reset! val (.-key v))
                     (rg/flush))}])))

(defn search-url [word]
  (str "https://google.com/search?q=define+" (string/lower-case word)))

(defn native-pickers
  "Display pickers full of mnemonics for a given `number`.

  Uses native picker, which looks fine in Android, but for this
  particular app is not the right fit."
  [elements]
  (into
   [:> rn/View {:style {:flex-direction "row"
                        :flex-wrap "wrap"}}]
   (map-indexed
    (fn [idx element]
      (let [el-cursor (rg/cursor elements [idx])]
        ^{:key idx}
        [:> rn/View {:margin 2}
         [:> rn/View {:display "flex"
                      :flex-direction "row"
                      :justify-content "flex-start"
                      :margin-left "auto"
                      :margin-right "auto"}
          [:> rn/Text
           (::db/number element)]
          [:> TouchableHighlight
           {:on-press
            #(.openURL rn/Linking (search-url (::db/chosen-word @el-cursor)))}
           [:> rn/Text " 🔎"]]]
         [modal-selector idx el-cursor]]))
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

(defn new-mnemonic-form
  [all-possible-mnemonic {:keys [on-save on-delete on-reset]}]
  (let [all-elements-idx (rg/atom 0)
        mnemonic-edition (let [{name ::db/name
                                story ::db/story
                                number ::db/number} @all-possible-mnemonic
                               elements (first (::db/all-possible-elements @all-possible-mnemonic))]
                           (rg/atom {::db/name name
                                     ::db/story story
                                     ::db/number number
                                     ::db/elements elements}))
        name (rg/cursor mnemonic-edition [::db/name])
        story (rg/cursor mnemonic-edition [::db/story])
        max-possible (dec (count (::db/all-possible-elements @all-possible-mnemonic)))]
    (fn [all-possible-mnemonic {:keys [on-save on-delete]}]
      [:> rn/View
       [:> View
        [div (str "You can memorize the number " (::db/number @mnemonic-edition) " with the words:")]
        [center-quote (string/join " " (map ::db/chosen-word (::db/elements @mnemonic-edition)))]
        [div "Use the pickers below to choose words you find memorable. Give the mnemonic a name and write a vivid story to help you remember. Save it for later reference."]
        [div "Don't like the words in the pickers? Use the arrows to change how the number is divided into the pickers."]
        [:> View {:style style/flex-row}
         [:> rn/Button
          {:title "<-"
           :style {:flex-grow 1
                   :flex-basis 0}
           :disabled (= @all-elements-idx 0)
           :on-press (fn []
                       (swap! all-elements-idx #(max 0 (dec %)))
                       (swap! mnemonic-edition
                              #(assoc % ::db/elements
                                      (get-in @all-possible-mnemonic
                                              [::db/all-possible-elements @all-elements-idx])))
                       (rf/dispatch [:switch-elements @all-elements-idx]))}]
         [:> rn/Button
          {:title "<<- 10"
           :style {:flex-grow 1
                   :flex-basis 0}
           :disabled (= @all-elements-idx 0)
           :on-press (fn []
                       (swap! all-elements-idx #(max 0 (- % 10)))
                       (swap! mnemonic-edition
                              #(assoc % ::db/elements
                                      (get-in @all-possible-mnemonic
                                              [::db/all-possible-elements @all-elements-idx])))
                       (rf/dispatch [:switch-elements @all-elements-idx]))}]
         [:> rn/View
          [:> rn/Text "Option: " (inc @all-elements-idx) " of " (inc max-possible)]]
         [:> rn/Button
          {:title "10 ->>"
           :style {:flex-grow 1
                   :flex-basis 0}
           :disabled (= @all-elements-idx max-possible)
           :on-press (fn []
                       (swap! all-elements-idx #(min max-possible (+ % 10)))
                       (swap! mnemonic-edition
                              #(assoc % ::db/elements
                                      (get-in @all-possible-mnemonic
                                              [::db/all-possible-elements @all-elements-idx])))
                       (rf/dispatch [:switch-elements @all-elements-idx]))}]
         [:> rn/Button
          {:title "->"
           :style {:flex-grow 1
                   :flex-basis 0}
           :disabled (= (+ 1 @all-elements-idx)
                        (count (::db/all-possible-elements @all-possible-mnemonic)))
           :on-press (fn []
                       (swap! all-elements-idx #(min max-possible (inc %)))
                       (swap! mnemonic-edition
                              #(assoc % ::db/elements
                                      (get-in @all-possible-mnemonic
                                              [::db/all-possible-elements @all-elements-idx])))
                       (rf/dispatch [:switch-elements @all-elements-idx]))}]]]
       [:> rn/View
        [:> rn/Text "Number: " (::db/number @mnemonic-edition)]]
       [:> rn/View
        [:> rn/Text "Words: "]
        [native-pickers
         (rg/cursor mnemonic-edition [::db/elements])]]
       [:> rn/View
        [:> rn/Text "Name:"]
        [:> rn/TextInput
         {:value @name
          :style (merge style/text-input
                        {:height 40})
          :placeholder "E.g. Jenny's number"
          :placeholder-text-color "grey"
          :on-change-text (fn [text]
                            (reset! name text)
                            (print @mnemonic-edition)
                            (rg/flush))}]]
       [:> rn/View
        [:> Text "Story:"]
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
         {:title "Reset"
          :color "red"
          :on-press on-reset}]
        [:> rn/Button
         {:title "Save"
          :disabled (empty? @name)
          :on-press (fn []
                      (if on-save
                        (on-save @mnemonic-edition))
                      (rf/dispatch [:save-mnemonic @mnemonic-edition]))}]]])))

(defn mnemonic-form
  [mnemonic all-possible-mnemonic {:keys [on-save on-delete on-reset]}]
  (let [all-elements-idx (rg/atom 0)
        mnemonic-edition (rg/atom @mnemonic)
        name (rg/cursor mnemonic-edition [::db/name])
        story (rg/cursor mnemonic-edition [::db/story])]
    (fn [mnemonic all-possible-mnemonic {:keys [on-save on-delete]}]
      [:> rn/View
       (if all-possible-mnemonic ;; We are in the creation area
         [:> View
          [div (str "You can memorize the number " (::db/number @mnemonic) " with the words:")]
          [center-quote (string/join " " (map ::db/chosen-word (::db/elements @mnemonic-edition)))]
          [div "Use the pickers below to choose words you find memorable. Give the mnemonic a name and write a vivid story to help you remember. Save it for later reference."]
          [div "Don't like the words in the pickers? Use the arrows to change how the number is divided into the pickers."]
          [:> View {:style style/flex-row}
           [:> rn/Button
            {:title "<-"
             :style {:flex-grow 1
                     :flex-basis 0}
             :disabled (= @all-elements-idx 0)
             :on-press (fn []
                         (swap! all-elements-idx dec)
                         (swap! mnemonic-edition
                                #(assoc % ::db/elements
                                        (get-in all-possible-mnemonic
                                                [::db/all-possible-elements @all-elements-idx])))
                         (rf/dispatch [:switch-elements @all-elements-idx]))}]
           [:> rn/Button
            {:title "->"
             :style {:flex-grow 1
                     :flex-basis 0}
             :disabled (= (+ 1 @all-elements-idx)
                          (count (::db/all-possible-elements all-possible-mnemonic)))
             :on-press (fn []
                         (swap! all-elements-idx inc)
                         (swap! mnemonic-edition
                                #(assoc % ::db/elements
                                        (get-in all-possible-mnemonic
                                                [::db/all-possible-elements @all-elements-idx])))
                         (rf/dispatch [:switch-elements @all-elements-idx]))}]]])
       [:> rn/View
        [:> rn/Text "Number: " (::db/number @mnemonic)]]
       [:> rn/View
        [:> rn/Text "Words: "]
        [native-pickers
         (rg/cursor mnemonic-edition [::db/elements])]]
       [:> rn/View
        [:> rn/Text "Name:"]
        [:> rn/TextInput
         {:value @name
          :style (merge style/text-input
                        {:height 40})
          :placeholder "E.g. Jenny's number"
          :placeholder-text-color "grey"
          :on-change-text (fn [text]
                            (reset! name text)
                            (print @mnemonic-edition)
                            (rg/flush))}]]
       [:> rn/View
        [:> Text "Story:"]
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
                        (on-delete @mnemonic)
                        (on-reset)))}]
        [:> rn/Button
         {:title "Save"
          :disabled (or
                     (empty? (string/trim @name))
                     (= mnemonic @mnemonic-edition))
          :on-press (fn []
                      (if on-save
                        (on-save @mnemonic-edition))
                      (rf/dispatch [:save-mnemonic @mnemonic-edition]))}]]])))
