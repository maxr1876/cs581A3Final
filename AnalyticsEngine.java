import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class AnalyticsEngine {
	static final String[] languages = new String("c,c#,c++,java,python,ruby,ruby-on-rails,php,perl,"
			+ "javascript,haskell,ios,swift,sql,lisp,javadoc").split(",");
	static int multipleLangsTagged = 0; //represents the number of posts that have multiple languages tagged
	public static void main(String [] args) throws IOException {
		
		HashMap<String, List<String>> languageAnswerMapping = new HashMap<>();
		HashMap<String, Set<String>> languageQuestionMapping = new HashMap<>();
		for (String lang : languages) {
			languageAnswerMapping.put(lang, new ArrayList<String>());
			languageQuestionMapping.put(lang, new HashSet<String>());
		}
		languageAnswerMapping.put("other", new ArrayList<String>());
		languageQuestionMapping.put("other", new HashSet<String>());
		languageAnswerMapping.remove("ruby-on-rails");
		languageQuestionMapping.remove("ruby-on-rails");
		languageAnswerMapping.remove("ios");
		languageQuestionMapping.remove("ios");
		languageAnswerMapping.remove("javadoc");
		languageQuestionMapping.remove("javadoc");
		String [] data = readFile("QueryResults.csv");	
		//Column ordering: id, acceptedAnswer, creationDate, questionScore, title, ownerUserID, tags, answerID, answerScore, creationDate
		for (String row : data) {
			if (row.contains("CreationDate"))
				continue;
			String [] split = row.split(",");
			String[] tags = splitTagsByComma(split[5]);
			int multiple = 0;
			for (String tag : tags) {
				if (languageAnswerMapping.keySet().contains(tag)) {
					if (tag.equals("ruby-on-rails") || tag.equals("ruby"))
						languageAnswerMapping.get("ruby").add(row);
					else if (tag.equals("ios") || tag.equals("swift"))
						languageAnswerMapping.get("swift").add(row);
					else if (tag.equals("java") || tag.equals("javadoc"))
						languageAnswerMapping.get("java").add(row);
					else
						languageAnswerMapping.get(tag).add(row);
					multiple ++;
				}
				if (languageQuestionMapping.keySet().contains(tag)) {
					if (tag.equals("ruby-on-rails") || tag.equals("ruby"))
						languageQuestionMapping.get("ruby").add(row);
					else if (tag.equals("ios") || tag.equals("swift"))
						languageQuestionMapping.get("swift").add(row);
					else if (tag.equals("java") || tag.equals("javadoc"))
						languageQuestionMapping.get("java").add(row);
					else
						languageQuestionMapping.get(tag).add(row);
					multiple ++;
				}
			}
			if (multiple > 1)
				multipleLangsTagged++;
			if (multiple == 0) {
				languageAnswerMapping.get("other").add(row);
				languageQuestionMapping.get("other").add(row);
			}
		}
		
		isQuestionScoreAffectedByLanguage(languageQuestionMapping);
		isAnswerScoreAffectedByLanguage(languageAnswerMapping);
		questionsWithNoAnswers(languageQuestionMapping);
	}
	
	static String [] readFile(String filePath) throws IOException {
		String rawData = new String(Files.readAllBytes(Paths.get(filePath)));
		return rawData.split(System.lineSeparator());
	}
	
	static String [] splitTagsByComma(String tagsList) {
		return tagsList.replaceAll("><", ",").replaceAll("<", "").replaceAll(">", "").replaceAll("\"", "").split(",");
	}
	
	
	//Each of these methods computes the answer to one of the proposed research questions
	static void isQuestionScoreAffectedByLanguage(HashMap<String, Set<String>> languageMapping) {
		for (Map.Entry<String, Set<String>> key : languageMapping.entrySet()) {
			int totalScore = 0;
			for (String post : key.getValue()) {
				totalScore += Integer.parseInt(post.split(",")[3]);
			}
			System.out.println(key.getKey() + " had average question rating of " + (double)totalScore/key.getValue().size());
		}
	}
	
	static void isAnswerScoreAffectedByLanguage(HashMap<String, List<String>> languageMapping) {
		for (Map.Entry<String, List<String>> key : languageMapping.entrySet()) {
			System.out.println(key.getKey()+ " had " + key.getValue().size() + " posts");
			int totalScore=0;
			for (String post : key.getValue()) {
				totalScore += Integer.parseInt(post.split(",")[7].replaceAll("\"", ""));

			}
			System.out.println(key.getKey() + " had average answer rating of " + (double)totalScore/key.getValue().size());
		}
	}
	
	static void questionsWithNoAnswers(HashMap<String, Set<String>> languageMapping) {
		for (Map.Entry<String, Set<String>> key : languageMapping.entrySet()) {
			int hasAnswer = 0;
			for (String post : key.getValue()) {
				String acceptedAnswerID = post.split(",")[1];
				if (!acceptedAnswerID.isEmpty())
					hasAnswer++;
			}
			System.out.println(key.getKey() + " had " +(double)hasAnswer/key.getValue().size()*100 + "% of questions answered");
		}
	}
}
