import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentResult;

public class NaturalLanguageUnderstandingForFeedbackEmail {
	String body;
	String username;
	String password;

	NaturalLanguageUnderstanding service;
	SentimentOptions sentimentOptions;
	KeywordsOptions keywordsOptions;
	EntitiesOptions entitiesOptions;
	Features features;

	AnalysisResults response;

	public NaturalLanguageUnderstandingForFeedbackEmail(String username, String password) {
		this.username = username;
		this.password = password;

		this.setup();
	}

	public void setup() {
		this.service = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27, this.username, this.password);
		this.sentimentOptions = new SentimentOptions.Builder().build();
		this.keywordsOptions = new KeywordsOptions.Builder().sentiment(true).build();
		this.entitiesOptions = new EntitiesOptions.Builder().emotion(true).sentiment(true).build();

		// final Features features = new Features.Builder().entities(entitiesOptions).keywords(keywordsOptions).build();
		this.features = new Features.Builder().sentiment(this.sentimentOptions).keywords(this.keywordsOptions).entities(this.entitiesOptions).build();
	}

	public void putBodyAndGetResponse(String body) {
		this.body = body;

		final AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(this.body).features(this.features).build();
		this.response = this.service.analyze(parameters).execute();
	}

	public SentimentResult getSetiment() {
		return this.response.getSentiment();
	}

	public String getSentimentLabel() {
		final String sentimentJSON = this.response.getSentiment().toString();
		return new JSONParser(sentimentJSON).getParsedValue(new ArrayList<String>(Arrays.asList("document", "label")));
	}

	public List<KeywordsResult> getKeywords() {
		return this.response.getKeywords();
	}
}
