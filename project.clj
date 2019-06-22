(defproject score-date "0.3.3-SNAPSHOT"
  :description "score date"
  :url "https://sourceforge.net/projects/scoredate/"
  :dependencies [;;[com.google.guava/guava "14.0.1"]
		         ;;[org.mockito/mockito-all "1.8.4"]
		         ;;[org.apache.logging.log4j/log4j-api "2.0.2"]
		         ;;[org.apache.logging.log4j/log4j-core "2.0.2"]
		         ;;[org.uispec4j/uispec4j "2.4"]
 ;;                [org.assertj/assertj-core "1.2.0"]
;;		         [org.apache.maven/maven-model "3.3.3"]
;;		         [org.apache.maven/maven-core "3.0.5"]
                 ;;[com.sun.activation/javax.activation "1.2.0"]
                 ]

  :main ScoreDate
  :prep-tasks [ "javac" ]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :java-source-paths ["src"]
)
