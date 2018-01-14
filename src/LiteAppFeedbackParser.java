
public class LiteAppFeedbackParser extends GmailParser {

	private final String rest;

	// device info
	private String androidVersion;
	private String appVersion;
	private String localDateTime;
	private String location;
	private String locale;
	private String mfg;
	private String model;

	// body
	private String body;
	// private String body20;

	public LiteAppFeedbackParser(String raw) {
		super(raw);
		this.rest = this.getRest();
		this.parse();
	}

	private String trimPleaseEnterCommentsBelow(String txt, String sentenceToTrim) {
		if (txt.indexOf(sentenceToTrim) != -1) {
			return txt.replace(sentenceToTrim, "").trim();
		}

		final String sentenceNoSpace = sentenceToTrim.replaceAll("\\s+", "");
		String txtNoSpace = txt.replaceAll("\\s+", "");
		if (txtNoSpace.indexOf(sentenceNoSpace) != -1) {
			final String[] words = sentenceToTrim.split("\\s+");
			final String txtcopy = txt;

			int searchingIndex = 0;
			int firstWordIndex = txtcopy.indexOf(words[0], searchingIndex);
			boolean b = firstWordIndex != -1;

			while (b) {
				final String txtsubstr = txtcopy.substring(firstWordIndex, firstWordIndex + sentenceToTrim.length() + 1);
				txtNoSpace = txtsubstr.replaceAll("\\s+", "");
				if (txtNoSpace.indexOf(sentenceNoSpace) != -1) {
					// found it
					final String result = txtcopy.substring(0, firstWordIndex) + txtcopy.substring(firstWordIndex + 1 + sentenceToTrim.length());
					return result;
				}
				searchingIndex = txt.indexOf(words[0], searchingIndex + words[0].length());
				firstWordIndex = txtcopy.indexOf(words[0], searchingIndex);
				b = firstWordIndex != -1;
			}
		}

		return txt;
	}

	private void parse() {
		final String tableBorder = "===========================";
		final String[] keys = {"Android Version: ", "App Version: ", "Local Date-Time: ", "Location: ", "Locale: ", "Mfg: ", "Model: "};

		int startIndex = this.rest.indexOf(tableBorder) + tableBorder.length();
		int endIndex = startIndex;;

		for (int i = 0; i < 7; ++i) {
			String result = "";
			if (this.rest.indexOf(keys[i], endIndex) != -1) {
				startIndex = this.rest.indexOf(keys[i], endIndex);

				if (this.rest.indexOf("\n", startIndex) != -1) {
					endIndex = this.rest.indexOf("\n", startIndex);
					result = this.rest.substring(startIndex + keys[i].length(), endIndex);
				}
			}

			switch (i) {
				case 0 :
					this.androidVersion = result;
					break;
				case 1 :
					this.appVersion = result;
					break;
				case 2 :
					this.localDateTime = result;
					break;
				case 3 :
					this.location = result;
					break;
				case 4 :
					this.locale = result;
					break;
				case 5 :
					this.mfg = result;
					break;
				case 6 :
					this.model = result;
					break;
			}
		}

		if (this.rest.indexOf(tableBorder, endIndex) != -1) {
			endIndex = this.rest.indexOf(tableBorder, endIndex) + tableBorder.length();
		}
		this.body = this.rest.substring(endIndex + 1);
		this.body = this.body.trim();

		this.body = this.trimPleaseEnterCommentsBelow(this.body, "Please enter your comments below. Thank you for helping us improve our app!");
		this.body = this.trimPleaseEnterCommentsBelow(this.body, "Escribe tus comentarios a continuación. Gracias por ayudarnos a mejorar nuestra app.");
		this.body = this.body.trim();
		// this.body20 = this.body.replaceAll("\\s+", "%20");
	}

	public String getAndroidVersion() {
		return this.androidVersion;
	}

	public String getAppVersion() {
		return this.appVersion;
	}

	public String getLocalDateTime() {
		return this.localDateTime;
	}

	public String getLocation() {
		return this.location;
	}

	public String getLocale() {
		return this.locale;
	}

	public String getMfg() {
		return this.mfg;
	}

	public String getModel() {
		return this.model;
	}

	public String getBody() {
		return this.body;
	}

	// public String getBody20() {
	// return this.body20;
	// }

}
