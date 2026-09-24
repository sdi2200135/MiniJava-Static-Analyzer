import java.util.HashMap;
import java.util.Set;

//class that stores the "ClassInfo", "MethodInfo" and "Parameter" information in a Symbol Table
class SymbolTable{
    private HashMap<String, ClassInfo> st;

    public SymbolTable(){
        this.st = new HashMap<>();
    }

    public void addClass(String node, ClassInfo classinfo)throws Exception{
        if(st.containsKey(node))
            throw new Exception("Class " + node + " has already been declared.");
        st.put(node, classinfo);
    } 

    public ClassInfo getClass(String node){
        return st.get(node);
    } 

    public boolean containsClass(String node){
        return st.containsKey(node);
    }

    public Set<String> getAllClass(){
        return st.keySet();
    }

    public void printTable(){
        for(String className : st.keySet()){
            ClassInfo ci = st.get(className);
            System.out.println("Class " + className + (ci.parent != null ? " extends " + ci.parent : ""));
            System.out.println("  Fields:");
            for(String f : ci.fields.keySet())
                System.out.println("    " + ci.fields.get(f) + " " + f);
            System.out.println("  Methods:");
            for(String m : ci.methods.keySet()){
                MethodInfo mi = ci.methods.get(m);
                System.out.print("    " + mi.getReturnType() + " " + m + "(");
                boolean first = true;
                for (Parameter p : mi.getParameters()) {
                    if (!first) System.out.print(", ");
                    System.out.print(p.type + " " + p.name);
                    first = false;
                }
                System.out.print("       LocalVars:  " + mi.getLocalVars());

                System.out.println(")");
            }
        }
    }
}