import syntaxtree.*;
import visitor.*;

//this visitor creates the symbol table
class MyVisitor_ST extends GJDepthFirst<String, Void>{
    SymbolTable st;
    ClassInfo curr_c = null;
    MethodInfo curr_m = null;

    public MyVisitor_ST(SymbolTable st){
        this.st = st;
    }

    /* Goal */
    @Override
    public String visit(Goal n, Void argu)throws Exception{
        n.f0.accept(this, argu);
        
        for(Node node: n.f1.nodes)
            node.accept(this, argu);
  
        return null;
    }

    /* MainClass */
    @Override
    public String visit(MainClass n, Void argu)throws Exception{    //adding information from MainClass
        String node = n.f1.accept(this, argu);
        String args = n.f11.accept(this, argu);
        
        curr_c = new ClassInfo(node, null);   
        MethodInfo main_m = new MethodInfo("void");
        main_m.addParameter(args, "String[]");
        curr_c.addMethods("main", main_m);

        st.addClass(node, curr_c);

        curr_m = main_m;
        n.f14.accept(this, argu);
        curr_m = null;

        curr_c = null;
        return null;
    }

    /* TypeDeclaration */
    @Override
    public String visit(TypeDeclaration n, Void argu)throws Exception{
        n.f0.accept(this, null);
        return null;
    }

    /* ClassDeclaration */
    @Override
    public String visit(ClassDeclaration n, Void argu)throws Exception{     //adding information from ClassDeclaration
        n.f0.accept(this, argu);

        String classname = n.f1.accept(this, argu);
        curr_c = new ClassInfo(classname, null);
        st.addClass(classname, curr_c);

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        curr_c = null;

        return null;
    }

    /* ClassExtendsDeclaration */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu)throws Exception{      //adding information from ClassExtendsDeclaration
        n.f0.accept(this, argu);

        String classname = n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        String parent = n.f3.accept(this, argu);
        curr_c = new ClassInfo(classname, parent);
        st.addClass(classname, curr_c);

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        curr_c = null;  

        return null;
    }

    /* VarDeclaration */
    @Override
    public String visit(VarDeclaration n, Void argu)throws Exception{       //adding information from VarDeclaration
        String _ret = null;
        String type = n.f0.accept(this, argu);
        String var = n.f1.accept(this, argu);
        
        if(curr_m != null)
            curr_m.addLocalVar(var, type);
        else if(curr_c != null)
            curr_c.addFields(var, type);    

        return _ret;
    }

    /* MethodDeclaration */
    @Override
    public String visit(MethodDeclaration n, Void argu)throws Exception{        //adding information from MethodDeclaration
        String myType = n.f1.accept(this, argu);
        String myName = n.f2.accept(this, argu);

        curr_m = new MethodInfo(myType);

        if (n.f4.present()) {
            String result = n.f4.accept(this, argu);
            if (result != null) {
                String[] parameters = result.split(",");
                for (String param : parameters) {
                    String[] parts = param.trim().split(" ");
                    if (parts.length == 2) {
                        String paramType = parts[0]; 
                        String paramName = parts[1]; 
                        curr_m.addParameter(paramName, paramType); 
                    }
                }
            }
        }

        n.f7.accept(this, argu);
        curr_c.addMethods(myName, curr_m);

        curr_m = null;

        return null;    
    }


    /* FormalParameterList */
    @Override
    public String visit(FormalParameterList n, Void argu)throws Exception{      //adding information from FormalParameterList
        String ret = n.f0.accept(this, null);

        if(n.f1 != null)
            ret += n.f1.accept(this, null);
        
        return ret;
    }

    /* FormalParameterTail */
    @Override
    public String visit(FormalParameterTail n, Void argu)throws Exception{      //adding information from FormalParameterTail
        String ret = "";
        for(Node node: n.f0.nodes)
            ret += ", " + node.accept(this, argu);

        return ret;
    }

    /* FormalParameterTerm */
    @Override
    public String visit(FormalParameterTerm n, Void argu)throws Exception{      //adding information from FormalParameterTerm
        return n.f1.accept(this, argu);
    }

    /* FormalParameter */
    @Override
    public String visit(FormalParameter n, Void argu)throws Exception{      //adding information from FormalParameter
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        return type +  " " + name;
    }

    /* Type */
    @Override 
    public String visit(BooleanArrayType n, Void argu){
        return "boolean[]";
    }

    public String visit(IntegerArrayType n, Void argu){
        return "int[]";
    }

    public String visit(BooleanType n, Void argu){
        return "boolean";
    }

    public String visit(IntegerType n, Void argu){
        return "int";
    }

    /* Identifier */
    @Override
    public String visit(Identifier n, Void argu){
        return n.f0.toString();
    }
}