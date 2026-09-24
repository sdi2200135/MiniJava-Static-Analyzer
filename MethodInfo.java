import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

//class for storing the information of a method and functions for accessing this information
public class MethodInfo{
    private String returnType;
    private List<Parameter> parameters = new ArrayList<>();
    private LinkedHashMap<String, String> localVars = new LinkedHashMap<>();

    public MethodInfo(String returnType) {
        this.returnType = returnType;
    }

    public void addParameter(String name, String type) {
        parameters.add(new Parameter(name, type));
    }

    public void addLocalVar(String name, String type) {
        localVars.put(name, type);
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public LinkedHashMap<String, String> getLocalVars() {
        return localVars;
    }

    public String getLocalVars1(String id) {
        return localVars.get(id);
    }

    public String getReturnType() {
        return returnType;
    }
}
