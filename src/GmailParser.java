
public class GmailParser {

	// email info
	private final String raw;
	private String from;
	private String date;
	private String subject;
	private String to;

	private String rest;

	public GmailParser(String raw) {
		this.raw = raw;
		this.parse();
	}

	private void parse() {
		int startIndex = 0;
		int endIndex = 0;

		final String[] keys = {"From: ", "Date: ", "Subject: ", "To: "};

		for (int i = 0; i < 4; ++i) {
			startIndex = this.raw.indexOf(keys[i], endIndex);
			endIndex = this.raw.indexOf("\n", startIndex);
			final String result = this.raw.substring(startIndex + keys[i].length(), endIndex);

			switch (i) {
				case 0 :
					this.from = result;
					break;
				case 1 :
					this.date = result;
					break;
				case 2 :
					this.subject = result;
					break;
				case 3 :
					this.to = result;
					break;
			}
		}

		this.rest = this.raw.substring(endIndex + 1); // assuming this endIndex is always for \n character after "To: "
	}

	public String getRaw() {
		return this.raw;
	}

	public String getFrom() {
		return this.from;
	}

	public String getDate() {
		return this.date;
	}

	public String getSubject() {
		return this.subject;
	}

	public String getTo() {
		return this.to;
	}

	public String getRest() {
		return this.rest;
	}

}
