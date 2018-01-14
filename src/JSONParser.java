import java.util.List;

import org.json.JSONObject;

public class JSONParser {

	String json;
	JSONObject obj;

	public JSONParser(String json) {
		System.out.println("Constructor: " + json);
		this.json = json;
		this.obj = new JSONObject(this.json);
	}

	public String getParsedValue(List<String> keys) {
		JSONObject copyobj = this.obj;
		String result = "";
		for (int i = 0; i < keys.size(); ++i) {
			System.out.println("keysize: " + keys.size() + ", index: " + i + ", value: " + keys.get(i));
			if (i != (keys.size() - 1)) {
				copyobj = copyobj.getJSONObject(keys.get(i));
			} else {
				result = copyobj.getString(keys.get(i));
			}
		}
		return result;
	}
}
