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
		ansiColor('gnome-terminal')
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
        string(
		name: 'Threads', 
		defaultValue: '1', 
		description: '<h4>1 threads generates around 10k/h4>'
		)			
    }
	
    stages {
        stage('Setting Broker Nodes') {
            steps {
                sh '''
				 sed -i \'s/BROKER_NODES/${Broker_Nodes}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
				'''
            }
        }
        stage('Setting Zookeeper Nodes') {
            steps {
                sh '''
				 sed -i \'s/ZOOKEEPER_NODES/${Zookeeper_Nodes}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
				'''
            }
        }
        stage('Setting Topic') {
            steps {
                sh '''
				 sed -i \'s/TOPIC_NAME/${Topic}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
				'''
            }
        }
        stage('Setting Threads') {
            steps {
                sh '''
				 sed -i \'s/THREAD_NUM/${Duration}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
				'''
            }
        }		
        stage('Setting Test Duration') {
            steps {
                sh '''
				 sed -i \'s/TIMER_S/${Duration}/\' ${WORKSPACE}/jmeter_kafka_template_single_1.8_KB_message.jmx
				'''
            }
        }		
        stage('Executing Jmeter Test') {
            steps {
                sh '''
				 time /opt/jmeter/bin/jmeter.sh -n -t /opt/jmeter/jmeter_kafka_files/TH_APP_th-cef_1.7_9092_3.2_GZIP_TIME.jmx
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