(ns chronicle.core
  (:import (java.io FileNotFoundException)
           (org.eclipse.jgit.revwalk RevCommit)
           (org.eclipse.jgit.api Git))
  (:require [chronicle.clj-jgit.internal]
            [chronicle.clj-jgit.porcelain]
            [chronicle.clj-jgit.querying]))

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
    (map #(assoc %
           :diff (chronicle.clj-jgit.querying/changed-files-with-patch repo (:raw %))
           :files (get-commit-file-tree repo (:raw %)))
         (get_commits))))