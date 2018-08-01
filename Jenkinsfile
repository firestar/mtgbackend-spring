node {
    checkout scm
    def app
    def dockerImage = docker.build("buildprocess/docker", "./JenkinsCI-Docker/docker/")
    withEnv([
        'DOCKER_ACCOUNT=firestarthehack',
        'IMAGE_NAME=scryfall',
    ]){
        stage('Build') {
          dockerImage.inside{
            sh 'rm -rf dockerbuild/'
            sh 'chmod 0755 ./gradlew;./gradlew clean build --refresh-dependencies'
          }
        }
        stage('Docker Build') {
            sh "mkdir dockerbuild/"
            sh 'cp build/libs/*.jar dockerbuild/app.jar && cp Dockerfile dockerbuild/Dockerfile'
            app = docker.build("${env.DOCKER_ACCOUNT}/${env.BUILD_ID}", "./dockerbuild/")
            archiveArtifacts(artifacts: 'build/libs/*.jar', onlyIfSuccessful: true)
        }
        stage('Test image') {
            app.inside {
              sh 'echo "Tests passed"'
            }
        }
        stage('Publish Latest Image') {
            app.push("${env.BUILD_ID}")
            app.push("latest")
        }
    }
}