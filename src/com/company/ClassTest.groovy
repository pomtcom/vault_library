class ClassTest {

    Script script;

    ClassTest(script) {          
        this.script = script;
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
        }else{
            // echo "THIS IS NOT WORKING" ;
            script.echo("this is not working");
        }

    }
}