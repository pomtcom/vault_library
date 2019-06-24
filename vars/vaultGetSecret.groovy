import groovy.json.JsonSlurper

// def role_id = params.role_id
// def secret_id
// def role_token

def call(String name = 'human') {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello, ${name}." ;
    print 'Test print from pipeline' ;


    node {

        stage('Check params'){
            assert params.role_id != '' : 'String param role_id should be assigned' ;
            assert params.environment != '' : 'String param environment should be assigned' ;
        }

        stage('TEST params node') {
            print 'test node from library';
            // print 'role id is ' + role_id
            print 'role_id is ' + params.role_id;
            print 'environment is ' + params.environment;
        }

    }




}


