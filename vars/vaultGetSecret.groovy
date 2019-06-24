import groovy.json.JsonSlurper

// def role_id = params.role_id
// def secret_id
// def role_token

def call(String name = 'human') {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello, ${name}." ;
    print 'Test print from pipeline' ;

    def role_id
    def secret_id
    def role_token

    def vaultHostAddr

    node {

        stage('Check params and assign value'){
            assert params.role_id != null : 'String param role_id should be assigned' ;
            role_id = params.role_id

            assert params.environment != null : 'String param environment should be assigned' ;
            if (params.environment.toLowerCase() == 'dev' || 
            params.environment.toLowerCase() == 'alpha' || 
            params.environment.toLowerCase() == 'preprod' ) {
                vaultHostAddr = 'http://10.198.105.221:8200'
            }
            else if (params.environment.toLowerCase() == 'prod'){
                // TBD HostAddress for production Vault Cluster
                vaultHostAddr = ''
            }

        }
        stage('create secret_id'){
            print 'creating secret_id'
            // POST
            def post = new URL(vaultHostAddr + "/v1/auth/approle/role/vault_poc_role/secret-id").openConnection();
            def message = '{}'
            post.setRequestMethod("POST")
            post.setDoOutput(true)
            post.setRequestProperty("X-Vault-Token", "s.3aVA6ckaOumc6N7WHTZHZ34a")
            post.getOutputStream().write(message.getBytes("UTF-8"));
            // println(postRC);
            if(post.getResponseCode().equals(200)) {
                def jsonResponse = post.getInputStream().getText() ;
                def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                
                // to add try catch for accessing json
                secret_id = jsonSlurped['data']['secret_id'];
                // print('secret_id is ' + secret_id)

            }else{
                println('http error response code ' + post.getResponseCode());
            }
        }

        stage('TEST OUTPUT') {
            print 'secret_id is ' + secret_id;
        }

    }




}


