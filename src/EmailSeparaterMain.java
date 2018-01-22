import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;

public class EmailSeparaterMain {

	static ArrayList<String> categories;
	static HashMap<String, FolderTempStorage> folderMap;

	public static int isCategory(String cat) {
		for (int i = 0; i < categories.size(); ++i) {
			if (cat.toLowerCase().contains(categories.get(i))) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * create folder
	 *
	 * @param parent
	 * @param folderName
	 * @return
	 */
	private static boolean createFolder(Folder parent, String folderName) {
		folderMap.put(folderName, new FolderTempStorage(folderName));

		boolean isCreated = true;

		try {
			final Folder newFolder = parent.getFolder(folderName);
			isCreated = newFolder.create(Folder.HOLDS_MESSAGES);
			System.out.println(folderName + " folder created: " + isCreated);

		} catch (final Exception e) {
			System.out.println("Error creating folder: " + e.getMessage());
			e.printStackTrace();
			isCreated = false;
		}
		System.out.println(folderName + " folder is created: " + isCreated);
		return isCreated;
	}

	public static void main(String[] args) throws Exception {

		String configPath = "config.cfg";

		if (args.length != 0) {
			configPath = args[0];
		}

		final Config cfg = new Config(configPath);
		final String email_username = cfg.getProperty("email_username");
		final String email_password = cfg.getProperty("email_password");
		final String email_default_inbox = cfg.getProperty("email_default_inbox");
		final String bluemix_username = cfg.getProperty("bluemix_username");
		final String bluemix_password = cfg.getProperty("bluemix_password");
		final boolean sentiment_positive = cfg.getProperty("sentiment_positive").equalsIgnoreCase("yes");
		final boolean sentiment_negative = cfg.getProperty("sentiment_negative").equalsIgnoreCase("yes");
		final boolean sentiment_used = sentiment_positive || sentiment_negative;

		final Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
			final Session session = Session.getDefaultInstance(props, null);
			final Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", email_username, email_password);

			final Folder inbox = store.getFolder(email_default_inbox);
			inbox.open(Folder.READ_WRITE);

			// initializing folderMap
			folderMap = new HashMap<String, FolderTempStorage>();

			// get keywords and create folders which have same name as keywords
			categories = new ArrayList<String>();
			final String KEYWORD = "keyword";
			int keywordIndex = 0;
			String keywordKey = "keyword" + keywordIndex;
			while (cfg.getProperty(keywordKey) != null) {
				System.out.println("Adding keyword: " + cfg.getProperty(keywordKey));

				// adding categories
				final String keyword = cfg.getProperty(keywordKey).toLowerCase();
				categories.add(keyword);

				if (sentiment_used) {
					// adding folders for {sentiment_keyword}
					if (sentiment_positive) {
						createFolder(inbox, "positive_" + keyword);
					}
					if (sentiment_negative) {
						createFolder(inbox, "negative_" + keyword);
					}
				}
				// adding folders for {keyword} for neutral sentiment or no sentiment
				createFolder(inbox, keyword);

				++keywordIndex;
				keywordKey = KEYWORD + keywordIndex;
			}

			// no key is used. Just separate emails by sentiment
			if (keywordIndex == 0) {
				createFolder(inbox, "positive");
				createFolder(inbox, "negative");
			}

			new BufferedReader(new InputStreamReader(System.in));

			final Message message[] = inbox.getMessages();
			final NaturalLanguageUnderstandingForFeedbackEmail nlu = new NaturalLanguageUnderstandingForFeedbackEmail(bluemix_username, bluemix_password);

			for (int i = 0, n = message.length; i < n; i++) {

				Object content = message[i].getContent();

				// message[i].getRecipients(null);'

				if (content instanceof String) {
					final String body = (String) content;
					System.out.println("Body!!!!!: " + body);
				} else if (content instanceof Multipart) {
					final Multipart multipart = (Multipart) content;
					for (int j = 0; j < multipart.getCount(); j++) {

						final BodyPart bodyPart = multipart.getBodyPart(j);
						final String disposition = bodyPart.getDisposition();

						if ((disposition != null) && (disposition.equalsIgnoreCase("ATTACHMENT"))) { // BodyPart.ATTACHMENT doesn't work for gmail
							System.out.println("Mail have some attachment");

							bodyPart.getDataHandler();
						} else {
							content = bodyPart.getContent().toString();
							if ((j % 2) == 0) {

								final LiteAppFeedbackParser lp = new LiteAppFeedbackParser(content.toString());

								if (!lp.getBody().trim().isEmpty()) {
									nlu.putBodyAndGetResponse(lp.getBody());

									final List<KeywordsResult> keywords = nlu.getKeywords();
									for (final KeywordsResult keyword : keywords) {
										// if the keyword is category -> move folder
										if (isCategory(keyword.getText()) != -1) {
											final String category = categories.get(isCategory(keyword.getText()));

											String folderName = category;
											if (sentiment_used) {
												// use {sentiment}_{keyword}

												if (nlu.getSentimentLabel().equalsIgnoreCase("positive") && sentiment_positive) {
													folderName = "positive_" + category;

												} else if (nlu.getSentimentLabel().equalsIgnoreCase("negative") && sentiment_negative) {
													folderName = "negative_" + category;

												}

											} // else .. just use {keyword}

											final ArrayList<Message> msgs = folderMap.get(folderName).getMessages();
											msgs.add(message[i]);

										}
									}
								}
							}
						}
					}
				}
			}

			for (final String folderMapKey : folderMap.keySet()) {
				System.out.println("folderMapKey: " + folderMapKey);
				final ArrayList<Message> messageList = folderMap.get(folderMapKey).getMessages();
				Message[] msgs = new Message[messageList.size()];
				msgs = messageList.toArray(msgs);
				final Folder folder = store.getFolder(email_default_inbox + "/" + folderMapKey);
				inbox.copyMessages(msgs, folder);
				inbox.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
			}

		} catch (final MessagingException e) {
			e.printStackTrace();
			System.exit(2);
		}

	}

}
