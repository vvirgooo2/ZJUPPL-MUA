import java.io.*;
import java.util.*;

public class function {
    public static HashMap<String, Value> GlobalValPool=new HashMap<String,Value>();//全局变量
    private static ArrayList<HashMap<String,Value>> history=new ArrayList<>();   //函数调用下一层的时候，应该把该函数的变量池存入子函数的history，找的时候从后往前找
    private HashMap<String,Value> ValPool=new HashMap<String,Value>();   //局部变量
    public int funID=-1;        //0代表主函数,list的ID应该和调用它的函数ID相同 1代表子函数
    public boolean ifret=false; //是否有返回值.
    public Value retv;          //返回值
    public InstStream sc;       //存这个函数应该执行的指令
    public boolean funend=false;//函数是否终止
    function(int ID){
        funID=ID;
    }
    public void test(){
        Value t=MUA_readlist();
        InstStream test=new InstStream(t);
        test.print();
    }
    public void exec(){
        while(sc.hasNext()){
             Value v=Get_value();
             if(funend==true) break;
             retv = v;
        }
    }
    //打印值
    public Value MUA_print(){
        Value t=Get_value();
        System.out.println(t.valuetoString());
        return t;
    }
    //定义值,尤其是函数的时候要检查闭包，本阶段主要处理这个部分

    Value makedepend(Value v){
        //只有显式定义的时候才检查，因为非显式定义的时候说明闭包已经处理好了
        ArrayList<String> freeval = Util.checkfreeval(v);
        //if(freeval.size()!=0) System.out.println("[Check] Find free value!");
        //check free value
        //从本地开始寻找，逐层找上去
        for (String val : freeval) {
            for (int i = history.size() - 1; i >= 0; i--) {
                HashMap<String, Value> pool = history.get(i);
                if (pool.containsKey(val)) {
                    v.dependence.put(val, pool.get(val));
                }
            }
        }
        return v;
    }


    public Value MUA_make(){
        Value t=new Value();
        Value name=Get_value(); //获得变量名
        boolean check=false;
        if(sc.next().equals("[")) check=true;
        sc.last();
        Value v=Get_value();    //获得变量值
        if(check==true&&v.listval.size()!=2)
            check=false;

        if(check) {
            makedepend(v);
        }

        //变量名检查暂时不写
        if(name.type!= Value.Type_enum.WORD) System.out.println("[ERROR!] The first value after make should be a string");
        if(funID==0){
            GlobalValPool.put(name.strval,v);
        }
        else ValPool.put(name.strval,v);
        return t;
    }
    //取值
    public Value MUA_thing(){
        Value t=t=Get_value();
        if(t.type!= Value.Type_enum.WORD) System.out.println("[ERROR!] Thing's parameter myst be a string");
        if(GlobalValPool.get(t.strval)!=null) t=GlobalValPool.get(t.strval);
        else t=ValPool.get(t.strval);
        return t;
    }
    //单独的MUA_read似乎没啥意义
    public Value MUA_read(){
        String input=sc.next();
        if(isNumber(input)){
            Value t=new Value(Double.parseDouble(input));
            return t;
        }
        else if(input.equals("true")){
            Value t=new Value(Boolean.parseBoolean("true"));
            return t;
        }
        else if(input.equals("false")){
            Value t=new Value(Boolean.parseBoolean("false"));
            return t;
        }
        else{
            Value t=new Value(input);
            return t;
        }
    }
    //计算函数
    public Value MUA_op(Value.OP_type op){
        Value t=new Value();
        Value a=Get_value();
        Value b=Get_value();
        if(a.type!= Value.Type_enum.NUMBER){
            if(isNumber(a.strval)) a=new Value(Double.parseDouble(a.strval));
            else System.out.println("[ERROR!] Operator can only operate number");
        }
        if(b.type!= Value.Type_enum.NUMBER){
            if(isNumber(b.strval)) b=new Value(Double.parseDouble(b.strval));
            else System.out.println("[ERROR!] Operator can only operate number");
        }
        switch (op){
            case add: t=a.op(b, Value.OP_type.add); break;
            case sub: t=a.op(b, Value.OP_type.sub); break;
            case div: t=a.op(b, Value.OP_type.div); break;
            case mul: t=a.op(b, Value.OP_type.mul); break;
            case mod: t=a.op(b, Value.OP_type.mod); break;
            default: System.out.println("not exist such operator");
        }
        return t;
    }
    //擦除一个变量
    public Value MUA_erase(){
        Value word =Get_value();
        Value ret=GlobalValPool.get(word.strval);
        GlobalValPool.remove(word.strval);
        return ret;
    }
    //确认变量
    public Value MUA_isname(){
        Value word =Get_value();
        if(GlobalValPool.get(word.strval)!=null) {
            return new Value(Boolean.parseBoolean("true"));
        }
        return new Value(Boolean.parseBoolean("false"));
    }
    //比较 0-eq 1-gt 2-lt
    public Value MUA_cmp(int op){
        Value v1=Get_value();
        Value v2=Get_value();
        if(op==0){
            if(v1.type== Value.Type_enum.NUMBER&&v2.type==Value.Type_enum.NUMBER) return new Value(v1.numval==v2.numval);
            else return new Value(v1.valuetoString().compareTo(v2.strval)==0);
        }
        if(op==1){
            if(v1.type== Value.Type_enum.NUMBER&&v2.type==Value.Type_enum.NUMBER) return new Value(v1.numval>v2.numval);
            else return new Value(v1.valuetoString().compareTo(v2.strval)>0);
        }
        if(op==2){
            if(v1.type== Value.Type_enum.NUMBER&&v2.type==Value.Type_enum.NUMBER) return new Value(v1.numval<v2.numval);
            else return new Value(v1.valuetoString().compareTo(v2.strval)<0);

        }
        return new Value(Boolean.parseBoolean("false"));
    }
    public Value MUA_and(){
        Value v1=Get_value();
        Value v2=Get_value();
        if(v1.type!= Value.Type_enum.BOOL||v2.type!=Value.Type_enum.BOOL) System.out.println("[ERROR] and need bool operand");
        return new Value(v1.boolval&&v2.boolval);
    }
    public Value MUA_or(){
        Value v1=Get_value();
        Value v2=Get_value();
        if(v1.type!= Value.Type_enum.BOOL||v2.type!=Value.Type_enum.BOOL) System.out.println("[ERROR] or need bool operand");
        return new Value(v1.boolval||v2.boolval);
    }
    public Value MUA_not(){
        Value v1=Get_value();
        if(v1.type!= Value.Type_enum.BOOL) System.out.println("[ERROR] not need bool operand");
        return new Value(!v1.boolval);
    }
    public Value MUA_isnumber(){
        return new Value(Get_value().type== Value.Type_enum.NUMBER);
    }
    public Value MUA_isword(){
        return new Value(Get_value().type== Value.Type_enum.WORD);
    }
    public Value MUA_islist(){
        return new Value(Get_value().type== Value.Type_enum.LIST);
    }
    public Value MUA_isbool(){
        return new Value(Get_value().type== Value.Type_enum.BOOL);
    }
    public Value MUA_isempty(){
        Value v=Get_value();
        if(v.type== Value.Type_enum.WORD){
            return new Value(v.strval=="");
        }
        else if(v.type==Value.Type_enum.LIST){
            return new Value(v.listval.size()==0);
        }
        else{
            System.out.println("[ERROR] isempty must follow a list or word");
        }
        return new Value("error");
    }
    //读一个list
    public Value MUA_readlist(){
        Value newlist=new Value();
        newlist.type= Value.Type_enum.LIST;
        while(true){
            String str=sc.next();
            if(str.equals("[")){
                Value sublist=MUA_readlist();
                newlist.listval.add(sublist);
            }
            else if(str.equals("]")) {  //有结束符号
                return newlist;
            }
            else{
                if(isNumber(str)) newlist.listval.add(new Value(Double.parseDouble(str)));
                else if(str.equals("true")) newlist.listval.add(new Value(Boolean.parseBoolean("true")));
                else if(str.equals("false")) newlist.listval.add(new Value(Boolean.parseBoolean("false")));
                else newlist.listval.add(new Value(str));
            }
        }
      }
    public Value MUA_runlist(Value list){
        function subfun=new function(this.funID);   //相当于和此时的函数同级
        subfun.ValPool=this.ValPool;
        subfun.ifret=true;
        subfun.sc=new InstStream(list);
        subfun.exec();
        ValPool= subfun.ValPool;
        if(subfun.funend==true){
            retv=subfun.retv;
            funend=true;
        }
        return subfun.retv;
    }

    //调用函数应该大改
    Value MUA_runfun(Value f){
        //创建子function
        ArrayList<Value> palist=f.listval.get(0).listval;
        int num_para=f.listval.get(0).listval.size();
        function subfun=new function(1);
        for(int i=0;i<num_para;i++){  //参数放入变量池
            Value p=Get_value();
            subfun.ValPool.put(palist.get(i).strval,p);
        }
        if(f.dependence.size()!=0)
        for(String key:f.dependence.keySet()){
            subfun.ValPool.put(key,f.dependence.get(key));
        }


        subfun.sc=new InstStream(f.listval.get(1));
        history.add(subfun.ValPool);
        subfun.exec();
        history.remove(history.size()-1);
        return subfun.retv;
    }

    //字表处理
    Value MUA_sentence(){
        Value v1=Get_value();
        Value v2=Get_value();

        if(v1.type== Value.Type_enum.LIST){
            if(v2.type!= Value.Type_enum.LIST){
                v1.listval.add(v2);
            }
            else{
                for(Value vin2:v2.listval){
                    v1.listval.add(vin2);
                }
            }
            return v1;
        }
        Value ret=new Value();
        ret.type= Value.Type_enum.LIST;
        if(v1.type!= Value.Type_enum.LIST){
            ret.listval.add(v1);
            ret.listval.add(v2);
        }

        return ret;
    }
    Value MUA_list(){
        Value v1=Get_value();
        Value v2=Get_value();
        Value ret=new Value();
        ret.type= Value.Type_enum.LIST;
        ret.listval.add(v1);
        ret.listval.add(v2);
        return ret;
    }
    Value MUA_if(){
        Value sel=Get_value();
        Value list1=Get_value();
        Value list2=Get_value();
        Value list;
        if(sel.boolval==true) list=list1;
        else list=list2;
        if(list.listval.size()==0) return list;
        if(list.listval.size()==1) return list.listval.get(0);
        return MUA_runlist(list);

    }
    Value MUA_join(){
        Value list=Get_value();
        Value v=Get_value();
        list.listval.add(v);
        return list;
    }
    Value MUA_first(){
        Value list=Get_value();
        if(list.type== Value.Type_enum.LIST)
            return list.listval.get(0);
        else return new Value(list.valuetoString().substring(0,1));
    }
    Value MUA_butfirst(){
        Value list=Get_value();
        if(list.type== Value.Type_enum.LIST) {
            list.listval.remove(0);
            return list;
        }
        else return new Value(list.valuetoString().substring(1));
    }
    Value MUA_last(){
        Value list=Get_value();
        if(list.type== Value.Type_enum.LIST)
            return list.listval.get(list.listval.size()-1);
        else return new Value(list.valuetoString().substring(list.valuetoString().length()-1));
    }
    Value MUA_butlast(){
        Value list=Get_value();
        if(list.type== Value.Type_enum.LIST) {
            list.listval.remove(list.listval.size()-1);
            return list;
        }
        else return new Value(list.valuetoString().substring(0,list.valuetoString().length()-1));
    }
    Value MUA_word(){
        Value v=Get_value();
        Value v2=Get_value();
        return new Value(v.valuetoString()+v2.valuetoString());
    }
    Value MUA_readlistreal(){
        Value list=new Value();
        list.type= Value.Type_enum.LIST;
        String l=sc.next();
        String[] strs=l.split(" ");
        for(String s:strs){
            list.listval.add(new Value(s.trim()));
        }
        return list;
    }
    void MUA_return(){
        funend=true;
        boolean check=false;
        if(sc.next()=="[") check=true;
        sc.last();
        retv=Get_value();
        if(check==true&&retv.type== Value.Type_enum.LIST&&retv.listval.size()==2){
            retv=makedepend(retv);
        }
    }
    public void MUA_export(Value name){
        Value v = ValPool.get(name.strval);
        GlobalValPool.put(name.strval, v);
    }
    Value MUA_random(){
        Value v=Get_value();
        double ret=0;
        if(v.type== Value.Type_enum.NUMBER){
            ret=Math.random()*v.numval;
        }
        return new Value(ret);
    }
    Value MUA_int(){
        Value t=Get_value();
        return new Value((double)(int)t.numval);
    }
    Value MUA_sqrt(){
        Value t=Get_value();
        return new Value(Math.sqrt(t.numval));
    }
    Value MUA_save() {
        Value t=Get_value();
        String filename=t.valuetoString();
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            HashMap<String,Value> Pool;
            if(funID==0){
                Pool=GlobalValPool;
            }
            else Pool=ValPool;
            if(Pool!=null)
                for(String key:Pool.keySet()){
                    Value sv=Pool.get(key);
                    if(sv.type== Value.Type_enum.NUMBER) out.write("make \""+key+" "+sv.numval+"\n");
                    if(sv.type== Value.Type_enum.WORD) out.write("make \""+key+" \""+sv.valuetoString()+"\n");
                    if(sv.type==Value.Type_enum.BOOL) out.write("make \""+key+" "+sv.boolval+"\n");
                    if(sv.type==Value.Type_enum.LIST) out.write("make \""+key+" ["+sv.valuetoString()+"]\n");

                }
            out.close();
        }
        catch (IOException e)
        {

        }
        return new Value(t.valuetoString());

    }
    Value MUA_load(){
        Value t=Get_value();
        String filename=t.valuetoString();
        function subfun=new function(this.funID);   //相当于和此时的函数同级
        subfun.ValPool=this.ValPool;
        subfun.sc=new InstStream(new File(filename));
        subfun.exec();
        ValPool= subfun.ValPool;
        return new Value(new Boolean("true"));
    }
    Value MUA_erall(){
        if(funID==0) GlobalValPool.clear();
        else ValPool.clear();
        return new Value(new Boolean("true"));
    }
    //执行后面一个不可断的所有可能获得value返回的语句，返回一个value
    public Value Get_value(){
        String cmd=sc.next();
        Value t=new Value("defalut");
        //判断是否是字符串
        if(cmd.charAt(0)=='\"'){
            if(isNumber(cmd.substring(1))) return new Value(Double.parseDouble(cmd.substring(1)));
            return new Value(cmd.substring(1));
        }
        else if(cmd.charAt(0)==':'){
            if(funID==0){
                if(GlobalValPool.containsKey(cmd.substring(1)))
                    return GlobalValPool.get(cmd.substring(1)).clone();
                System.out.println("[ERROR!] No such variable "+cmd.substring(1));
            }
            else {
                if(ValPool.containsKey(cmd.substring(1)))
                    return ValPool.get(cmd.substring(1)).clone();
                else if(GlobalValPool.containsKey(cmd.substring(1)))
                    return GlobalValPool.get(cmd.substring(1)).clone();
                System.out.println("[ERROR!] No such variable " +cmd.substring(1));
            }
        }
        else if(cmd.charAt(0)=='[')   return MUA_readlist();
        else if(cmd.equals("return")) MUA_return();
        else if(cmd.equals("thing"))  return MUA_thing();
        else if(cmd.equals("read"))   return MUA_read();
        else if(cmd.equals("print"))  return MUA_print();
        else if(cmd.equals("make"))   return MUA_make();
        else if(cmd.equals("add"))    return MUA_op(Value.OP_type.add);
        else if(cmd.equals("sub"))    return MUA_op(Value.OP_type.sub);
        else if(cmd.equals("mul"))    return MUA_op(Value.OP_type.mul);
        else if(cmd.equals("div"))    return MUA_op(Value.OP_type.div);
        else if(cmd.equals("mod"))    return MUA_op(Value.OP_type.mod);
        else if(cmd.equals("erase"))  return MUA_erase();
        else if(cmd.equals("isname")) return MUA_isname();
        else if(cmd.equals("eq"))     return MUA_cmp(0);
        else if(cmd.equals("gt"))     return MUA_cmp(1);
        else if(cmd.equals("lt"))     return MUA_cmp(2);
        else if(cmd.equals("and"))    return MUA_and();
        else if(cmd.equals("or"))     return MUA_or();
        else if(cmd.equals("not"))    return MUA_not();
        else if(cmd.equals("isnumber"))return MUA_isnumber();
        else if(cmd.equals("isword")) return MUA_isword();
        else if(cmd.equals("islist")) return MUA_islist();
        else if(cmd.equals("isbool")) return MUA_isbool();
        else if(cmd.equals("isempty"))return MUA_isempty();
        else if(cmd.equals("if"))     return MUA_if();
        else if(cmd.equals("sentence")) return  MUA_sentence();
        else if(cmd.equals("list")) return MUA_list();
        else if(cmd.equals("join")) return MUA_join();
        else if(cmd.equals("first")) return MUA_first();
        else if(cmd.equals("butfirst")) return MUA_butfirst();
        else if(cmd.equals("last")) return MUA_last();
        else if(cmd.equals("butlast")) return MUA_butlast();
        else if(cmd.equals("word")) return MUA_word();
        else if(cmd.equals("readlist")) return MUA_readlistreal();
        else if(cmd.equals("random")) return MUA_random();
        else if(cmd.equals("int")) return MUA_int();
        else if(cmd.equals("sqrt")) return MUA_sqrt();
        else if(cmd.equals("load")) return MUA_load();
        else if(cmd.equals("save")) return MUA_save();
        else if(cmd.equals("erall")) return MUA_erall();
        else if(cmd.equals("run")){
            Value list=Get_value();
            return MUA_runlist(list);
        }
        else if(cmd.equals("export")){
            Value name=Get_value();
            MUA_export(name);
        }
        else if(isNumber(cmd))        return new Value(Double.parseDouble(cmd));
        else if(cmd.equals("true"))   return new Value(Boolean.parseBoolean("true"));
        else if(cmd.equals("false"))  return new Value(Boolean.parseBoolean("false"));
        else{
            if(funID==0) {
                if (GlobalValPool.containsKey(cmd)) {
                    Value f = GlobalValPool.get(cmd);
                    if (f.type == Value.Type_enum.LIST) return MUA_runfun(f);
                }
            }
            else {
                if (ValPool.containsKey(cmd)){
                    Value f = ValPool.get(cmd);
                    if (f.type == Value.Type_enum.LIST) return MUA_runfun(f);
                }
                if (GlobalValPool.containsKey(cmd)) {
                    Value f = GlobalValPool.get(cmd);
                    if (f.type == Value.Type_enum.LIST) return MUA_runfun(f);
                }
            }
            System.out.println("[ERROR!] No such function "+ cmd);

        }
        return t;
    }
    //判断数字
    public static  boolean isNumber(String str){
        Boolean flag = false;
        String tmp;
        if(!str.equals("")){
            if(str.startsWith("-")){
                tmp = str.substring(1);
            }else{
                tmp = str;
            }
            flag = tmp.matches("^[0.0-9.0]+$");
        }
        return flag;
    }
}
