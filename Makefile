JTB_JAR=jtb132di.jar
JJ_JAR=javacc5.jar

all: compile

compile:
	java -jar $(JTB_JAR) -te MiniJava.jj
	java -jar $(JJ_JAR) MiniJava-jtb.jj
	javac syntaxtree/*.java visitor/*.java
	javac *.java

run: 
	java Main <inputfiles>

clean:
	rm -rf *.class syntaxtree visitor MiniJava-jtb.jj *~
	find . -maxdepth 1 -name "*.java" ! -name "Main.java" ! -name "MyVisitor_ST.java" ! -name "MyVisitor_OffSet.java" ! -name "MyVisitor_TC.java" ! -name "ClassInfo.java" ! -name "Parameter.java" ! -name "MethodInfo.java" ! -name "SymbolTable.java" ! -name "ClassInfo_OffSet.java" ! -name "MethodInfo_Offset.java" ! -name "FieldInfo_Offset.java" -exec rm {} +