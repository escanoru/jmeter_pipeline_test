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
		description: '<h4>Broker nodes where the events will be sent/h4>'
		)
        password(
		name: 'Host_Password', 
		defaultValue: 'arst@dm1n', 
		description: '<h4>Host root\'s password. The default password is <span style="color:red">arst@dm1n</span>, you can change it by clicking on "Change Password".</h4>'
		)
        string(
		name: 'Interval', 
		defaultValue: '10', 
		description: '<h4>How often(in seconds) the metrics will be scrapped, default is 10 seconds.</h4>'
		)
        string(
		name: 'InfluxDB', 
		defaultValue: '15.214.128.179', 
		description: '<h4>The InfluxDB server ip/hostname where the metrics will be sent. If you don\'t have an InfluxDB instance you can use <span style="color:red">15.214.128.179</span> with the database name <span style="color:red">system_test</span></h4>'
		)
        string(
		name: 'Database', 
		defaultValue: 'system_test', 
		description: '<h4>The data base name where the metrics will be stored. If you don\'t have an InfluxDB instance you can use <span style="color:red">15.214.128.179</span> with the database name <span style="color:red">system_test</span></h4>'
		)
		string(
		name: 'Username', 
		defaultValue: '', 
		description: '<h4>The influxDB username to authenticate before sending metrics to InfluxDB, leave empty if this wasn\'t setup on your influxDB instance</h4>'
		)
		password(
		name: 'Username_Password', 
		defaultValue: '', 
		description: '<h4>The influxDB username\'s password to authenticate before sending metrics to InfluxDB, leave empty if this wasn\'t setup on your influxDB instance</h4>'
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