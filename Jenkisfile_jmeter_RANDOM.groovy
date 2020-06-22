def setDescription() { 
  def item = Jenkins.instance.getItemByFullName(env.JOB_NAME) 
  item.setDescription("Jmeter test") 
  item.save()
  }
setDescription()


// Functions
def jmeter_command() {
    sh '''
	/opt/jmeter/bin/jmeter.sh -n -t ${WORKSPACE}/Jmeter_Pepperbox_Random_1.8_KB_message.jmx
	'''
}

jmeter_instances_1 = [
    "Single Instance": {
        jmeter_command()
    }
]

jmeter_instances_2 = [
    "Multiple Instances - 1": {
        jmeter_command()
    },
	"Multiple Instances - 2": {
        jmeter_command()
    }
]

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
		description: '<h4>Kafka broker nodes separated by comma, e.g 15.214.x.x, 15.214.x.x, 15.214.x.x, 15.214.x.x</h4>'
		)
        string(
		name: 'Zookeeper_Nodes', 
		defaultValue: '15.214.', 
		description: '<h4>Zookeeper nodes separated by comma, usually the zookeeper nodes are the same as the kafka broker nodes.</h4>'
		)
        string(
		name: 'Topic', 
		defaultValue: 'th-cef', 
		description: '<h3>Target topic where the events will be sent</a></h3>'
		)
        choice(
		name: 'Compression',
		choices: ['gzip', 'zstd', 'lz4','snappy'],
		description: '<h4>Compression type, by default SmartConnectors use GZIP</h4>'
		)	
        choice(
		name: 'acks',
		choices: ['0', '1','all'],
		description: '<h4>Acknowledgment mode</h4>'
		)
        string(
		name: 'Batch_Size', 
		defaultValue: '16384', 
		description: '<h4>Batch size in bytes. The Kafka default value is 16384.</h4>'
		)				
        string(
		name: 'Linger_Time', 
		defaultValue: '0', 
		description: '<h4>Linger time in miliseconds. The Kafka default value is 0. When using ZSTD compression type a linger time between 10ms and 30ms is recommmended.</h4>'
		)		
        string(
		name: 'Duration', 
		defaultValue: '60', 
		description: '<h4>Duration of test in seconds.</h4>'
		)			
        choice(
		name: 'Threads',
		choices: ['1', '2'],
		description: '<h4>Java threads per instance. 1 instance with 1 thread can generate around 10k when using GZIP compression (In a VM with 6cpu, 4GB RAm and 1GB NIC)</h4>'
		)			
        choice(
		name: 'Instances',
		choices: ['1', '2'],
		description: '<h4>Jmeter instances per node</h4>'
		)	
    }
	
    stages {
        stage('Setting Parameters') {
            steps {
                sh '''
				  sed -i "s/BROKER_NODES_FROM_JENKINS/$(echo "${Broker_Nodes}" | tr "," "\n" | awk '{print $1":9092"}' | paste -sd ",")/" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i "s/ZOOKEEPER_NODES_FROM_JENKINS/$(echo "${Zookeeper_Nodes}" | tr "," "\n" | awk '{print $1":2181"}' | paste -sd ",")/" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i \"s/TOPIC_NAME_FROM_JENKINS/${Topic}/\" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i \"s/COMPRESSION_FROM_JENKINS/${Compression}/\" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i \"s/ACKS_FROM_JENKINS/${acks}/\" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i \"s/LINGER_TIME_FROM_JENKINS/${Linger_Time}/\" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i \"s/BATCH_SIZE_FROM_JENKINS/${Batch_Size}/\" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i \"s/THREADS_FROM_JENKINS/${Threads}/\" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				  sed -i \"s/TIMER_FROM_JENKINS/${Duration}/\" ${WORKSPACE}/Jmeter_Pepperbox_MS_WindowsEvents.jmx
				'''
            }
        }		
        stage('Executing Jmeter Test (1 Instance)') {
            when {
                // Only say hello if a "greeting" is requested
                expression { params.Instances == '1' }
            }		
            steps {
			  script {
                    parallel(jmeter_instances_1)
                }
            }
        }
        stage('Executing Jmeter Test (2 Instances)') {
            when {
                // Only say hello if a "greeting" is requested
                expression { params.Instances == '2' }
            }		
            steps {
			  script {
                    parallel(jmeter_instances_2)
                }
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