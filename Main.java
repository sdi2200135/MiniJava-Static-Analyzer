import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import syntaxtree.*;

public class Main{
    public static void main(String[] args)throws Exception{
        if(args.length < 1){                                            //checking the function arguments
            System.err.println("Usage: java Main <inputFiles>");
            System.exit(1);
        }

        for(String filename : args){                                    //processing each input file
            System.out.println("\nFilename: " + filename + "\n");
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(args[0]);                     //creating an input stream to parse the file
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();                              //parsing the input file and creating an AST
                System.out.println("Program parsed successfully.");

                SymbolTable st = new SymbolTable();                     //creating the symbol table
                MyVisitor_ST eval = new MyVisitor_ST(st);
                root.accept(eval, null);

                // st.printTable();                                     //function that prints the symbol table

                MyVisitor_TC type_checking = new MyVisitor_TC(st);      //type checking
                root.accept(type_checking, null);

                MyVisitor_OffSet offset = new MyVisitor_OffSet(st);      //calculating offset
                offset.calculate_print();
            } 
            catch(ParseException ex){
                System.err.println(ex.getMessage());
            } 
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            } 
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}