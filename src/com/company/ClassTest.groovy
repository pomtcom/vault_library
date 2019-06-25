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

        if (testInt == 3){
            // echo "THIS IS WORKING" ;
            script.echo("this is working");
            script.echo("environment param is ");
            // script.echo(params.environment);
            // script.echo('test again');
            script.echo(script.params.environment);
            script.echo('end of test');
        }else{
            // echo "THIS IS NOT WORKING" ;
            script.echo("this is not working");
        }

    }
}