module watamebot.max0rcustom {
	exports net.foxgenesis.max0r;

	requires java.desktop;
	requires transitive net.dv8tion.jda;
	requires transitive org.slf4j;
	requires transitive watamebot;
	requires org.apache.commons.configuration2;
	
	//provides net.foxgenesis.watame.plugin.IPlugin with net.foxgenesis.max0r.Max0rCustomPlugin;
	provides net.foxgenesis.watame.plugin.Plugin with net.foxgenesis.max0r.Max0rCustomPlugin;

}