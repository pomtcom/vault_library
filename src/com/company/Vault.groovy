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
            script.echo("TTTTTT") ;
            withCredentials([string(credentialsId: 'VaultToken', variable: 'vaultToken')]) {
                // echo "My password is '${vaultToken}'!"

                script.echo('test with credential') ;
            }




        }

        // script.stage('Vault - generate role_token'){
        //     print 'generating role_token'
        //     def post = new URL(vaultHostAddr + "/v1/auth/approle/login").openConnection();
        //     def message = '{"role_id": "' + role_id + '",' + '"secret_id": "' + secret_id + '"}';
        //     post.setRequestMethod("POST");
        //     post.setDoOutput(true);
        //     post.getOutputStream().write(message.getBytes("UTF-8"));
        //     if(post.getResponseCode().equals(200)) {
        //         def jsonResponse = post.getInputStream().getText() ;
        //         def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
        //         role_token = jsonSlurped['auth']['client_token'];
        //         // print('role_token is ' + role_token);
        //     }
        //     else{
        //         error("error for calling " + vaultHostAddr + "/v1/auth/approle/login");
        //         println('http error response code ' + post.getResponseCode());
        //     }

        //     assert role_token != null : 'role_token is not generated, please check role_id and secret_id for AppRole' ;

        // // print('message to send is ' + message);
        // }
        // script.stage('Vault - get secret'){
        //     print 'getting secrt'
        //     def get = new URL(vaultHostAddr + "/v1/secret_poc/vault_poc_path").openConnection();
        //     get.setRequestProperty("X-Vault-Token", role_token)
        //     def getRC = get.getResponseCode();
        //     if(getRC.equals(200)) {
        //         def jsonResponse = get.getInputStream().getText() ;
        //         def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
                
        //         def poc_password = jsonSlurped['data']['MySQL_PASSWORD'];
        //         print('MySQL_PASSWORD is ' + poc_password) ;
        //     }
        //     else{
        //         error("error for calling " + vaultHostAddr + "/v1/secret_poc/vault_poc_path");
        //         println('http error response code ' + getRC);
        //     }

        // }
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