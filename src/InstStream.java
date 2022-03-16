import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class InstStream {
    ArrayList<String> s;
    int cur=-1;
    InstStream(){  //缺省参数就是从Systemin导入
        Scanner sc=new Scanner(System.in);
        s=new ArrayList<String>();
        while(sc.hasNext()){      //此处要把括号都分开
            String t=sc.next();
            if(t.equals("readlist")){ //把下一行存成一个字符串
                s.add(t);
                String n=sc.nextLine(); //不知道为啥，第一个nextline不太对
                if(n.equals(" ")){
                    s.add(sc.nextLine());
                    continue;
                }
                s.add(n);
                continue;

            }
            while(true){
                if(t.contains("[")==false) break;
                else{
                    s.add("[");
                    t=t.substring(1);
                }
            }
            while(true){
                if(t.contains("]")==false) break;
                else{
                    if(t.charAt(0)!=']'){
                        int pos=t.indexOf("]");
                        s.add(t.substring(0,pos));
                        t=t.substring(pos);
                    }
                    else{
                        s.add("]");
                        t=t.substring(1);
                    }
                }
            }
            if(t.equals("")==false) s.add(t);
        }
    }
    InstStream(File f){  //缺省参数就是从Systemin导入
        try{
            Scanner sc=new Scanner(new FileInputStream(f.getName()));
            s=new ArrayList<String>();
            while(sc.hasNext()){      //此处要把括号都分开
                String t=sc.next();
                if(t.equals("readlist")){ //把下一行存成一个字符串
                    s.add(t);
                    String n=sc.nextLine(); //不知道为啥，第一个nextline不太对
                    if(n.equals(" ")){
                        s.add(sc.nextLine());
                        continue;
                    }
                    s.add(n);
                    continue;

                }
                while(true){
                    if(t.contains("[")==false) break;
                    else{
                        s.add("[");
                        t=t.substring(1);
                    }
                }
                while(true){
                    if(t.contains("]")==false) break;
                    else{
                        if(t.charAt(0)!=']'){
                            int pos=t.indexOf("]");
                            s.add(t.substring(0,pos));
                            t=t.substring(pos);
                        }
                        else{
                            s.add("]");
                            t=t.substring(1);
                        }
                    }
                }
                if(t.equals("")==false) s.add(t);
        }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    InstStream(Value list){  //从一个list里面还原指令流\
        s=new ArrayList<String>();
        praselist(list);
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
    boolean hasNext(){
        return s.size()>cur+1;
    }
    String next(){
        return s.get(++cur);
    }
    String cur(){
        return s.get(cur);
    }
    String setcur(String in){
        return s.set(cur,in);
    }
    void last(){
        cur--;
    }
    public void print(){
        for(int i=0;i<s.size();i++){
            System.out.print(s.get(i)+" ");
        }
    }
}
