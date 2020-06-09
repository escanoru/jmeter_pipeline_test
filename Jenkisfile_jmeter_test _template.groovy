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
		description: '<h4>Kafka Broker nodes/h4>'
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
    }
	
    stages {
        stage('Executing Jmeter Test') {
            steps {
                sh '''
				 time /opt/jmeter/bin/jmeter.sh -n -t /opt/jmeter/jmeter_kafka_files/TH_APP_th-cef_1.7_9092_3.2_GZIP_TIME.jmx
				 echo $?
				'''
            }
        }
        stage('Executing Jmeter Test') {
            steps {
                sh '''
				 time /opt/jmeter/bin/jmeter.sh -n -t /opt/jmeter/jmeter_kafka_files/TH_APP_th-cef_1.7_9092_3.2_GZIP_TIME.jmx
				 echo $?
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