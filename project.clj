(defproject chronicle "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  ;:plugins [[lein-git-deps "0.0.1-SNAPSHOT"]]

  ;:git-dependencies [["https://github.com/clj-jgit/clj-jgit.git"]]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-jgit "0.7.2"]
                 [org.jeromq/jeromq "0.3.0-SNAPSHOT"]
                 [org.zeromq/cljzmq "0.1.4" :exclusions [org.zeromq/jzmq]]
                 [org.clojure/data.json "0.2.5"]]


  :repositories [ ["sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"]
                  ["sonatype-snapshots"
                    {:url "https://oss.sonatype.org/content/repositories/snapshots"
                     :update :always}]])
