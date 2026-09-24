import java.util.*;

//class for storing the information of a class and functions for accessing this information
public class ClassInfo{
    public String node;
    public String parent;
    public LinkedHashMap<String, String> fields = new LinkedHashMap<>();
    public LinkedHashMap<String, MethodInfo> methods = new LinkedHashMap<>();

    public ClassInfo(String node, String parent){
        this.node = node;
        this.parent = parent;
    } 

    public void addFields(String node, String type){
        fields.put(node, type);
    } 

    public void addMethods(String node, MethodInfo method){
        methods.put(node, method);
    } 

    public String returnNode(){
        return this.node;
    }

    public LinkedHashMap<String, String> getFields() {
        return fields;
    }

    public String getFields(String id) {
        return fields.get(id);
    }
}