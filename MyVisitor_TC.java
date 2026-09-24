import syntaxtree.*;
import visitor.*;

//this visitor performs type checking using the symbol table
class MyVisitor_TC extends GJDepthFirst<String, Void>{
    SymbolTable st;
    ClassInfo curr_c;
    MethodInfo curr_m;

    public MyVisitor_TC(SymbolTable st){
        this.st = st;
    }    

    /* MainCLass */
    @Override
    public String visit(MainClass n, Void argu)throws Exception{        //checks type correctness in main class
        n.f1.accept(this, argu);
        n.f11.accept(this, argu);
        
        for(String c : st.getAllClass()){                               //finds and sets current method to main
            ClassInfo ci = st.getClass(c);
            for(String m : ci.methods.keySet()){
                MethodInfo mi = ci.methods.get(m);
                if(m.equals("main"))
                    curr_m = mi;
            }
        }
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        
        return null;
    }

    /* ClassDeclaration */
    @Override
    public String visit(ClassDeclaration n, Void argu)throws Exception{     //checks type correctness in class declarations
        String className = n.f1.accept(this, argu); 
        curr_c = st.getClass(className); 

        n.f3.accept(this, argu);        //processes variable declarations
        n.f4.accept(this, argu);        //processes method declarations
        
        return null;
    }

    /* ClassExtendsDeclaration */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu)throws Exception{      //checks type correctness in extended class declarations
        String parent = n.f3.accept(this, argu);
        String name = n.f1.accept(this, argu);
        curr_c = st.getClass(name); 

        boolean flag = false;                       //verifies parent class exists
        for(String ci : st.getAllClass()){
            if(ci.equals(parent))
                break;
            else{
                for(String c : st.getAllClass())
                    if(c.equals(parent)){
                        flag = true;
                        break;
                    }

                if(flag == false)
                    throw new Exception("Cannot have as extend class an undefined class.");
            }   
        }
        
        n.f5.accept(this, argu);            //processes variable declarations
        n.f6.accept(this, argu);            //processes method declarations
 
        return null;
    }

    /* VarDeclaration */
    @Override
    public String visit(VarDeclaration n, Void argu)throws Exception{       //checks for duplicate variable declarations
        String name = n.f1.accept(this, argu);
        String type = n.f0.accept(this, argu);
        
        if(curr_m != null)          //checks for parameter/local variable name conflicts
            for(String lv : curr_m.getLocalVars().keySet())
                for(Parameter p : curr_m.getParameters())
                    if(name.equals(p.name) && lv.equals(name))
                        throw new Exception("Can't have parameter and local variable with the same name!");

        for(String c : st.getAllClass()){
            ClassInfo ci = st.getClass(c);
            for(String m : ci.methods.keySet()){
                MethodInfo mi = ci.methods.get(m);
                for(String f : ci.fields.keySet()){
                    if(!name.equals(f) && mi.getLocalVars().containsKey(name) && !type.equals(mi.getLocalVars1(name)) && !ci.node.equals(c))
                        throw new Exception("Duplicate local variable: " + name);
                    
                    for(Parameter p : mi.getParameters())
                        if(name.equals(p.name))
                            throw new Exception("Duplicate parameter: " + name);
                }                
            }
        }

        return null;
    }

    /* MethodDeclaration */
    String m_name;
    String class_name;
    int i;
    @Override
    public String visit(MethodDeclaration n, Void argu)throws Exception{        //performs type checking for method declarations
        String type = n.f1.accept(this, argu);
        String name = n.f2.accept(this, argu);
        
        i++;
        curr_m = curr_c.methods.get(name);
        
        for(Parameter param : curr_m.getParameters())           //adds parameters as local variables
            curr_m.addLocalVar(param.name, param.type);
              
        for(String c : st.getAllClass()){                       //checks method overriding in parent classes
            ClassInfo ci = st.getClass(c);
            for(String m : ci.methods.keySet()){
                MethodInfo mi = ci.methods.get(m);
                if(name.equals(m) && type.equals(mi.getReturnType()))
                    if(ci.parent != null)
                        for(String c1 : st.getAllClass()){
                            ClassInfo ci1 = st.getClass(c1);
                            for(String m1 : ci1.methods.keySet()){
                                MethodInfo mi1 = ci1.methods.get(m1);
                                if(ci.parent.equals(c1) && name.equals(m1))
                                    if(name.equals(m1) && type.equals(mi1.getReturnType())){
                                        if(mi.getParameters().isEmpty() && !mi1.getParameters().isEmpty() || !mi.getParameters().isEmpty() && mi1.getParameters().isEmpty())
                                            throw new Exception("Error in the common method declaration!");
                                        if(mi.getParameters().isEmpty() && mi1.getParameters().isEmpty())
                                            return null;
                                        else
                                            for(Parameter p : mi.getParameters())
                                                for(Parameter p1 : mi1.getParameters())
                                                    if((!p.name.equals(p1.name) && !p.type.equals(p1.type)) || p == null || p1 == null)
                                                        throw new Exception("Error in the common method declaration!");
                                    }
                            }
                        }                
            }
        }   
        
        if(m_name!= null && m_name.equals(name) && class_name!=null && class_name.equals(curr_c.node) && curr_c.parent== null && i > 1)     //checks for method overloading
            throw new Exception("Method overloading captured!!");

        for(String c : st.getAllClass()){       //checks for duplicate parameters
            ClassInfo ci = st.getClass(c);
            for(String m : ci.methods.keySet()){
                MethodInfo mi = ci.methods.get(m);
                String par_name = null;
                String par_type = null;
                for(Parameter p : mi.getParameters()){
                    if(par_name != null && par_type != null)
                        if(par_name.equals(p.name) && !par_type.equals(p.type))
                            throw new Exception("Duplicate parameter: " + par_name);
                    par_name = p.name;
                    par_type = p.type;
                }
            }
        }

        n.f7.accept(this, argu);        //processes variable declarations
        n.f8.accept(this, argu);        //processes statements
        
        String ret_expr = n.f10.accept(this, argu);
        String ret_expr_type = get_type(ret_expr, curr_c, curr_m);
        
        if(!ret_expr.equals(type) && ret_expr_type != null)     //checks return type matches declared type
            if(!ret_expr_type.equals(type))
                throw new Exception("Wrong expression type in return!!");
        if(ret_expr_type == null && !ret_expr.equals(type))
            throw new Exception("Wrong expression type in return!!");
        
        class_name = curr_c.node;
        m_name = name;

        return null;    
    }

    /* AssignmentStatement */
    @Override
    public String visit(AssignmentStatement n, Void argu)throws Exception{      //checks type correctness in assignments
        String id = n.f0.accept(this, argu);
        String expr = n.f2.accept(this, argu);
        if(!id.equals(expr)){           //checks type compatibility
            String type_id = curr_m.getLocalVars1(id);
            String type_expr = curr_m.getLocalVars1(expr);
            if(type_id != null){
                if(type_id.endsWith("[]"))
                    type_id = type_id.replace("[]", "");
                
                if(!type_id.equals(expr)){
                    if(type_expr != null)
                        for(String c : st.getAllClass()){
                            ClassInfo ci = st.getClass(c);
                        
                            if(type_expr.equals(c) && type_id.equals(ci.parent))
                                return null;
                            else if(type_expr.equals(c) && type_id.equals(c))
                                return null;
                        }
                    else{
                        for(String c : st.getAllClass()){
                            ClassInfo ci = st.getClass(c);
                            if(expr.equals(c) && type_id.equals(ci.parent)) 
                                return null;
                            
                            String type_expr1 = get_type(expr, ci, curr_m);
                            if(type_expr1 != null)
                                return null;
                            
                           }
                    }
                    throw new Exception("Type error, exprcted: " + type_id);
                }
            }
            else{                        
                type_id = get_type(id, curr_c, curr_m);
                if(type_id.endsWith("[]"))
                    type_id = type_id.replace("[]", "");
                
                if(type_id.equals(expr))
                    return null;
                
                if(type_expr == null){
                    type_expr = get_type(expr, curr_c, curr_m);
                    if(!type_expr.equals(type_id) && type_expr!=null && type_id!=null)
                        throw new Exception("Type error, expected: " + type_id);
                }
                else
                    if(!type_expr.equals(type_id))
                        throw new Exception("Type error, expected: " + type_id);
            }
        }
    
        return null;
    }

    /* ArrayAssignmentStatement */
    @Override
    public String visit(ArrayAssignmentStatement n, Void argu)throws Exception{     //checks type correctness in array assignments
        String arr_type = n.f0.accept(this, argu);
        String ind_type = n.f2.accept(this, argu);
        String expr = n.f5.accept(this, argu);
        
        if(ind_type.equals("int"))
            return null;
        
        if(!ind_type.equals("int")){
            String type1 = curr_m.getLocalVars1(arr_type);
            if(type1!= null){
                if(!type1.endsWith("[]"))
                    throw new Exception("Expected array!!");

                if(!ind_type.equals("int")){
                    String type2 = curr_m.getLocalVars1(ind_type);
                    if(type2!=null)
                        if(!type2.equals("int"))
                            throw new Exception("Expected integer");
                        else
                            return null;
                }
            
                if(!expr.equals(type1.replace("[]", ""))){
                    String type3 = curr_m.getLocalVars1(expr);
                    if(!type3.equals(type1.replace("[]", "")))
                        throw new Exception("Type Error: expected: " + type1.replace("[]", ""));
                }
                else
                    throw new Exception("Index type should be integer!");
            }
        }
        
        return null;
    }

    /* IfStatement */
    @Override 
    public String visit(IfStatement n, Void argu)throws Exception{      //checks condition is boolean type
        String cond = n.f2.accept(this, argu);
        
        String result = check_boolean_1(cond);
        if(result == "error")
            throw new Exception("Condition sould be boolean");
        else{
            n.f4.accept(this, argu);
            n.f6.accept(this, argu);
            return null;
        }
    }

    /* WhileStatement */
    @Override 
    public String visit(WhileStatement n, Void argu)throws Exception{       //checks condition is boolean type
        String cond = n.f2.accept(this, argu);

        String result = check_boolean_1(cond);
        if(result == "error")
            throw new Exception("Condition sould be boolean");
        else{
            n.f4.accept(this, argu);
            return null;
        }
    }

    /* PrintStatement */
    @Override
    public String visit(PrintStatement n, Void argu)throws Exception{       //checks println argument is integer type
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr = n.f2.accept(this, argu);
        if(expr.equals("int"))
            return null;
        
        String type_expr = get_type(expr, curr_c, curr_m);
        
        if(!type_expr.equals("int"))
            throw new Exception("Can't call println function with no integer type!");
        
        return null;
    }

    /* ArrayLookup */
    @Override
    public String visit(ArrayLookup n, Void argu)throws Exception{      //checks array access is valid
        String arr_type = n.f0.accept(this, argu);
        String ind_type = n.f2.accept(this, argu);

        String type1 = curr_m.getLocalVars1(arr_type);
        if(type1!=null){
            if(!type1.endsWith("[]"))
                throw new Exception("Expected array, found: " + type1);

            if(!ind_type.equals("int")){
                String type2 = curr_m.getLocalVars1(ind_type);
                if(!type2.equals("int"))
                    throw new Exception("Expected integer");
            }
            return type1.replace("[]", "");
        }
        else{
            type1 = get_type(arr_type, curr_c, curr_m);
            if(!type1.endsWith("[]"))
                throw new Exception("Expected array, found: " + type1);

            if(!ind_type.equals("int")){
                String type2 = curr_m.getLocalVars1(ind_type);
                if(!type2.equals("int"))
                    throw new Exception("Expected integer");
            }
            return type1.replace("[]", "");   
        }
    }

    /* NotExpression */
    @Override
    public String visit(NotExpression n, Void argu)throws Exception{    //checks operand is boolean
        String expr = n.f1.accept(this, argu);

        String result = check_boolean_1(expr);
        if(result == "error")
            throw new Exception("Operator '!' requires boolean operand");
        else
            return "boolean";
    }

    /* PlusExpression */
    @Override
    public String visit(PlusExpression n, Void argu)throws Exception{   //checks operands are integers
        String expr1 = n.f0.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        
        String result = check_int_2(expr1, expr2);
        if(result == "error")
            throw new Exception("Operator '+' requires integer operand");
        else
            return "int";
    }

    /* MinusExpression */
    @Override
    public String visit(MinusExpression n, Void argu)throws Exception{  //checks operands are integers
        String expr1 = n.f0.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        
        String result = check_int_2(expr1, expr2);
        if(result == "error")
            throw new Exception("Operator '-' requires integer operand");
        else
            return "int";
    }

    /* TimesExpression */
    @Override
    public String visit(TimesExpression n, Void argu)throws Exception{  //checks operands are integers
        String expr1 = n.f0.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        
        String result = check_int_2(expr1, expr2);
        if(result == "error")
            throw new Exception("Operator '*' requires integer operand");
        else
            return "int";
    }

    /* CompareExpression */
    @Override
    public String visit(CompareExpression n, Void argu)throws Exception{    //checks operands are integers
        String expr1 = n.f0.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        
        String result = check_int_2(expr1, expr2);
        if(result == "error")
            throw new Exception("Operator '<' requires integer operand");
        else
            return "boolean";
    }

    /* AndExpression */
    @Override
    public String visit(AndExpression n, Void argu)throws Exception{    //checks operands are booleans
        String expr1 = n.f0.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        
        String result = check_boolean_2(expr1, expr2);
        if(result == "error")
            throw new Exception("Operator '&&' requires boolean operand");
        else
            return "boolean";
    }

    /* ArrayLength */
    @Override
    public String visit(ArrayLength n, Void argu)throws Exception{  //checks array type and returns length as integer
        String arr_name = n.f0.accept(this, argu);
        String type = curr_m.getLocalVars1(arr_name);
        
        if(!type.endsWith("[]"))
            throw new Exception("Expected an array type variable");
        
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "int";
    }

    /* MessageSend */
    @Override
    public String visit(MessageSend n, Void argu)throws Exception{  //checks method calls and their types
        String id = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr = n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        String expr1 = n.f4.accept(this, argu);
        
        String id_type = curr_m.getReturnType();
        if(id_type != null && !id_type.equals("void") && expr != null){
            for(String ci :st.getAllClass())
                if(!id.equals(ci)){
                    String result = in_methods(id_type, expr, expr1);
                    return result;
                }
            return id_type;
        }

        if(expr != null)
            for(String ci :st.getAllClass()){
                if(id.equals(ci)){
                    String result = in_methods(id, expr, expr1);
                    return result;                 
                }        
                else{            
                    in_methods(id, expr, expr1);
                    
                    ClassInfo c = st.getClass(ci);
                    for(String m:c.methods.keySet()){
                        MethodInfo mi = c.methods.get(m);
                        if(expr.equals(m) ){
                            return mi.getReturnType();
                        }
                    }
                }
            }    

        return null;
    }

    /* ExpressionList */
    @Override
    public String visit(ExpressionList n, Void argu)throws Exception{   //processes comma-separated expressions
        String firstExpr = n.f0.accept(this, argu);  
        String tailResult = n.f1.accept(this, argu);
        
        String result = null;
        if(tailResult != null)
            result = firstExpr + ", " + tailResult;
        else    
            result = firstExpr + "";
        
        return result;
    }

    /* ExpressionTail */
    @Override
    public String visit(ExpressionTail n, Void argu)throws Exception{   //processes remaining expressions in list
        StringBuilder result = new StringBuilder();
        
        for(Node term : n.f0.nodes){  
            String termResult = term.accept(this, argu);
            if(termResult != null){
                if(result.length() > 0) 
                    result.append(", ");
                result.append(termResult);
            }
        }

        if(result.length() > 0)
            return result.toString();
        else
            return null;
        
    }

    /* ExpressionTerm */
    @Override
    public String visit(ExpressionTerm n, Void argu)throws Exception{   //processes single expression in list
        return n.f1.accept(this, argu); 
    }

    /* BooleanArrayAllocationExpression */
    @Override
    public String visit(BooleanArrayAllocationExpression n, Void argu)throws Exception{     //checks boolean array creation
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        String expr = n.f3.accept(this, argu);

        if(!expr.equals("boolean")){
            String result = get_type(expr, curr_c, curr_m);
            if(result == null)
                throw new Exception("Expected Boolean!");
        }
        return "boolean";
    }

    /* IntegerArrayAllocationExpression */
    @Override
    public String visit(IntegerArrayAllocationExpression n, Void argu)throws Exception{     //checks integer array creation
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        String expr = n.f3.accept(this, argu);

        if(!expr.equals("int")){
            String result = get_type(expr, curr_c, curr_m);
            if(result == null)
                throw new Exception("Expected Integer!");
        }
        
        return "int";
    }

    /* AllocationExpression */
    @Override
    public String visit(AllocationExpression n, Void argu)throws Exception{     //checks object creation
        n.f0.accept(this, argu);
        String id = n.f1.accept(this, argu);

        for (String ci : st.getAllClass())
            if(id.equals(ci)){
                ClassInfo ci1 = st.getClass(ci);
                for(String m : ci1.methods.keySet())
                    if(m.equals("main"))
                        throw new Exception("Cannot allocate the class that includes main function!");
            }
        return id;
    }

    /* BracketExpression */
    @Override
    public String visit(BracketExpression n, Void argu)throws Exception{    //processes parenthesized expressions
        return n.f1.accept(this, argu); 
    }

    /* ThisExpression */
    @Override
    public String visit(ThisExpression n, Void argu)throws Exception{   //checks valid use of 'this'
        n.f0.accept(this, argu);
        if(curr_c == null)
            throw new Exception("'this' cannot be used outside of a class");
            
        return curr_c.returnNode();
    }

    /* TrueLiteral */
    @Override
    public String visit(TrueLiteral n, Void argu)throws Exception{
        return "boolean";
    }

    /* FalseLiteral */
    @Override
    public String visit(FalseLiteral n, Void argu)throws Exception{
        return "boolean";
    }

    /* IntegerLiteral */
    @Override
    public String visit(IntegerLiteral n, Void argu)throws Exception{
        return "int";
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

    /* Helpful functions */
    private String in_methods(String id, String expr, String expr1)throws Exception{    //checks method calls and parameter types   
        String id_type = curr_m.getLocalVars1(id);

        for(String c : st.getAllClass()){
            ClassInfo ci = st.getClass(c);
            if(id_type!=null)
                if(id_type.equals(c) && ci.methods.isEmpty())
                    throw new Exception("There are no methods in this class!");
             
            for(String m : ci.methods.keySet()){
                MethodInfo mi = ci.methods.get(m);
                String expr_type = mi.getReturnType();
                
                if(expr.equals(m)){
                    if(expr_type != null && expr_type.equals(expr_type)){
                        if(expr_type.equals(id))
                            return expr_type;
                        
                        if(expr1 != null){
                            String expr1_type = mi.getReturnType();
                            for(Parameter p : mi.getParameters()){
                                if(p.type.equals(expr1))
                                    return expr1_type;
                                
                                
                                String type1 = curr_m.getLocalVars1(expr1);
                                if(p.type.equals(type1)) 
                                    return type1;
                                
                                for(String x : st.getAllClass()){
                                    ClassInfo xi = st.getClass(x);
                                    if(p.type.equals(xi.parent)) 
                                        return xi.parent;  
                                    
                                }
                                
                                String[] expr1Array = expr1.split(",\\s*");
                                boolean foundMatch = false;
                                
                                for(String name : expr1Array){
                                    String trimmedName = name.trim();
                                    
                                    if(p.type.equals(trimmedName)){
                                        foundMatch = true;
                                        break;
                                    }
                                    
                                    String varType = curr_m.getLocalVars1(trimmedName);
                                    if(p.type.equals(varType)){
                                        foundMatch = true;
                                        break;
                                    }
                                }
                                
                                if(!foundMatch) 
                                    throw new Exception("Wrong parameter type!!");
                            }                            
                        }
                        return expr_type;
                    }
                    else
                        throw new Exception("Error in the type");
                }
            }
        }
        return null;
    }
    
    private String get_type(String expr, ClassInfo ci, MethodInfo mi)throws Exception{      //gets type of expression
        for(Parameter par : mi.getParameters()){    //checks parameters first
            if(expr.equals(par.name)){
                String expr_type = par.type;
                if(expr_type.equals("String[]"))
                    throw new Exception("Can't use main fuction's parameter!");
                
                return expr_type;
            }
            else
                continue;
        }

        for(String var : mi.getLocalVars().keySet()){       //checks local variables
            if(expr.equals(var)){
                String expr_type = mi.getLocalVars1(expr);
                return expr_type;
            }
            else
                continue;
        }
        
        if(ci.fields!=null){        //checks fields
            for(String f : ci.getFields().keySet()){
                if(expr.equals(f)){
                    String expr_type = ci.getFields(expr);
                    return expr_type;
                }
                else
                    continue;
            } 

            if(ci.parent != null)       //checks parent class fields if exists
                for(String c : st.getAllClass()){
                    if(c.equals(ci.parent)){
                        ClassInfo ci_1 = st.getClass(c);
                        if(ci_1.parent != null){
                            String res = get_type(expr, ci_1, mi);
                            return res;
                        }
                        else
                            for(String f : ci_1.getFields().keySet()){
                                if(expr.equals(f)){
                                    String expr_type = ci_1.getFields(expr);
                                    return expr_type;
                                }
                                else
                                    continue;
                            }
                    }
                }
        }
        
        return null;
    }
    
    private String check_boolean_1(String expr){    //checks single boolean operand
        if(expr.equals("int"))
            return "error";
       
        if(expr.equals(expr)){
            String lv = curr_m.getLocalVars1(expr);
            if(!expr.equals("boolean")){
                if(lv == null){
                    String field = curr_c.getFields(expr);
                    if(field.equals("int"))
                        return "error";
                } 
                else if(lv.equals("int"))
                    return "error";
            }
        }

        return "ok";
    }

    private String check_int_2(String expr1, String expr2){     //checks two integer operands
        if(expr1.equals("boolean") || expr2.equals("boolean"))
            return "error"; 
       

        if(expr1.equals(expr1)){
            String lv1 = curr_m.getLocalVars1(expr1);
            if(!expr1.equals("int")){
                if(lv1 == null){
                    String field1 = curr_c.getFields(expr1);
                    if(field1.equals("boolean"))
                        return "error";
                } 
                else if(lv1.equals("boolean"))
                    return "error";
                else{
                    return "ok";
                }    
            }
            else if(expr2.equals(expr2)){
                String lv2 = curr_m.getLocalVars1(expr2);
                if(!expr2.equals("int")){
                    if(lv2 == null){
                        String field2 = curr_c.getFields(expr2);
                        if(field2.equals("boolean"))
                            return "error";    
                    } 
                    else if(lv2.equals("boolean"))
                        return "error";
                    else{
                        return "ok";
                    }
                }
            }
        }
        
        return "ok";
    }

    private String check_boolean_2(String expr1, String expr2){     //checks two boolean operands
        if(expr1.equals("int") || expr2.equals("int"))
            return "error"; 
       
        if(expr1.equals(expr1)){
            String lv1 = curr_m.getLocalVars1(expr1);
            if(!expr1.equals("boolean")){
                if(lv1 == null){
                    String field1 = curr_c.getFields(expr1);
                    if(field1.equals("int"))
                        return "error";
                } 
                else if(lv1.equals("int"))
                    return "error";    
            }
            else if(expr2.equals(expr2)){
                String lv2 = curr_m.getLocalVars1(expr2);
                if(!expr2.equals("boolean")){
                    if(lv2 == null){
                        String field2 = curr_c.getFields(expr2);
                        if(field2.equals("int"))
                            return "error";    
                    } 
                    else if(lv2.equals("int"))
                        return "error";
                }
            }
        }

        return "ok";
    }
}