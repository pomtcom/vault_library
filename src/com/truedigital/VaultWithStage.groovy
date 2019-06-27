import groovy.json.JsonSlurper

class VaultWithStage {

    Script script;
    def role_id
    def secret_id
    def role_token
    def vaultHostAddr


    def VaultWithStage(script_in) {      
        this.script = script_in;
    }

    def init(){
        script.stage('Vault - Check params and assign value'){
            assert script.params.role_id != null : 'String param role_id should be assigned' ;
            role_id = script.params.role_id

            assert script.params.environment != null : 'String param environment should be assigned' ;
            if (script.params.environment.toLowerCase() == 'dev' || 
            script.params.environment.toLowerCase() == 'alpha' || 
            script.params.environment.toLowerCase() == 'preprod' ) {
                vaultHostAddr = 'http://10.198.105.221:8200'
            }
            else if (script.params.environment.toLowerCase() == 'prod'){
                // TBD HostAddress for production Vault Cluster
                vaultHostAddr = ''
            }
        }
        script.stage('Vault - create secret_id'){
            
            script.withCredentials([script.string(credentialsId: 'VaultToken', variable: 'vaultToken')]) {
                def post = new URL(vaultHostAddr + "/v1/auth/approle/role/vault_poc_role/secret-id").openConnection();
                def message = '{}'
                post.setRequestMethod("POST")
                post.setDoOutput(true)
                post.setRequestProperty("X-Vault-Token", script.vaultToken)
                post.getOutputStream().write(message.getBytes("UTF-8"));
                if(post.getResponseCode().equals(200)) {
                    def jsonResponse = post.getInputStream().getText() ;
                    def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                    secret_id = jsonSlurped['data']['secret_id'];
                }
                else{
                    error("error for calling " + vaultHostAddr + "/v1/auth/approle/role/vault_poc_role/secret-id");
                    script.echo('http error response code ' + post.getResponseCode());
                }
            }
            assert secret_id != null : 'secret_id is not generated, please check Vault API & token' ;
            script.echo('secret_id is ' + secret_id);

        }

        script.stage('Vault - generate role_token'){
            script.echo('generate role_token');
            def post = new URL(vaultHostAddr + "/v1/auth/approle/login").openConnection();
            def message = '{"role_id": "' + role_id + '",' + '"secret_id": "' + secret_id + '"}';
            post.setRequestMethod("POST");
            post.setDoOutput(true);
            post.getOutputStream().write(message.getBytes("UTF-8"));
            if(post.getResponseCode().equals(200)) {
                def jsonResponse = post.getInputStream().getText() ;
                def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                role_token = jsonSlurped['auth']['client_token'];
            }
            else{
                error("error for calling " + vaultHostAddr + "/v1/auth/approle/login");
                script.echo('http error response code ' + post.getResponseCode());
            }

            assert role_token != null : 'role_token is not generated, please check role_id and secret_id for AppRole' ;
        }
    }

    def getSecret(path, secret_key){
            def get = new URL(vaultHostAddr + "/v1/" + path).openConnection();
            get.setRequestProperty("X-Vault-Token", role_token)
            def getRC = get.getResponseCode();
            if(getRC.equals(200)) {
                def jsonResponse = get.getInputStream().getText() ;
                def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                def secretValue = jsonSlurped['data'][secret_key];
                assert secretValue != null : 'cannot get secretValue from path ' +  "/v1/" + path + ' with key ' + secret_key;
                return secretValue ;
            }
            else{
                error("error for calling " + vaultHostAddr + "/v1/secret_poc/vault_poc_path");
                script.echo('http error response code ' + getRC);
            }

    }
}