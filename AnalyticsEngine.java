import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsEngine {
	static final String[] languages = new String("c,c#,c++,java,python,ruby/ruby-on-rails,php,perl,"
			+ "javascript,haskell,ios/swift,sql,lisp").split(",");
	static int multipleLangsTagged = 0; //represents the number of posts that have multiple languages tagged
	public static void main(String [] args) throws IOException {
		
		HashMap<String, List<String>> languageMapping = new HashMap<>();
		for (String lang : languages)
			languageMapping.put(lang, new ArrayList<String>());
		languageMapping.put("other", new ArrayList<String>());
		String [] data = readFile("QueryResults.csv");
		
		//Column ordering: id, acceptedAnswer, creationDate, questionScore, title, ownerUserID, tags, answerID, answerScore, creationDate
		for (String row : data) {
			String [] split = row.split(",");
			String[] tags = splitTagsByComma(split[6]);
			int multiple = 0;
			for (String tag : tags) {
				if (languageMapping.keySet().contains(tag)) {
					languageMapping.get(tag).add(row);
					multiple ++;
				}
			}
			if (multiple > 1)
				multipleLangsTagged++;
			if (multiple == 0)
				languageMapping.get("other").add(row);	
		}
		for (Map.Entry<String, List<String>> key : languageMapping.entrySet()) {
			System.out.println(key.getKey()+ " had " + key.getValue().size() + " posts");
		}
	}
	
	static String [] readFile(String filePath) throws IOException {
		String rawData = new String(Files.readAllBytes(Paths.get(filePath)));
		return rawData.split(System.lineSeparator());
	}
	
	static String [] splitTagsByComma(String tagsList) {
		return tagsList.replaceAll("><", ",").replaceAll("<", "").replaceAll(">", "").replaceAll("\"", "").split(",");
	}
	
}
