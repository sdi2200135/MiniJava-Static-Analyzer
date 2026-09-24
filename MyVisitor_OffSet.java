import visitor.*;
import java.util.*;

class MyVisitor_OffSet extends GJDepthFirst<Void, Void>{
    private SymbolTable st;
    private List<ClassInfo_OffSet> cio;
    
    public MyVisitor_OffSet(SymbolTable st){
        this.st = st;
        this.cio = new ArrayList<>();
    }

    public void calculate_print(){      
        for(String c : st.getAllClass()){
            ClassInfo ci = st.getClass(c);
            ClassInfo_OffSet offsetInfo = new ClassInfo_OffSet(c);
            
            for(Map.Entry<String, String> f : ci.fields.entrySet())     //adds fields
                offsetInfo.addField(f.getKey(), f.getValue());
            
            if(ci.parent != null){          //adds methods
                ClassInfo parent = st.getClass(ci.parent);
                for(String m : ci.methods.keySet()) 
                    if (!parent.methods.containsKey(m)) 
                        offsetInfo.addMethod(m);
            } 
            else
                for(String m : ci.methods.keySet())
                    offsetInfo.addMethod(m);
                
            cio.add(offsetInfo);
        }
        
        for(ClassInfo_OffSet oi : cio)      //calculates offsets considering inheritance 
            calculate(oi);
        
        boolean flag = false;
        for(ClassInfo_OffSet offsetInfo : cio){         //prints offsets
            for (MethodInfo_Offset method : offsetInfo.methods) 
                if(method.name.equals("main"))
                    flag = true;
                
            if(flag == false){
                System.out.println("-------Class " + offsetInfo.name + "-------");
                print(offsetInfo);
            }
            
        }
    }
    
    private void calculate(ClassInfo_OffSet oi){
        ClassInfo ci = st.getClass(oi.name);
        
        int field_offset = 0;
        if(ci.parent != null){      //calculates field offsets
            ClassInfo_OffSet parent = find_class_offset(ci.parent);
            if(parent != null) 
                field_offset = parent.getLastFieldOffset() + get_type_size(st.getClass(ci.parent).fields.get(parent.getLastFieldName()));   
        }
        
        for(FieldInfo_Offset f : oi.fields){
            f.offset = field_offset;
            field_offset += get_type_size(f.type);
        }
        
        int method_offset = 0;
        if(ci.parent != null){      //calculates method offsets
            ClassInfo_OffSet parent = find_class_offset(ci.parent);
            if(parent != null){
                method_offset = parent.getLastMethodOffset() + 8;
                
                for(MethodInfo_Offset parentMethod : parent.methods)        //inherits parent methods 
                    oi.methods.add(new MethodInfo_Offset(parentMethod.name, parentMethod.offset));
            }
        }
        
        for(MethodInfo_Offset m : oi.methods)
            if(m.offset == -1){                 //only for new methods
                m.offset = method_offset;
                method_offset += 8;
            }  
    }
    
    private ClassInfo_OffSet find_class_offset(String c){
        for(ClassInfo_OffSet info : cio)
            if(info.name.equals(c)) 
                return info;
         
        return null;
    }
    
    private int get_type_size(String type) {
        if(type.equals("int")) 
            return 4;
        if(type.equals("boolean")) 
            return 1;
        return 8;                   //for arrays and objects
    }
    
    private void print(ClassInfo_OffSet oi){
        System.out.println("-------Variables-------");
        for(FieldInfo_Offset f : oi.fields) 
            System.out.println(oi.name + "." + f.name + " : " + f.offset);
        

        System.out.println("-------Methods-------");
        for(MethodInfo_Offset m : oi.methods){
            if(m.name.equals("main"))
                continue;

            System.out.println(oi.name + "." + m.name + " : " + m.offset);
        }
    }    
}