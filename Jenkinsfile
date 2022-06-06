pipeline {
  environment {
    registry = "getafix.nestwave.com:5000/device"
    dockerImage = ''
  }
  agent any
  stages {
    stage('Cloning Git') {
     steps {
       echo '> Checking out the source control ...'
       git 'git@github.com:rc-par001/cloudDevice.git'
     }
   }
    stage ('Build') {
       steps {
           echo '> Building maven project mvn clean install...'
           sh 'mvn -Dmaven.test.failure.ignore=true clean install'
       }
   }
    stage('Test') {
      steps {
        echo '> Process unit tests...'
        sh 'mvn test'
      }
    }
    stage('Building image') {
      steps{
        echo '> Building container image...'
        script {
          dockerImage = docker.build registry + ":$BUILD_NUMBER"
        }
      }
    }
    stage('Deploy Image') {
      steps{
         script {
            dockerImage.push()
        }
      }
    }
    stage('Remove Unused docker image') {
      steps{
        echo '> Remove Unused docker image...'
        sh "docker rmi $registry:$BUILD_NUMBER"
      }
    }
  }
}