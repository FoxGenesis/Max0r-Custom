module watamebot.max0rcustom {
	exports net.foxgenesis.max0r;

	requires transitive watamebot;

	requires static org.jetbrains.annotations;
	requires java.desktop;
	requires net.dv8tion.jda;
	requires org.apache.commons.lang3;
	
	exports net.foxgenesis.max0r.api to com.fasterxml.jackson.databind;

	provides net.foxgenesis.watame.plugin.Plugin with net.foxgenesis.max0r.Max0rCustomPlugin;

}