module watamebot.max0rcustom {
	exports net.foxgenesis.max0r;

	requires java.desktop;
	requires jsr305;
	requires transitive net.dv8tion.jda;
	requires transitive org.slf4j;
	requires transitive watamebot;
	
	provides net.foxgenesis.watame.plugin.IPlugin with net.foxgenesis.max0r.Max0rCustomPlugin;
}