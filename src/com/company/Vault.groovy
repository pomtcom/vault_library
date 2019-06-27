import groovy.json.JsonSlurper

class Vault {

    Script script;
    def role_id
    def secret_id
    def role_token
    def vaultHostAddr


    def Vault(script_in) {      
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
                // echo "My password is '${vaultToken}'!"

                def post = new URL(vaultHostAddr + "/v1/auth/approle/role/vault_poc_role/secret-id").openConnection();
                def message = '{}'
                post.setRequestMethod("POST")
                post.setDoOutput(true)

                post.setRequestProperty("X-Vault-Token", script.vaultToken)
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
                    script.echo('http error response code ' + post.getResponseCode());
                }
            }
            assert secret_id != null : 'secret_id is not generated, please check Vault API & token' ;
            script.echo('secret_id is ' + secret_id);

        }

        script.stage('Vault - generate role_token'){
            // print 'generating role_token'
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
                script.echo('http error response code ' + post.getResponseCode());
            }

            assert role_token != null : 'role_token is not generated, please check role_id and secret_id for AppRole' ;

        // // print('message to send is ' + message);
        }

        // script.stage('Vault - get secret'){
        //     // print 'getting secrt'
        //     def get = new URL(vaultHostAddr + "/v1/secret_poc/vault_poc_path").openConnection();
        //     get.setRequestProperty("X-Vault-Token", role_token)
        //     def getRC = get.getResponseCode();
        //     if(getRC.equals(200)) {
        //         def jsonResponse = get.getInputStream().getText() ;
        //         def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                
        //         def poc_password = jsonSlurped['data']['MySQL_PASSWORD'];
        //         script.echo('MySQL_PASSWORD is ' + poc_password) ;
        //     }
        //     else{
        //         error("error for calling " + vaultHostAddr + "/v1/secret_poc/vault_poc_path");
        //         script.echo('http error response code ' + getRC);
        //     }

        // }
    }

    // def test_return() {
    //     script.echo('test return is executing');
    //     return 999999999;
    // }

    def getSecret(path, secret_key){

            // def get = new URL(vaultHostAddr + "/v1/secret_poc/vault_poc_path").openConnection();
            def get = new URL(vaultHostAddr + "/v1/" + path).openConnection();
            get.setRequestProperty("X-Vault-Token", role_token)
            def getRC = get.getResponseCode();
            if(getRC.equals(200)) {
                def jsonResponse = get.getInputStream().getText() ;
                def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                
                // def poc_password = jsonSlurped['data']['MySQL_PASSWORD'];
                def poc_password = jsonSlurped['data'][secret_key];
                script.echo('secretKey is ' + poc_password) ;
            }
            else{
                error("error for calling " + vaultHostAddr + "/v1/secret_poc/vault_poc_path");
                script.echo('http error response code ' + getRC);
            }

    }



    String testString;
    def testInt

    def method1(){
        print 'hello from method 1';
        // echo 'HHHHHHHHHHHHH';
        // return "99999999" ;

        if (testInt == 3){
            // echo "THIS IS WORKING" ;
            script.echo("this is working");
            script.echo("environment param is ");
            // script.echo(params.environment);
            // script.echo('test again');
            script.echo(script.params.environment);
            script.echo('end of test');

            script.stage("NEW STAGE XXXXXXX"){
                script.echo('NEW STAGE XXXXXXX');
            }

        }else{
            // echo "THIS IS NOT WORKING" ;
            script.echo("this is not working");
        }

    }
}