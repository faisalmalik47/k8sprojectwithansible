node {
    stage("Git checkout") { 
        git branch: 'main', changelog: false, url: 'https://github.com/faisalmalik47/k8sprojectwithansible.git'
    }
    stage("Pushing the Dockerfile to the ansible server"){
        sshagent(credentials : ['Ansible']) {
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11'
            sh 'scp /var/lib/jenkins/workspace/kubernetes-demo/* ubuntu@10.0.0.11:/home/ubuntu'
    }
    }
    stage("Docker Image Building") {
        sshagent(credentials : ['Ansible']) {
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 cd /home/ubuntu/'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 docker build -t $JOB_NAME:v1.$BUILD_ID .'
        }
    }
    stage("Docker Image Tagging") {
        sshagent(credentials : ['Ansible']) {
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 cd /home/ubuntu/'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 docker image tag $JOB_NAME:v1.$BUILD_ID philic/$JOB_NAME:v1.$BUILD_ID'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 docker image tag $JOB_NAME:v1.$BUILD_ID philic/$JOB_NAME:latest'
        }
    }
    stage("Pushing image to dockerhub"){
        sshagent(credentials : ['Ansible']) {
            withCredentials([string(credentialsId: 'docker_pass', variable: 'docker_pass')]) {
                sh "ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 docker login -u philic -p ${docker_pass}"
                sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 docker image push philic/$JOB_NAME:v1.$BUILD_ID'
                sh 'ssh -o StrictHostKeyChecking=no ubuntu@10.0.0.11 docker image push philic/$JOB_NAME:latest'
            }
        }
    }

}
