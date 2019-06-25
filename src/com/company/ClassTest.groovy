class ClassTest {

    Script script;

    def ClassTest(script_in) {      
        this.script = script_in;
    }


    String testString;
    def testInt

    def method1(){
        print 'hello from method 1';
        // echo 'HHHHHHHHHHHHH';
        // return "99999999" ;

        node {
            stage('XXXXXX'){
                script.echo("this is stage from pipeline XX");
            }
            stage('YYYYYY'){
                script.echo("This is stage from pipeline YY");
            }
        }



        if (testInt == 3){
            // echo "THIS IS WORKING" ;
            script.echo("this is working");
        }else{
            // echo "THIS IS NOT WORKING" ;
            script.echo("this is not working");
        }

    }
}