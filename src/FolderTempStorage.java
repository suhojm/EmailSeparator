import java.util.ArrayList;

import javax.mail.Message;

public class FolderTempStorage {
	private String name;
	private final ArrayList<Message> msgs;

	public FolderTempStorage(String name) {
		this.msgs = new ArrayList<Message>();
	}

	public String getName() {
		return this.name;
	}

	public ArrayList<Message> getMessages() {
		return this.msgs;
	}
}
