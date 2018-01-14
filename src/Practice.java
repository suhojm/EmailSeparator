import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;

public class Practice {

	final String[] categories = {"temperature", "weather", "location"};

	public int isCategory(String cat) {
		for (int i = 0; i < this.categories.length; ++i) {
			if (cat.toLowerCase().contains(this.categories[i])) {
				return i;
			}
		}

		return -1;
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

		final Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
			final Session session = Session.getDefaultInstance(props, null);
			final Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", email_username, email_password);

			final Folder inbox = store.getFolder(email_default_inbox);
			inbox.open(Folder.READ_WRITE);

			new BufferedReader(new InputStreamReader(System.in));

			final Message message[] = inbox.getMessages();

			final NaturalLanguageUnderstandingForFeedbackEmail nlu = new NaturalLanguageUnderstandingForFeedbackEmail(bluemix_username, bluemix_password);

			// Message message[] = folder.getMessages();
			for (int i = 0, n = message.length; i < n; i++) {
				// System.out.println(i + ": " + message[i].getFrom()[0] + "\t" + message[i].getSubject());
				//
				// System.out.println("Do you want to read message? " + "[YES to read/QUIT to end]");
				// final String line = reader.readLine();
				// if ("YES".equals(line)) {
				// message[i].writeTo(System.out);
				// } else if ("QUIT".equals(line)) {
				// break;
				// }

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
							// System.out.println("Body@@@@@: " + bodyPart.getContent());
							content = bodyPart.getContent().toString();
							if ((j % 2) == 0) {

								System.out.println("\n\ni: " + i + "\n\n");
								final LiteAppFeedbackParser lp = new LiteAppFeedbackParser(content.toString());
								// System.out.println(content);
								System.out.println("from: " + lp.getFrom());
								System.out.println("date: " + lp.getDate());
								System.out.println("subject: " + lp.getSubject());
								System.out.println("to: " + lp.getTo());
								System.out.println("android version: " + lp.getAndroidVersion());
								System.out.println("app version: " + lp.getAppVersion());
								System.out.println("localDateTime: " + lp.getLocalDateTime());
								System.out.println("location: " + lp.getLocation());
								System.out.println("locale: " + lp.getLocale());
								System.out.println("mfg: " + lp.getMfg());
								System.out.println("model: " + lp.getModel());
								System.out.println("body: " + lp.getBody());
								// System.out.println("body20: " + lp.getBody20());

								if (!lp.getBody().trim().isEmpty()) {
									nlu.putBodyAndGetResponse(lp.getBody());

									System.out.println("@@ Sentiment: " + nlu.getSentimentLabel());

									final List<KeywordsResult> keywords = nlu.getKeywords();
									System.out.println("@@ Keywords: ");
									for (final KeywordsResult keyword : keywords) {

										System.out.println("keyword: " + keyword.getText());
									}
								}
							}
						}
					}
				}
			}
		} catch (final MessagingException e) {
			e.printStackTrace();
			System.exit(2);
		}

	}

}
