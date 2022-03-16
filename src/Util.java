import java.util.*;

public class Util {

    //检查函数中的自由变量，返回自由变量的名字
    public static ArrayList<String> checkfreeval(Value fun){
        if(fun.type!= Value.Type_enum.LIST) System.out.println("Check error! [not a function]");
        Value parameter=fun.listval.get(0);
        ArrayList<String> defval=GetparaString(parameter); //获得参数变量表

        Value instruction=fun.listval.get(1);
        InstStream temp=new InstStream(instruction);
        ArrayList<String> inst =temp.s;
        ArrayList<String> freeval=Getfreeval(inst);       //获得自由变量表

        //自由变量表减去参数变量表，得到真正的自由变量表
        return  Listsub(freeval,defval);
    }

    //输入参数表，返回翻译出来的String类型的参数表
    private static ArrayList<String> GetparaString(Value parameter){
        ArrayList<String> ret=new ArrayList<String>();
        for(Value v:parameter.listval){
            if(v.type!= Value.Type_enum.WORD){
                System.out.println("Check error! [parameter must be all WORD]");
            }
            ret.add(v.strval);
        }
        return ret;
    }

    //输入运行表，返回所有的没有make的，且用到的自由变量
    private static ArrayList<String> Getfreeval(ArrayList<String> inst){
        ArrayList<String> def=new ArrayList<>();  //定义了的
        ArrayList<String> use=new ArrayList<>();  //用到的
        for(int i=0;i<inst.size();i++){
            String v=inst.get(i);
            if(v.equals("make")) {
                if(def.contains(inst.get(i+1).substring(1)) ) continue;
                def.add(inst.get(++i).substring(1));  //make过的加到def
                continue;
            }
            else if(v.charAt(0)==':') {
                if(use.contains(v.substring(1))) continue;
                use.add(v.substring(1)); //带冒号的加到use
                continue;
            }
            else if(!remainword.contains(v)){
                if(use.contains(v)||function.isNumber(v)) continue;
                use.add(v); //函数名
                continue;
            }
            else if(v.equals("[")&&(inst.get(i-1).equals("return")||inst.get(i-2).equals("make"))){ //跳过函数里面显显式定义的函数
                int left=1;
                int right=0;
                while(left!=right){
                    i++;
                    if(inst.get(i).equals("[")) left++;
                    if(inst.get(i).equals("]")) right++;
                }
            }
        }
        return Listsub(use,def);
    }

    private static ArrayList<String> Listsub(ArrayList<String> a, ArrayList<String> b){
        ArrayList<String> result=new ArrayList<>();
        for(String str:a){
            if(!b.contains(str)) result.add(str);
        }
        return result;
    }
    static HashSet<String> remainword=new HashSet<String>(){{add("return");add("thing");add("print");add("make");
    add("add");add("sub");add("mul");add("div");add("mod");add("erase");add("isname");add("eq");add("gt");
    add("lt");add("and");add("or");add("not");add("isnumber");add("isword");add("islist");add("isbool");add("isempty");
    add("if");add("run");add("run");add("export");add("true");add("false");add("[");add("sentence");add("list");add("join");add("word");
    add("first");add("butfirst");add("last");add("butlast");add("readlist");}};
}
