package model;
import main.Arguments;
import org.json.JSONObject;
public class CommandLineParser {

    public static void parse(String[] args)
    {
        for(int i=0;i<args.length;i++)
        {
            String[] parts = args[i].split("=");
            String key = parts[0];
            String value = parts[1];

            //fill in the cases
            //get the dataset filename
            if(key.compareTo("filename")==0)
                Arguments.minSup = Long.parseLong(value);
            //get the core filename
            if(key.compareTo("swapcondition")==0)
                Arguments.swapcondition = value;
            //assign the datasets folder
            if(key.compareTo("minNodeNum")==0)
                Arguments.minNodeNum = Long.parseLong(value);

            if(key.compareTo("maxNodeNum")==0)
                Arguments.maxNodeNum = Long.parseLong(value);
            //assign top-k
            if(key.compareTo("k")==0)
                Arguments.numberofpatterns = Integer.parseInt(value);
            if(key.compareTo("PRM")==0)
                Arguments.hasPRM = Boolean.parseBoolean(value);
            if(key.compareTo("isPESIndex")==0)
                Arguments.isPESIndex = Boolean.parseBoolean(value);
            if(key.compareTo("DSS")==0)
                Arguments.hasDSS= Boolean.parseBoolean(value);
//            if(key.compareTo("datasetFolder")==0)
//                Arguments.datasetsFolder = value;
        }
    }

    public static void Jsonparse(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: No JSON input provided.");
            return;
        }

        // 获取 JSON 字符串（假设 JSON 是通过第一个命令行参数传入）
        String jsonString = args[0];

        try {
            // 使用 org.json 解析 JSON 字符串
            JSONObject jsonObject = new JSONObject(jsonString);

            // 填充 Arguments 类的字段
            if (jsonObject.has("minSup")) {
                Arguments.minSup = Long.parseLong(jsonObject.getString("filename"));
            }
            if (jsonObject.has("swapcondition")) {
                Arguments.swapcondition = jsonObject.getString("swapcondition");
            }
            if (jsonObject.has("minNodeNum")) {
                Arguments.minNodeNum = jsonObject.getLong("minNodeNum");
            }
            if (jsonObject.has("maxNodeNum")) {
                Arguments.maxNodeNum = jsonObject.getLong("maxNodeNum");
            }
            if (jsonObject.has("k")) {
                Arguments.numberofpatterns = jsonObject.getInt("k");
            }
            if (jsonObject.has("PRM")) {
                Arguments.hasPRM = jsonObject.getBoolean("PRM");
            }
            if (jsonObject.has("isPESIndex")) {
                Arguments.isPESIndex = jsonObject.getBoolean("isPESIndex");
            }
            if (jsonObject.has("DSS")) {
                Arguments.hasDSS = jsonObject.getBoolean("DSS");
            }
            if (jsonObject.has("inFilePath")) {
                Arguments.inFilePath = jsonObject.getString("inFilePath");
            }
            if (jsonObject.has("coreFilePath")) {
                Arguments.coreFilePath = jsonObject.getString("coreFilePath");
            }
            if (jsonObject.has("outFilePath")) {
                Arguments.outFilePath = jsonObject.getString("outFilePath");
            }


        } catch (Exception e) {
            System.out.println("Error parsing JSON input: " + e.getMessage());
        }
    }
    public static boolean isJsonString(String input) {
        return input.trim().startsWith("{") && input.trim().endsWith("}");
    }

}


