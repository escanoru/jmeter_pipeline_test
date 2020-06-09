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
		defaultValue: 'ip:32092,ip:32092',
		description: '<h4>Kafka Broker nodes, for this case the port 32092 mus be used</h4>'
		)
        string(
		name: 'Zookeeper_Nodes', 
		defaultValue: 'ip:2181,ip:2181', 
		description: '<h4>Zookeeper_Nodes</h4>'
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
		description: '<h4>Each instance generate around 25k (This depens on the host where the task is executed)</h4>'
		)	
    }
	
    stages {
        stage('Setting Parameters') {
            steps {
              sed -i \'s/BROKER_NODES/${params.Broker_Nodes}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
			  sed -i \'s/ZOOKEEPER_NODES/${params.Zookeeper_Nodes}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
		      sed -i \'s/TOPIC_NAME/${params.Topic}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
				
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