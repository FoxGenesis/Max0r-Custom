module watamebot.max0rcustom {
	exports net.foxgenesis.max0r;

	requires transitive watamebot;
	requires org.json;
	
	provides net.foxgenesis.watame.plugin.Plugin with net.foxgenesis.max0r.Max0rCustomPlugin;

}