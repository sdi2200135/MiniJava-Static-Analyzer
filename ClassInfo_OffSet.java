import java.util.*;

//class for storing the information of a class and providing them for calculating offset
class ClassInfo_OffSet{
    String name;
    List<FieldInfo_Offset> fields = new ArrayList<>();
    List<MethodInfo_Offset> methods = new ArrayList<>();
    
    public ClassInfo_OffSet(String name){
        this.name = name;
    }
    
    public void addField(String name, String type){
        fields.add(new FieldInfo_Offset(name, type));
    }
    
    public void addMethod(String name){
        methods.add(new MethodInfo_Offset(name, -1));  //-1 means not calculated yet
    }
    
    public int getLastFieldOffset(){
        if(fields.isEmpty()) 
            return 0;
        return fields.get(fields.size()-1).offset;
    }
    
    public String getLastFieldName(){
        if(fields.isEmpty()) 
            return null;
        return fields.get(fields.size()-1).name;
    }
    
    public int getLastMethodOffset(){
        if(methods.isEmpty()) 
            return 0;
        return methods.get(methods.size()-1).offset;
    }
}