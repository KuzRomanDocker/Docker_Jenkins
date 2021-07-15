pipeline {
  agent any
  tools {
        maven 'M3'
    }
  stages {
    stage('Log Jenkins Maven, Git and Java version info') {
    steps {
      sh 'java -version'
      sh 'git --version'
    }
   }
   stage('GitHub Jenkins Maven Build') {
     steps {
       git 'https://github.com/KuzRomanDocker/springbootwebapp.git'
          sh 'mvn -B -DskipTests clean package'
      }
    }
  stage('Archive the Artifact') {
    steps {
      archiveArtifacts artifacts: 'target/**/*.jar', followSymlinks: false
      }
    }
  stage('Copy Artifact to another job') {
    steps {
      build job: 'CopyAPP'
      }
    }  
  }
}
