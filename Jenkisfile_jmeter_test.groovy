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

    stages {
        stage('Starting task') {
            steps {
                bash
				'''
				 timeout 60 /opt/jmeter/bin/jmeter.sh -n -t /opt/jmeter/jmeter_kafka_files/TH_APP_th-cef_1.7_9092_3.2_GZIP.jmx
				'''
            }
        }
    }
}