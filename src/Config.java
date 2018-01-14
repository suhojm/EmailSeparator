import java.io.FileInputStream;
import java.util.Properties;

public class Config {
	Properties configFile;

	public Config() {
		this.configFile = new Properties();
		try {
			this.configFile.load(this.getClass().getClassLoader().getResourceAsStream("config.cfg"));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public Config(String cfgDir) {
		this.configFile = new Properties();
		try {
			this.configFile.load(new FileInputStream(cfgDir));
		} catch (final Exception e) {
			System.out.println("HERE");
			e.printStackTrace();
		}
	}

	public String getProperty(String key) {
		return this.configFile.getProperty(key);
	}
}
