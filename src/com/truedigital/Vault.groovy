import groovy.json.JsonSlurper




class Vault {

    // CONSTANCE VARIABLES FOR THIS LIBRARY
    final SECRET_GIT_TEMPLATE = "https://github.com/pomtcom/dmpss-omx-v3.git" ; 
    final SECRET_GIT_TEMPLATE_BRANCH = '*/master' ;
    final SECRET_TEMPLATE_YAML = 'secret_template.yaml'

    Script script;
    def role_id
    def secret_id
    def role_token
    def microservice_name
    def environment
    def vaultHostAddr


    def secretData


    def Vault(script_in) {      
        this.script = script_in;
    }

    def init(){
        // add checkout secret file here
        script.echo('checkout SCM version2 is executing');
        script.checkout([
            $class: 'GitSCM', 
            branches: [[name: SECRET_GIT_TEMPLATE_BRANCH]], 
            doGenerateSubmoduleConfigurations: false, 
            extensions: [], 
            submoduleCfg: [], 
            userRemoteConfigs: [[url: SECRET_GIT_TEMPLATE]]])
        script.echo('checkout SCM is completed (version2)');

        secretData = script.readYaml file: SECRET_TEMPLATE_YAML
        script.echo('secretData read value is ' + secretData.type);








        // ============================ Getting environment params ===============================
        script.echo('checking environment parameters') ;
        assert script.env.JOB_BASE_NAME != null : 'Get pipeline name has problem, please check'
        microservice_name = script.env.JOB_BASE_NAME;

        assert script.params.role_id != null : 'String param role_id should be assigned' ;
        role_id = script.params.role_id ;

        assert script.params.environment != null : 'String param environment should be assigned' ;
        environment = script.params.environment.toLowerCase() ;

        if (environment == 'dev' || environment == 'alpha' || environment == 'preprod' ) {
            vaultHostAddr = 'http://10.198.105.221:8200'
        }
        else if (environment == 'prod'){
            // TBD HostAddress for production Vault Cluster
            vaultHostAddr = ''
        }
        
        // ============================ Generate secret id =======================================
        script.echo('generating secret_id') ;
        script.withCredentials([script.string(credentialsId: 'VaultToken', variable: 'vaultToken')]) {
            def post = new URL(vaultHostAddr + "/v1/auth/approle/role/" + microservice_name + "/secret-id").openConnection();
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
                script.echo('http error response code ' + post.getResponseCode());
                error("Jenkins write secret_id error with calling " + vaultHostAddr + "/v1/auth/approle/role/" + microservice_name + "/secret-id");
            }
        }
        assert secret_id != null : 'secret_id is not generated, please check Vault API & token' ;

        // ============================ Generate role_token =======================================

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
            script.echo('http error response code ' + post.getResponseCode());
            error("error for calling " + vaultHostAddr + "/v1/auth/approle/login");
        }
        assert role_token != null : 'role_token is not generated, please check role_id and secret_id for AppRole' ;
    }


    def getSecret(category, secret_key){
        script.echo('get secret vaule of category ' + category + ' with key ' + secret_key);
        assert vaultHostAddr != null && role_token != null: 'vaultHostAddr or role_token are null value, please check that you have call .init() before getSecret' ;
        def secret_path = microservice_name + '/' + environment + '/' + category ;
        def get = new URL(vaultHostAddr + "/v1/" + secret_path).openConnection();
        get.setRequestProperty("X-Vault-Token", role_token)
        def getRC = get.getResponseCode();
        if(getRC.equals(200)) {
            def jsonResponse = get.getInputStream().getText() ;
            def jsonSlurped = new JsonSlurper().parseText(jsonResponse);
            def secretValue = jsonSlurped['data'][secret_key];
            assert secretValue != null : 'cannot get secretValue from secret_path ' +  "/v1/" + secret_path + ' with key ' + secret_key;

            

            return secretValue ;
        }
        else{
            script.echo('http error response code ' + getRC);
            error("error for calling " + vaultHostAddr + "/v1/" + secret_path);
        }

    }

    def putSecretTest(key,value){
        // def key = '55555'

        script.echo('put secret test is executing');

        script.echo('check class of secretData');

        script.echo("BEFORE GET CLASS")
        script.echo(secretData.getClass().getName());
        script.echo("AFTER GET CLASS")

        // script.echo('test hashmap is ' + secretData['data']['mysqlinstance1']);

        secretData['data'][key] = value

        // secretData.data.key = value;
        // secretData.data[key] = value ;
        script.sh "rm -rf newtest2.yaml"
        script.writeYaml file: 'newtest2.yaml', data: secretData ;


        script.echo('put secret test is completed');

    }

    // def checkOutSecretTemplate(){
    //     script.echo('checkout SCM version2 is executing');
    //     script.checkout([
    //         $class: 'GitSCM', 
    //         branches: [[name: SECRET_GIT_TEMPLATE_BRANCH]], 
    //         doGenerateSubmoduleConfigurations: false, 
    //         extensions: [], 
    //         submoduleCfg: [], 
    //         userRemoteConfigs: [[url: SECRET_GIT_TEMPLATE]]])
    //     script.echo('checkout SCM is completed (version2)');
    // }

    def defReadSecretFile(){
        
    }


    def test_usage(){
        script.echo('test usage is executing');
        // checkOutSecretTemplate();

        



    }

}