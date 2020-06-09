def setDescription() { 
  def item = Jenkins.instance.getItemByFullName(env.JOB_NAME) 
  item.setDescription("Jmeter test") 
  item.save()
  }
setDescription()

// Declarative //
pipeline {
	agent { label 'jmeter_slave' }
	options {
		ansiColor('xterm')
		buildDiscarder(logRotator(daysToKeepStr: '180'))
		}
  parameters {
        string(
		name: 'Broker_Nodes',
		defaultValue: '15.214.',
		description: '<h4>Kafka Broker nodes ip separated by comma, e.g 15.214.x.x, 15.214.x.x, 15.214.x.x, 15.214.x.x</h4>'
		)
        string(
		name: 'Zookeeper_Nodes', 
		defaultValue: '15.214.', 
		description: '<h4>Kafka Broker nodes ip separated by comma, e.g 15.214.x.x, 15.214.x.x, 15.214.x.x, 15.214.x.x</h4>'
		)
        string(
		name: 'Topic', 
		defaultValue: 'th-cef', 
		description: '<h4>Target topic where the events will be sent</h4>'
		)
        string(
		name: 'Duration', 
		defaultValue: '60', 
		description: '<h4>Duration of test in seconds</h4>'
		)
        choice(
		name: 'Instances',
		choices: ['1', '2', '3', '4'],
		description: '<h4>Instance per node. Each instance generates around 10K (This depends on the node where the task is executed)</h4>'
		)	
    }
	
    stages {
        stage('Setting Parameters') {
            steps {
                sh '''
				  sed -i "s/BROKER_NODES/$(echo "${Broker_Nodes}" | tr "," "\n" | awk '{print $1":32092"}' | paste -sd ",")/" ${WORKSPACE}/in_test.jmx
				  sed -i "s/ZOOKEEPER_NODES/$(echo "${Zookeeper_Nodes}" | tr "," "\n" | awk '{print $1":2181"}' | paste -sd ",")/" ${WORKSPACE}/in_test.jmx
				  sed -i \"s/TOPIC_NAME/${Topic}/\" ${WORKSPACE}/in_test.jmx
				  sed -i \"s/TIMER_S/${Duration}/\" ${WORKSPACE}/in_test.jmx
				'''
            }
        }		
        stage('Executing Jmeter Test') {
            steps {
                sh '''
				 time /opt/jmeter/bin/jmeter.sh -n -t ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
				'''
            }
        }
    }
	
    post {
        always {
            echo 'Clenning up the workspace'
            deleteDir() 
        }
	}	
}