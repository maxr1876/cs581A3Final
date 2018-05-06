import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class AnalyticsEngine {
	static final String[] languages = new String("c,c#,c++,java,python,ruby,ruby-on-rails,php,perl,"
			+ "javascript,haskell,ios,swift,sql,lisp,javadoc,mysql").split(",");
	static int multipleLangsTagged = 0; //represents the number of posts that have multiple languages tagged
	public static void main(String [] args) throws IOException, ParseException {
		
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
		languageAnswerMapping.remove("mysql");
		languageQuestionMapping.remove("mysql");
		String [] data = readFile("QueryResults.csv");	
		//Column ordering: 
		//Id	AcceptedAnswerId	CreationDate	Score	OwnerUserId	Tags	AnswerCount	Id	CreationDate	Score

		for (String row : data) {
			if (row.contains("CreationDate"))
				continue;//ignore headers
			String [] split = row.split(",");
			String[] tags = splitTagsByComma(split[5]);
			String questionRow = split[0] + "," + split[1] + "," + split[2] + "," +  split[3] + ","+split[4]+","+split[5];
			int multiple = 0;
			for (String tag : tags) {
				if (languageAnswerMapping.keySet().contains(tag)) {
					if (tag.equals("ruby-on-rails") || tag.equals("ruby"))
						languageAnswerMapping.get("ruby").add(row);
					else if (tag.equals("ios") || tag.equals("swift"))
						languageAnswerMapping.get("swift").add(row);
					else if (tag.equals("java") || tag.equals("javadoc"))
						languageAnswerMapping.get("java").add(row);
					else if (tag.equals("sql") || tag.equals("mysql"))
						languageAnswerMapping.get("sql").add(row);
					else
						languageAnswerMapping.get(tag).add(row);
					multiple ++;
				}
				
				//don't want repeats for questions, only answers
				if (languageQuestionMapping.keySet().contains(tag)) {
					if (tag.equals("ruby-on-rails") || tag.equals("ruby"))
						languageQuestionMapping.get("ruby").add(questionRow);
					else if (tag.equals("ios") || tag.equals("swift"))
						languageQuestionMapping.get("swift").add(questionRow);
					else if (tag.equals("java") || tag.equals("javadoc"))
						languageQuestionMapping.get("java").add(questionRow);
					else if (tag.equals("sql") || tag.equals("mysql"))
						languageQuestionMapping.get("sql").add(questionRow);
					else
						languageQuestionMapping.get(tag).add(questionRow);
					multiple ++;
				}
			}
			if (multiple > 1)
				multipleLangsTagged++;
			if (multiple == 0) {
				languageAnswerMapping.get("other").add(row);
				languageQuestionMapping.get("other").add(questionRow);
			}
		}
		
		isQuestionScoreAffectedByLanguage(languageQuestionMapping);
		isAnswerScoreAffectedByLanguage(languageAnswerMapping);
		questionsWithNoAnswers(languageQuestionMapping);
		doesQuestionQualityAffectAnswerSpeed(languageAnswerMapping);
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
				totalScore += Integer.parseInt(post.split(",")[3].replaceAll("\"", ""));
			}
			System.out.println(key.getKey() + " had " + key.getValue().size() + " questions");
			System.out.println(key.getKey() + " had average question rating of " + (double)totalScore/key.getValue().size());
		}
	}
	
	static void isAnswerScoreAffectedByLanguage(HashMap<String, List<String>> languageMapping) {
		for (Map.Entry<String, List<String>> key : languageMapping.entrySet()) {
			int totalScore=0;
			for (String post : key.getValue()) {
				totalScore += Integer.parseInt(post.split(",")[9].replaceAll("\"", ""));

			}
			System.out.println(key.getKey() + " had " + key.getValue().size() + " answers");
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
			System.out.println(key.getKey() + " had " +(double)hasAnswer/key.getValue().size()*100 + "% of questions with accepted answers");
		}
	}
	
	static void doesQuestionQualityAffectAnswerSpeed(HashMap<String, List<String>> languageAnswerMapping) 
			throws FileNotFoundException, ParseException {
//		File outFile = new File("questionQualityVSanswerSpeed");
//		if (outFile.exists())
//			outFile.delete();
//		PrintWriter writer = new PrintWriter(outFile);
		for (Map.Entry<String, List<String>> entry : languageAnswerMapping.entrySet()) {
//			writer.write(entry.getKey() + ":" +System.lineSeparator());
			HashMap<Integer, List<String>> questionIDToAnswers = new HashMap<>();
			for (String row : entry.getValue()) {
				int qID = Integer.parseInt(row.split(",")[0]);
				if (!questionIDToAnswers.keySet().contains(qID)) {
					questionIDToAnswers.put(qID, new ArrayList<>());
					questionIDToAnswers.get(qID).add(row);
				}
				else
					questionIDToAnswers.get(qID).add(row);
			}
			ArrayList<Long> answerTimes = new ArrayList<>();
			//Now have all answers mapped to their original question, time to find earliest answer for each question
			for (Map.Entry<Integer, List<String>> entry2 : questionIDToAnswers.entrySet()) {
				SimpleDateFormat parser=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				for (String row : entry2.getValue()) {
					answerTimes.add(parser.parse(row.split(",")[8]).getTime()-parser.parse(entry2.getValue().get(0).split(",")[2]).getTime());
				}
				
//				Collections.sort(answerTimes);
//				long quickestAnswerTime = answerTimes.get(0).getTime()-parser.parse(entry2.getValue().get(0).split(",")[2]).getTime();
				//append questionScore, fastestAnswerTime to file
//				writer.append(entry2.getValue().get(0).split(",")[3] + "," + ((double)quickestAnswerTime/1000/60/60)+System.lineSeparator());
			}
			long sum = 0;
			for (long time : answerTimes) {
				if (time/1000/60 > 1000)
					continue;
				sum += (time/1000/60);
			}
			System.out.println(entry.getKey() + " had an average answer time of " + (double)(sum/answerTimes.size()) + " minutes");
		}
//		writer.close();
		
	}
}
