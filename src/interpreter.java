public class interpreter {
    public interpreter(){

    }
    public void test(){

    }
    public void exec(){
        function Global_fun=new function(0);
        Global_fun.sc=new InstStream();
        Global_fun.GlobalValPool.put("pi",new Value(3.14159));
        Global_fun.exec();
    }
}