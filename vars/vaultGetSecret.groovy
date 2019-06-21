// groovy library for Vault get secret

def call(String name = 'human') {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello, ${name}." ;
    print 'Test print from pipeline' ;


    node {

        stage('TEST node') {
            print 'test node from library';
            // print 'role id is ' + role_id
        }

    }




}


