import java.util.*;
public class Value {

    public Value(){
        type=Type_enum.NUMBER; // 默认为字
        numval=0;
        strval="default";
        boolval=false;
        valname="undefined";
        listval=new ArrayList<Value>();
        dependence=new HashMap<String,Value>();
    }
    public Value(double x){
        type=Type_enum.NUMBER; // 默认为字
        numval=x;
        strval="default";
        boolval=false;
    }
    public Value(String x){
        type=Type_enum.WORD; // 默认为字
        numval=13;
        strval=x;
        boolval=false;
    }
    public Value(boolean x){
        type=Type_enum.BOOL; // 默认为字
        numval=13;
        if(x==true) strval="true";
        else strval="false";
        boolval=x;
    }
    public enum Type_enum{
        NUMBER,WORD,BOOL,LIST,FUN,ERROR
    }
    //初始化表的函数，还没想好怎么写

    public Type_enum type;
    public double numval;
    public String strval;
    public boolean boolval;
    public String valname;
    public ArrayList<Value> listval;
    public HashMap<String,Value> dependence;

    //转换为输出量
    String valuetoString(){
        switch (type){
            case NUMBER: {
                String str=""+numval;
                if(str.endsWith(".0")) return str.substring(0,str.length()-2);
                return str;
            }
            case WORD: return strval;
            case BOOL: return boolval==true?"true":"false";
            case LIST: return Listout();
            default:
        }
        return "error";
    }

    ArrayList<String> s;
    String Listout(){
        s=new ArrayList<String>();
        praselist(this);
        StringBuffer str=new StringBuffer();
        for(int i=0;i<s.size();i++){
            str.append(s.get(i));
            if(s.get(i)!="["&&i!=s.size()-1&&s.get(i+1)!="]") str.append(" ");
        }
        s.clear();
        return str.toString();
    }
    private void praselist(Value list){
        for(Value v:list.listval){
            if(v.type!= Value.Type_enum.LIST) s.add(v.valuetoString());
            else{
                s.add("[");
                praselist(v);
                s.add("]");
            }
        }
    }
    public enum OP_type{
        add,sub,mul,div,mod
    }
    //add
    Value op(Value v,OP_type op){
        Value nv=new Value(666);
        if(type!=Type_enum.NUMBER){
            System.out.println("[VALUE ERROR] Value operator counter non-number val!");
            nv.numval=404;
            return nv;
        }
        else {
            switch (op){
                case add: nv.numval=this.numval+v.numval; break;
                case sub: nv.numval=this.numval-v.numval; break;
                case mul: nv.numval=this.numval*v.numval; break;
                case div: nv.numval=this.numval/v.numval; break;
                case mod: nv.numval=(double)((int)this.numval % (int)v.numval);
                default:
            }
            return nv;
        }
    }

    public Value clone(){
        Value nv=new Value();
        nv.type=this.type;
        nv.numval=this.numval;
        nv.strval=this.strval;
        if(this.listval!=null) {
            nv.listval = new ArrayList<Value>();
            for (Value v : this.listval) {
                nv.listval.add(v.clone());
            }
        }
        nv.boolval=this.boolval;
        nv.valname=this.valname;
        nv.dependence=this.dependence;
        return nv;
    }
}
