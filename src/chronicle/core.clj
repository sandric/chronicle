(ns chronicle.core
  (:import (java.io FileNotFoundException)
           (org.eclipse.jgit.revwalk RevCommit)
           (org.eclipse.jgit.api Git)
           (java.sql Time))
  (:require [chronicle.clj-jgit.internal]
            [chronicle.clj-jgit.porcelain]
            [chronicle.clj-jgit.querying]
            [zeromq.zmq :as zmq]

            [clojure.data.json :as json]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))


(defn get_repo [repos_forder repo_name repo_url]
  (try
    (def repo (chronicle.clj-jgit.porcelain/load-repo (str repos_forder "/" repo_name)))
    (catch FileNotFoundException e
      (def repo (chronicle.clj-jgit.porcelain/git-clone-full repo_url (str repos_forder "/" repo_name))))
    ))

(defn get_commits []
  (map #(chronicle.clj-jgit.querying/commit-info-without-branches
            repo
            (chronicle.clj-jgit.internal/new-rev-walk repo)
            %)
       (chronicle.clj-jgit.porcelain/git-log repo)))

(defn get-commit-file-tree [^Git repo ^RevCommit rev-commit]
  (let [tree-walk (chronicle.clj-jgit.internal/new-tree-walk repo rev-commit)
        changes (transient [])]
    (while (.next tree-walk)
      (conj! changes (chronicle.clj-jgit.util/normalize-path (.getPathString tree-walk))))
    (persistent! changes)))

(defn get_full_commits_history []
  (reverse
    (map #(dissoc (assoc %
           :diff (chronicle.clj-jgit.querying/changed-files-with-patch repo (:raw %))
           :files (get-commit-file-tree repo (:raw %)))
           :repo :raw :time)
         (get_commits))))

(defn worker []
  (let [context (zmq/zcontext 1)]
    (with-open [receiver (doto (zmq/socket context :pull)
                           (zmq/connect "tcp://192.168.2.1:5557"))
                sender (doto (zmq/socket context :push)
                         (zmq/connect "tcp://192.168.2.1:5558"))]
      (while (not (.. Thread currentThread isInterrupted))
        (let [data (zmq/receive-str receiver)]
          (println data "received query")

          (get_repo "repos" (get (json/read-str data) "repo_name") (get (json/read-str data) "repo_url"))

          (zmq/send-str sender (json/write-str {:message "there is some data I can't resist1..."}))
          (zmq/send-str sender (json/write-str {:message "there is some data I can't resist2..."}))
          (zmq/send-str sender (json/write-str {:message "there is some data I can't resist3..."}))

          (zmq/send-str sender (json/write-str (get_full_commits_history)))
          )))))

(get_repo "repos", "slim", "https://github.com/slim-template/slim.git")
(get_full_commits_history)
