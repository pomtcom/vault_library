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

        stage('Vault - Check params and assign value'){
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
        stage('Vault - create secret_id'){
            print 'creating secret_id'
            // POST


            withCredentials([string(credentialsId: 'VaultToken', variable: 'vaultToken')]) {
                // echo "My password is '${vaultToken}'!"

                def post = new URL(vaultHostAddr + "/v1/auth/approle/role/vault_poc_role/secret-id").openConnection();
                def message = '{}'
                post.setRequestMethod("POST")
                post.setDoOutput(true)

                post.setRequestProperty("X-Vault-Token", vaultToken)
                post.getOutputStream().write(message.getBytes("UTF-8"));
                // println(postRC);
                if(post.getResponseCode().equals(200)) {
                    def jsonResponse = post.getInputStream().getText() ;
                    def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                    secret_id = jsonSlurped['data']['secret_id'];
                    // print('secret_id is ' + secret_id)

                }
                else{
                    error("error for calling " + vaultHostAddr + "/v1/auth/approle/role/vault_poc_role/secret-id");
                    println('http error response code ' + post.getResponseCode());
                }
            }
            assert secret_id != null : 'secret_id is not generated, please check Vault API & token' ;
        }

        stage('Vault - generate role_token'){
            print 'generating role_token'
            def post = new URL(vaultHostAddr + "/v1/auth/approle/login").openConnection();
            def message = '{"role_id": "' + role_id + '",' + '"secret_id": "' + secret_id + '"}';
            post.setRequestMethod("POST");
            post.setDoOutput(true);
            post.getOutputStream().write(message.getBytes("UTF-8"));
            if(post.getResponseCode().equals(200)) {
                def jsonResponse = post.getInputStream().getText() ;
                def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                role_token = jsonSlurped['auth']['client_token'];
                // print('role_token is ' + role_token);
            }
            else{
                error("error for calling " + vaultHostAddr + "/v1/auth/approle/login");
                println('http error response code ' + post.getResponseCode());
            }

            assert role_token != null : 'role_token is not generated, please check role_id and secret_id for AppRole' ;

        // print('message to send is ' + message);
        }
        stage('Vault - get secret'){
            print 'getting secrt'
            def get = new URL(vaultHostAddr + "/v1/secret_poc/vault_poc_path").openConnection();
            get.setRequestProperty("X-Vault-Token", role_token)
            def getRC = get.getResponseCode();
            if(getRC.equals(200)) {
                def jsonResponse = get.getInputStream().getText() ;
                def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                
                def poc_password = jsonSlurped['data']['MySQL_PASSWORD'];
                print('MySQL_PASSWORD is ' + poc_password) ;
            }
            else{
                error("error for calling " + vaultHostAddr + "/v1/secret_poc/vault_poc_path");
                println('http error response code ' + getRC);
            }

        }

    }

}


