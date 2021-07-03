pipeline {
    agent {
        node {
            label 'metersphere || master'
        }
    }
    options { quietPeriod(60) }
    environment { 
        IMAGE_NAME = 'metersphere'
        IMAGE_PREFIX = 'swr.cn-east-3.myhuaweicloud.com/docker-work-test/metersphere'
    }
    stages {
        stage('Build/Test') {
            steps {
                sh "export https_proxy=http://proxy.tuhu.work:8001 http_proxy=http://proxy.tuhu.work:8001 all_proxy=socks5://proxy.tuhu.work:8001 && /opt/apache-maven/bin/mvn clean package"
            }
        }
        stage('Docker build & push') {
            steps {
                sh "docker images | grep \<none\> | awk '{print $3}' | xargs docker rmi -f"
                sh "docker build --build-arg MS_VERSION=\${TAG_NAME:-\$BRANCH_NAME}-\${GIT_COMMIT:0:8} -t ${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} ."
                sh "docker tag ${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME} ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME}"
                sh "docker login -u cn-east-3@9AZQH7VWBHALIFXLCC7K -p 555ab5c008a6a191bcb1ca69a466412e81f1285defe5e279aa28f37a7b8ff72b swr.cn-east-3.myhuaweicloud.com && \
                        docker push ${IMAGE_PREFIX}/${IMAGE_NAME}:\${TAG_NAME:-\$BRANCH_NAME}"
            }
        }
    }
    post('Notification') {
        always {
            sh "echo \$WEBHOOK\n"
            // withCredentials([string(credentialsId: 'wechat-bot-webhook', variable: 'WEBHOOK')]) {
            //     qyWechatNotification failSend: true, mentionedId: '', mentionedMobile: '', webhookUrl: "$WEBHOOK"
            // }
        }
    }
}
